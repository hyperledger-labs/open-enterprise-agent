package org.hyperledger.identus.connect.core.service

import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError
import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError.*
import org.hyperledger.identus.connect.core.model.ConnectionRecord
import org.hyperledger.identus.mercury.model.DidId
import org.hyperledger.identus.mercury.protocol.connection.{ConnectionRequest, ConnectionResponse}
import org.hyperledger.identus.shared.models.WalletAccessContext
import zio.*

import java.time.Duration
import java.util.UUID

trait ConnectionService {

  def createConnectionInvitation(
      label: Option[String],
      goalCode: Option[String],
      goal: Option[String],
      pairwiseDID: DidId
  ): ZIO[WalletAccessContext, UserInputValidationError, ConnectionRecord]

  def receiveConnectionInvitation(
      invitation: String
  ): ZIO[WalletAccessContext, InvitationParsingError | InvitationAlreadyReceived, ConnectionRecord]

  def acceptConnectionInvitation(
      recordId: UUID,
      pairwiseDid: DidId
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]

  def markConnectionRequestSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]

  def receiveConnectionRequest(
      request: ConnectionRequest,
      expirationTime: Option[Duration]
  ): ZIO[WalletAccessContext, ThreadIdNotFound | InvalidStateForOperation | InvitationExpired, ConnectionRecord]

  def acceptConnectionRequest(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]

  def markConnectionResponseSent(
      recordId: UUID
  ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]

  def markConnectionInvitationExpired(
      recordId: UUID
  ): URIO[WalletAccessContext, ConnectionRecord]

  def receiveConnectionResponse(
      response: ConnectionResponse
  ): ZIO[
    WalletAccessContext,
    ThreadIdMissingInReceivedMessage | ThreadIdNotFound | InvalidStateForOperation,
    ConnectionRecord
  ]

  def findAllRecords(): URIO[WalletAccessContext, Seq[ConnectionRecord]]

  def findRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[ConnectionRecord]]

  def findRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): UIO[Seq[ConnectionRecord]]

  def findRecordById(
      recordId: UUID
  ): URIO[WalletAccessContext, Option[ConnectionRecord]]

  def findRecordByThreadId(
      thid: String
  ): URIO[WalletAccessContext, Option[ConnectionRecord]]

  def deleteRecordById(
      recordId: UUID
  ): URIO[WalletAccessContext, Unit]

  def reportProcessingFailure(
      recordId: UUID,
      failReason: Option[org.hyperledger.identus.shared.models.Failure]
  ): URIO[WalletAccessContext, Unit]
}
