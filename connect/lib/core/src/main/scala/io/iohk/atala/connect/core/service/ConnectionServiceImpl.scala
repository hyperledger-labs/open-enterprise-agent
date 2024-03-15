package io.iohk.atala.connect.core.service

import io.iohk.atala.connect.core.model.ConnectionRecord
import io.iohk.atala.connect.core.model.ConnectionRecord.*
import io.iohk.atala.connect.core.model.error.ConnectionServiceError
import io.iohk.atala.connect.core.model.error.ConnectionServiceError.*
import io.iohk.atala.connect.core.repository.ConnectionRepository
import io.iohk.atala.mercury.*
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.connection.*
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation
import io.iohk.atala.shared.models.WalletAccessContext
import io.iohk.atala.shared.utils.Base64Utils
import io.iohk.atala.shared.utils.aspects.CustomMetricsAspect
import zio.*

import java.time.{Duration, Instant}
import java.util.UUID
private class ConnectionServiceImpl(
    connectionRepository: ConnectionRepository,
    maxRetries: Int = 5, // TODO move to config
) extends ConnectionService {

  override def createConnectionInvitation(
      label: Option[String],
      goalCode: Option[String],
      goal: Option[String],
      pairwiseDID: DidId
  ): URIO[WalletAccessContext, ConnectionRecord] =
    for {
      invitation <- ZIO.succeed(ConnectionInvitation.makeConnectionInvitation(pairwiseDID, goalCode, goal))
      record <- ZIO.succeed(
        ConnectionRecord(
          id = UUID.fromString(invitation.id),
          createdAt = Instant.now,
          updatedAt = None,
          thid = invitation.id,
          label = label,
          goalCode = goalCode,
          goal = goal,
          role = ConnectionRecord.Role.Inviter,
          protocolState = ConnectionRecord.ProtocolState.InvitationGenerated,
          invitation = invitation,
          connectionRequest = None,
          connectionResponse = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      count <- connectionRepository.create(record)
    } yield record

  override def getConnectionRecords(): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    connectionRepository.findAll

  override def getConnectionRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    connectionRepository.findByStates(ignoreWithZeroRetries, limit, states: _*)

  override def getConnectionRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): UIO[Seq[ConnectionRecord]] =
    connectionRepository.findByStatesForAllWallets(ignoreWithZeroRetries, limit, states: _*)

  override def getConnectionRecord(
      recordId: UUID
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    connectionRepository.findById(recordId)

  override def getConnectionRecordByThreadId(
      thid: String
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    connectionRepository.findByThreadId(thid)

  override def deleteConnectionRecord(recordId: UUID): URIO[WalletAccessContext, Unit] =
    connectionRepository.deleteById(recordId)

  override def receiveConnectionInvitation(
      invitation: String
  ): ZIO[WalletAccessContext, InvitationParsingError | InvitationAlreadyReceived, ConnectionRecord] =
    for {
      invitation <- ZIO
        .fromEither(io.circe.parser.decode[Invitation](Base64Utils.decodeUrlToString(invitation)))
        .mapError(err => InvitationParsingError(err.getMessage()))
      maybeRecord <- connectionRepository.findByThreadId(invitation.id)
      _ <- ZIO.noneOrFailWith(maybeRecord)(_ => InvitationAlreadyReceived(invitation.id))
      record <- ZIO.succeed(
        ConnectionRecord(
          id = UUID.randomUUID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = invitation.id,
          label = None,
          goalCode = invitation.body.goal_code,
          goal = invitation.body.goal,
          role = ConnectionRecord.Role.Invitee,
          protocolState = ConnectionRecord.ProtocolState.InvitationReceived,
          invitation = invitation,
          connectionRequest = None,
          connectionResponse = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      _ <- connectionRepository.create(record)
    } yield record

  override def acceptConnectionInvitation(
      recordId: UUID,
      pairwiseDid: DidId
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    for {
      record <- getRecordByIdAndStates(recordId, ProtocolState.InvitationReceived)
      request = ConnectionRequest
        .makeFromInvitation(record.invitation, pairwiseDid)
        .copy(thid = Some(record.invitation.id))
      _ <- connectionRepository
        .updateWithConnectionRequest(recordId, request, ProtocolState.ConnectionRequestPending, maxRetries)
        @@ CustomMetricsAspect.startRecordingTime(
          s"${record.id}_invitee_pending_to_req_sent"
        )
      maybeRecord <- connectionRepository
        .findById(record.id)
      record <- ZIO.getOrFailWith(new RecordIdNotFound(recordId))(maybeRecord)
    } yield record

  override def markConnectionRequestSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    for {
      record <- getRecordByIdAndStates(recordId, ProtocolState.ConnectionRequestPending)
      updatedRecord <- updateConnectionProtocolState(
        recordId,
        ProtocolState.ConnectionRequestPending,
        ProtocolState.ConnectionRequestSent
      )
    } yield updatedRecord

  override def markConnectionInvitationExpired(
      recordId: UUID
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordByIdAndStates(recordId, ProtocolState.InvitationGenerated)
      updatedRecord <- updateConnectionProtocolState(
        recordId,
        ProtocolState.InvitationGenerated,
        ProtocolState.InvitationExpired
      )
    } yield updatedRecord

  override def receiveConnectionRequest(
      request: ConnectionRequest,
      expirationTime: Option[Duration] = None
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordByThreadIdAndStates(
        request.thid.getOrElse(request.id),
        ProtocolState.InvitationGenerated
      )
      _ <- expirationTime.fold {
        ZIO.unit
      } { expiryDuration =>
        val actualDuration = Duration.between(record.createdAt, Instant.now())
        if (actualDuration > expiryDuration) {
          for {
            _ <- markConnectionInvitationExpired(record.id)
            result <- ZIO.fail(InvitationExpired(record.id.toString))
          } yield result
        } else ZIO.unit
      }
      _ <- connectionRepository.updateWithConnectionRequest(
        record.id,
        request,
        ProtocolState.ConnectionRequestReceived,
        maxRetries
      )
      maybeRecord <- connectionRepository.findById(record.id)
      record <- ZIO.getOrFailWith(RecordIdNotFound(record.id))(maybeRecord)
    } yield record

  override def acceptConnectionRequest(
      recordId: UUID
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordByIdAndStates(recordId, ProtocolState.ConnectionRequestReceived)
      response <- {
        record.connectionRequest.map(_.makeMessage).map(ConnectionResponse.makeResponseFromRequest(_)) match
          case None                  => ZIO.fail(RepositoryError.apply(new RuntimeException("Unable to make Message")))
          case Some(Left(value))     => ZIO.fail(RepositoryError.apply(new RuntimeException(value)))
          case Some(Right(response)) => ZIO.succeed(response)
      }
      _ <- connectionRepository
        .updateWithConnectionResponse(recordId, response, ProtocolState.ConnectionResponsePending, maxRetries)
        @@ CustomMetricsAspect.startRecordingTime(
          s"${record.id}_inviter_pending_to_res_sent"
        )
      maybeRecord <- connectionRepository.findById(record.id)
      record <- ZIO.getOrFailWith(RecordIdNotFound(record.id))(maybeRecord)
    } yield record

  override def markConnectionResponseSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordByIdAndStates(recordId, ProtocolState.ConnectionResponsePending)
      updatedRecord <- updateConnectionProtocolState(
        recordId,
        ProtocolState.ConnectionResponsePending,
        ProtocolState.ConnectionResponseSent,
      )
    } yield updatedRecord

  override def receiveConnectionResponse(
      response: ConnectionResponse
  ): ZIO[WalletAccessContext, ConnectionServiceError, ConnectionRecord] =
    for {
      thid <- ZIO.fromOption(response.thid).mapError(_ => ThreadIdMissingInMessage)
      record <- getRecordByThreadIdAndStates(
        thid,
        ProtocolState.ConnectionRequestPending,
        ProtocolState.ConnectionRequestSent
      )
      _ <- connectionRepository.updateWithConnectionResponse(
        record.id,
        response,
        ProtocolState.ConnectionResponseReceived,
        maxRetries
      )
      maybeRecord <- connectionRepository.findById(record.id)
      record <- ZIO.getOrFailWith(RecordIdNotFound(record.id))(maybeRecord)
    } yield record

  private[this] def getRecordByIdAndStates(
      recordId: UUID,
      states: ProtocolState*
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] = {
    for {
      maybeRecord <- connectionRepository.findById(recordId)
      record <- ZIO.fromOption(maybeRecord).mapError(_ => RecordIdNotFound(recordId))
      _ <- ensureRecordHasExpectedState(record, states*)
    } yield record
  }

  private[this] def getRecordByThreadIdAndStates(
      thid: String,
      states: ProtocolState*
  ): ZIO[WalletAccessContext, ThreadIdNotFound | InvalidStateForOperation, ConnectionRecord] = {
    for {
      maybeRecord <- connectionRepository.findByThreadId(thid)
      record <- ZIO.fromOption(maybeRecord).mapError(_ => ThreadIdNotFound(thid))
      _ <- ensureRecordHasExpectedState(record, states*)
    } yield record
  }

  private[this] def ensureRecordHasExpectedState(record: ConnectionRecord, states: ProtocolState*) =
    record.protocolState match {
      case s if states.contains(s) => ZIO.unit
      case state => ZIO.fail(InvalidStateForOperation(s"Invalid protocol state for operation: $state"))
    }

  private[this] def updateConnectionProtocolState(
      recordId: UUID,
      from: ProtocolState,
      to: ProtocolState,
  ): URIO[WalletAccessContext, ConnectionRecord] = {
    for {
      _ <- connectionRepository.updateProtocolState(recordId, from, to, maxRetries)
      record <- connectionRepository.getById(recordId)
    } yield record
  }

  def reportProcessingFailure(
      recordId: UUID,
      failReason: Option[String]
  ): URIO[WalletAccessContext, Unit] =
    connectionRepository.updateAfterFail(recordId, failReason)

}

object ConnectionServiceImpl {
  val layer: URLayer[ConnectionRepository, ConnectionService] =
    ZLayer.fromFunction(ConnectionServiceImpl(_))
}
