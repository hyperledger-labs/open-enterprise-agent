package org.hyperledger.identus.connect.core.service

import org.hyperledger.identus.*
import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError
import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError.*
import org.hyperledger.identus.connect.core.model.ConnectionRecord
import org.hyperledger.identus.connect.core.model.ConnectionRecord.*
import org.hyperledger.identus.connect.core.repository.ConnectionRepository
import org.hyperledger.identus.mercury.model.DidId
import org.hyperledger.identus.mercury.protocol.connection.*
import org.hyperledger.identus.mercury.protocol.invitation.v2.Invitation
import org.hyperledger.identus.shared.models.WalletAccessContext
import org.hyperledger.identus.shared.utils.aspects.CustomMetricsAspect
import org.hyperledger.identus.shared.utils.Base64Utils
import org.hyperledger.identus.shared.validation.ValidationUtils
import zio.*
import zio.prelude.*

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
  ): ZIO[WalletAccessContext, UserInputValidationError, ConnectionRecord] =
    for {
      _ <- validateInputs(label, goalCode, goal)
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

  private def validateInputs(
      label: Option[String],
      goalCode: Option[String],
      goal: Option[String]
  ): IO[UserInputValidationError, Unit] = {
    val validation = Validation
      .validate(
        ValidationUtils.validateLengthOptional("label", label, 0, 255),
        ValidationUtils.validateLengthOptional("goalCode", goalCode, 0, 255),
        ValidationUtils.validateLengthOptional("goal", goal, 0, 255)
      )
      .unit
    ZIO.fromEither(validation.toEither).mapError(UserInputValidationError.apply)
  }

  override def findAllRecords(): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    connectionRepository.findAll

  override def findRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    connectionRepository.findByStates(ignoreWithZeroRetries, limit, states*)

  override def findRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): UIO[Seq[ConnectionRecord]] =
    connectionRepository.findByStatesForAllWallets(ignoreWithZeroRetries, limit, states*)

  override def findRecordById(
      recordId: UUID
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    connectionRepository.findById(recordId)

  override def findRecordByThreadId(
      thid: String
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    connectionRepository.findByThreadId(thid)

  override def deleteRecordById(recordId: UUID): URIO[WalletAccessContext, Unit] =
    connectionRepository.deleteById(recordId)

  override def receiveConnectionInvitation(
      invitation: String
  ): ZIO[WalletAccessContext, InvitationParsingError | InvitationAlreadyReceived, ConnectionRecord] =
    for {
      invitation <- ZIO
        .fromEither(io.circe.parser.decode[Invitation](Base64Utils.decodeUrlToString(invitation)))
        .mapError(err => InvitationParsingError(err.getMessage))
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
      record <- ZIO.getOrFailWith(RecordIdNotFound(recordId))(maybeRecord)
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
  ): URIO[WalletAccessContext, ConnectionRecord] =
    for {
      updatedRecord <- updateConnectionProtocolState(
        recordId,
        ProtocolState.InvitationGenerated,
        ProtocolState.InvitationExpired
      )
    } yield updatedRecord

  override def receiveConnectionRequest(
      request: ConnectionRequest,
      expirationTime: Option[Duration] = None
  ): ZIO[WalletAccessContext, ThreadIdNotFound | InvalidStateForOperation | InvitationExpired, ConnectionRecord] =
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
            result <- ZIO.fail(InvitationExpired(record.invitation.id))
          } yield result
        } else ZIO.unit
      }
      _ <- connectionRepository.updateWithConnectionRequest(
        record.id,
        request,
        ProtocolState.ConnectionRequestReceived,
        maxRetries
      )
      record <- connectionRepository.getById(record.id)
    } yield record

  override def acceptConnectionRequest(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    for {
      record <- getRecordByIdAndStates(recordId, ProtocolState.ConnectionRequestReceived)
      request <- ZIO
        .fromOption(record.connectionRequest)
        .orDieWith(_ => RuntimeException(s"No connection request found in record: $recordId"))
      response <- ZIO
        .fromEither(ConnectionResponse.makeResponseFromRequest(request.makeMessage))
        .orDieWith(str => RuntimeException(s"Cannot make response from request: $recordId"))
      _ <- connectionRepository
        .updateWithConnectionResponse(recordId, response, ProtocolState.ConnectionResponsePending, maxRetries)
        @@ CustomMetricsAspect.startRecordingTime(
          s"${record.id}_inviter_pending_to_res_sent"
        )
      record <- connectionRepository.getById(record.id)
    } yield record

  override def markConnectionResponseSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
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
  ): ZIO[
    WalletAccessContext,
    ThreadIdMissingInReceivedMessage | ThreadIdNotFound | InvalidStateForOperation,
    ConnectionRecord
  ] =
    for {
      thid <- ZIO.fromOption(response.thid).mapError(_ => ThreadIdMissingInReceivedMessage(response.id))
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
      record <- connectionRepository.getById(record.id)
    } yield record

  private def getRecordByIdAndStates(
      recordId: UUID,
      states: ProtocolState*
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] = {
    for {
      maybeRecord <- connectionRepository.findById(recordId)
      record <- ZIO.fromOption(maybeRecord).mapError(_ => RecordIdNotFound(recordId))
      _ <- ensureRecordHasExpectedState(record, states*)
    } yield record
  }

  private def getRecordByThreadIdAndStates(
      thid: String,
      states: ProtocolState*
  ): ZIO[WalletAccessContext, ThreadIdNotFound | InvalidStateForOperation, ConnectionRecord] = {
    for {
      maybeRecord <- connectionRepository.findByThreadId(thid)
      record <- ZIO.fromOption(maybeRecord).mapError(_ => ThreadIdNotFound(thid))
      _ <- ensureRecordHasExpectedState(record, states*)
    } yield record
  }

  private def ensureRecordHasExpectedState(record: ConnectionRecord, states: ProtocolState*) =
    record.protocolState match {
      case s if states.contains(s) => ZIO.unit
      case state                   => ZIO.fail(InvalidStateForOperation(state))
    }

  private def updateConnectionProtocolState(
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
      failReason: Option[org.hyperledger.identus.shared.models.Failure]
  ): URIO[WalletAccessContext, Unit] =
    connectionRepository.updateAfterFail(recordId, failReason)

}

object ConnectionServiceImpl {
  val layer: URLayer[ConnectionRepository, ConnectionService] =
    ZLayer.fromFunction(ConnectionServiceImpl(_))
}
