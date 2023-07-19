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
import io.iohk.atala.shared.utils.Base64Utils
import zio.*

import java.rmi.UnexpectedException
import java.time.Instant
import java.util.UUID

private class ConnectionServiceImpl(
    connectionRepository: ConnectionRepository[Task],
    maxRetries: Int = 5, // TODO move to config
) extends ConnectionService {

  override def createConnectionInvitation(
      label: Option[String],
      pairwiseDID: DidId
  ): IO[ConnectionServiceError, ConnectionRecord] =
    for {
      invitation <- ZIO.succeed(ConnectionInvitation.makeConnectionInvitation(pairwiseDID))
      record <- ZIO.succeed(
        ConnectionRecord(
          id = UUID.fromString(invitation.id),
          createdAt = Instant.now,
          updatedAt = None,
          thid = invitation.id,
          label = label,
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
      count <- connectionRepository
        .createConnectionRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
    } yield record

  override def getConnectionRecords(): IO[ConnectionServiceError, Seq[ConnectionRecord]] = {
    for {
      records <- connectionRepository.getConnectionRecords
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def getConnectionRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): IO[ConnectionServiceError, Seq[ConnectionRecord]] = {
    for {
      records <- connectionRepository
        .getConnectionRecordsByStates(ignoreWithZeroRetries, limit, states: _*)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def getConnectionRecord(recordId: UUID): IO[ConnectionServiceError, Option[ConnectionRecord]] = {
    for {
      record <- connectionRepository
        .getConnectionRecord(recordId)
        .mapError(RepositoryError.apply)
    } yield record
  }

  override def getConnectionRecordByThreadId(thid: String): IO[ConnectionServiceError, Option[ConnectionRecord]] =
    for {
      record <- connectionRepository
        .getConnectionRecordByThreadId(thid)
        .mapError(RepositoryError.apply)
    } yield record

  override def deleteConnectionRecord(recordId: UUID): IO[ConnectionServiceError, Int] = ???

  override def receiveConnectionInvitation(invitation: String): IO[ConnectionServiceError, ConnectionRecord] =
    for {
      invitation <- ZIO
        .fromEither(io.circe.parser.decode[Invitation](Base64Utils.decodeUrlToString(invitation)))
        .mapError(err => InvitationParsingError(err))
      _ <- connectionRepository
        .getConnectionRecordByThreadId(invitation.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None    => ZIO.unit
          case Some(_) => ZIO.fail(InvitationAlreadyReceived(invitation.id))
        }
      record <- ZIO.succeed(
        ConnectionRecord(
          id = UUID.randomUUID(),
          createdAt = Instant.now,
          updatedAt = None,
          // TODO: According to the standard, we should rather use 'pthid' and not 'thid'
          thid = invitation.id,
          label = None,
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
      count <- connectionRepository
        .createConnectionRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
    } yield record

  override def acceptConnectionInvitation(
      recordId: UUID,
      pairwiseDid: DidId
  ): IO[ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordWithState(recordId, ProtocolState.InvitationReceived)
      request = ConnectionRequest
        .makeFromInvitation(record.invitation, pairwiseDid)
        .copy(thid = Some(record.invitation.id)) //  This logic shound be move to the SQL when fetching the record
      count <- connectionRepository
        .updateWithConnectionRequest(recordId, request, ProtocolState.ConnectionRequestPending, maxRetries)
        .mapError(RepositoryError.apply)
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- connectionRepository
        .getConnectionRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(recordId))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record

  override def markConnectionRequestSent(recordId: UUID): IO[ConnectionServiceError, ConnectionRecord] =
    updateConnectionProtocolState(
      recordId,
      ProtocolState.ConnectionRequestPending,
      ProtocolState.ConnectionRequestSent
    ).flatMap {
      case None        => ZIO.fail(RecordIdNotFound(recordId))
      case Some(value) => ZIO.succeed(value)
    }

  override def receiveConnectionRequest(
      request: ConnectionRequest
  ): IO[ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordFromThreadIdAndState(
        Some(request.thid.orElse(request.pthid).getOrElse(request.id)),
        ProtocolState.InvitationGenerated
      )
      _ <- connectionRepository
        .updateWithConnectionRequest(record.id, request, ProtocolState.ConnectionRequestReceived, maxRetries)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
      record <- connectionRepository
        .getConnectionRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record

  override def acceptConnectionRequest(recordId: UUID): IO[ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordWithState(recordId, ProtocolState.ConnectionRequestReceived)
      response <- {
        record.connectionRequest.map(_.makeMessage).map(ConnectionResponse.makeResponseFromRequest(_)) match
          case None                  => ZIO.fail(RepositoryError.apply(new RuntimeException("Unable to make Message")))
          case Some(Left(value))     => ZIO.fail(RepositoryError.apply(new RuntimeException(value)))
          case Some(Right(response)) => ZIO.succeed(response)
      }
      // response = createDidCommConnectionResponse(record)
      count <- connectionRepository
        .updateWithConnectionResponse(recordId, response, ProtocolState.ConnectionResponsePending, maxRetries)
        .mapError(RepositoryError.apply)
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- connectionRepository
        .getConnectionRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record

  override def markConnectionResponseSent(recordId: UUID): IO[ConnectionServiceError, ConnectionRecord] =
    updateConnectionProtocolState(
      recordId,
      ProtocolState.ConnectionResponsePending,
      ProtocolState.ConnectionResponseSent,
    ).flatMap {
      case None        => ZIO.fail(RecordIdNotFound(recordId))
      case Some(value) => ZIO.succeed(value)
    }

  override def receiveConnectionResponse(
      response: ConnectionResponse
  ): IO[ConnectionServiceError, ConnectionRecord] =
    for {
      record <- getRecordFromThreadIdAndState(
        response.thid.orElse(response.pthid),
        ProtocolState.ConnectionRequestPending,
        ProtocolState.ConnectionRequestSent
      )
      _ <- connectionRepository
        .updateWithConnectionResponse(record.id, response, ProtocolState.ConnectionResponseReceived, maxRetries)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
      record <- connectionRepository
        .getConnectionRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record

  private[this] def getRecordWithState(
      recordId: UUID,
      state: ProtocolState
  ): IO[ConnectionServiceError, ConnectionRecord] = {
    for {
      maybeRecord <- connectionRepository
        .getConnectionRecord(recordId)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => RecordIdNotFound(recordId))
      _ <- record.protocolState match {
        case s if s == state => ZIO.unit
        case state           => ZIO.fail(InvalidFlowStateError(s"Invalid protocol state for operation: $state"))
      }
    } yield record
  }

  private[this] def updateConnectionProtocolState(
      recordId: UUID,
      from: ProtocolState,
      to: ProtocolState,
  ): IO[ConnectionServiceError, Option[ConnectionRecord]] = {
    for {
      _ <- connectionRepository
        .updateConnectionProtocolState(recordId, from, to, maxRetries)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
      record <- connectionRepository
        .getConnectionRecord(recordId)
        .mapError(RepositoryError.apply)
    } yield record
  }

  private[this] def getRecordFromThreadIdAndState(
      thid: Option[String],
      states: ProtocolState*
  ): IO[ConnectionServiceError, ConnectionRecord] = {
    for {
      thid <- ZIO
        .fromOption(thid)
        .mapError(_ => UnexpectedError("No `thid` found in credential request"))
      maybeRecord <- connectionRepository
        .getConnectionRecordByThreadId(thid)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => ThreadIdNotFound(thid))
      _ <- record.protocolState match {
        case s if states.contains(s) => ZIO.unit
        case state                   => ZIO.fail(InvalidFlowStateError(s"Invalid protocol state for operation: $state"))
      }
    } yield record
  }

  def reportProcessingFailure(recordId: UUID, failReason: Option[String]): IO[ConnectionServiceError, Unit] =
    connectionRepository
      .updateAfterFail(recordId, failReason)
      .mapError(RepositoryError.apply)
      .flatMap {
        case 1 => ZIO.unit
        case n => ZIO.fail(UnexpectedError(s"Invalid number of records updated: $n"))
      }

}

object ConnectionServiceImpl {
  val layer: URLayer[ConnectionRepository[Task], ConnectionService] =
    ZLayer.fromFunction(ConnectionServiceImpl(_))
}
