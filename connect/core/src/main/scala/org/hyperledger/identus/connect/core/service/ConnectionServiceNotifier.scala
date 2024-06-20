package org.hyperledger.identus.connect.core.service

import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError
import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError.*
import org.hyperledger.identus.connect.core.model.ConnectionRecord
import org.hyperledger.identus.event.notification.{Event, EventNotificationService}
import org.hyperledger.identus.mercury.model.DidId
import org.hyperledger.identus.mercury.protocol.connection.{ConnectionRequest, ConnectionResponse}
import org.hyperledger.identus.shared.models.WalletAccessContext
import zio.{UIO, URIO, URLayer, ZIO, ZLayer}

import java.time.Duration
import java.util.UUID

class ConnectionServiceNotifier(
    svc: ConnectionService,
    eventNotificationService: EventNotificationService
) extends ConnectionService {

  private val connectionUpdatedEvent = "ConnectionUpdated"

  override def createConnectionInvitation(
      label: Option[String],
      goalCode: Option[String],
      goal: Option[String],
      pairwiseDID: DidId
  ): ZIO[WalletAccessContext, UserInputValidationError, ConnectionRecord] =
    notifyOnSuccess(svc.createConnectionInvitation(label, goalCode, goal, pairwiseDID))

  override def receiveConnectionInvitation(
      invitation: String
  ): ZIO[WalletAccessContext, InvitationParsingError | InvitationAlreadyReceived, ConnectionRecord] =
    notifyOnSuccess(svc.receiveConnectionInvitation(invitation))

  override def acceptConnectionInvitation(
      recordId: UUID,
      pairwiseDid: DidId
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    notifyOnSuccess(svc.acceptConnectionInvitation(recordId, pairwiseDid))

  override def markConnectionRequestSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    notifyOnSuccess(svc.markConnectionRequestSent(recordId))

  override def receiveConnectionRequest(
      request: ConnectionRequest,
      expirationTime: Option[Duration]
  ): ZIO[WalletAccessContext, ThreadIdNotFound | InvalidStateForOperation | InvitationExpired, ConnectionRecord] =
    notifyOnSuccess(svc.receiveConnectionRequest(request, expirationTime))

  override def acceptConnectionRequest(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    notifyOnSuccess(svc.acceptConnectionRequest(recordId))

  override def markConnectionResponseSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    notifyOnSuccess(svc.markConnectionResponseSent(recordId))

  override def markConnectionInvitationExpired(
      recordId: UUID
  ): URIO[WalletAccessContext, ConnectionRecord] =
    notifyOnSuccess(svc.markConnectionInvitationExpired(recordId))

  override def receiveConnectionResponse(
      response: ConnectionResponse
  ): ZIO[
    WalletAccessContext,
    ThreadIdMissingInReceivedMessage | ThreadIdNotFound | InvalidStateForOperation,
    ConnectionRecord
  ] =
    notifyOnSuccess(svc.receiveConnectionResponse(response))

  private def notifyOnSuccess[E](effect: ZIO[WalletAccessContext, E, ConnectionRecord]) =
    for {
      record <- effect
      _ <- notify(record)
    } yield record

  private def notify(record: ConnectionRecord) = {
    val result = for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      producer <- eventNotificationService.producer[ConnectionRecord]("Connect")
      _ <- producer.send(Event(connectionUpdatedEvent, record, walletId))
    } yield ()
    result.catchAll(e => ZIO.logError(s"Notification service error: $e"))
  }

  override def findRecordById(
      recordId: UUID
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    svc.findRecordById(recordId)

  override def findRecordByThreadId(
      thid: String
  ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
    svc.findRecordByThreadId(thid)

  override def deleteRecordById(recordId: UUID): URIO[WalletAccessContext, Unit] =
    svc.deleteRecordById(recordId)

  override def reportProcessingFailure(
      recordId: UUID,
      failReason: Option[org.hyperledger.identus.shared.models.Failure]
  ): URIO[WalletAccessContext, Unit] =
    svc.reportProcessingFailure(recordId, failReason)

  override def findAllRecords(): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    svc.findAllRecords()

  override def findRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[ConnectionRecord]] =
    svc.findRecordsByStates(ignoreWithZeroRetries, limit, states*)

  override def findRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): UIO[Seq[ConnectionRecord]] =
    svc.findRecordsByStatesForAllWallets(ignoreWithZeroRetries, limit, states*)
}

object ConnectionServiceNotifier {
  val layer: URLayer[ConnectionService & EventNotificationService, ConnectionService] =
    ZLayer.fromFunction(ConnectionServiceNotifier(_, _))
}
