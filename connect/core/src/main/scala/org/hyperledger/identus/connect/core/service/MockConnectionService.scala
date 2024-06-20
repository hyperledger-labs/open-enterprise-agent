package org.hyperledger.identus.connect.core.service

import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError
import org.hyperledger.identus.connect.core.model.error.ConnectionServiceError.*
import org.hyperledger.identus.connect.core.model.ConnectionRecord
import org.hyperledger.identus.mercury.model.DidId
import org.hyperledger.identus.mercury.protocol.connection.{ConnectionRequest, ConnectionResponse}
import org.hyperledger.identus.shared.models.WalletAccessContext
import zio.{mock, UIO, URIO, URLayer, ZIO, ZLayer}
import zio.mock.{Mock, Proxy}

import java.time.Duration
import java.util.UUID

object MockConnectionService extends Mock[ConnectionService] {

  object CreateConnectionInvitation
      extends Effect[(Option[String], Option[String], Option[String], DidId), Nothing, ConnectionRecord]
  object ReceiveConnectionInvitation
      extends Effect[String, InvitationParsingError | InvitationAlreadyReceived, ConnectionRecord]
  object AcceptConnectionInvitation
      extends Effect[(UUID, DidId), RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]
  object MarkConnectionRequestSent extends Effect[UUID, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]
  object ReceiveConnectionRequest
      extends Effect[
        ConnectionRequest,
        ThreadIdNotFound | InvalidStateForOperation | InvitationExpired,
        ConnectionRecord
      ]
  object AcceptConnectionRequest extends Effect[UUID, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]
  object MarkConnectionResponseSent extends Effect[UUID, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord]
  object MarkConnectionInvitationExpired extends Effect[UUID, Nothing, ConnectionRecord]
  object FindById extends Effect[UUID, Nothing, Option[ConnectionRecord]]

  object ReceiveConnectionResponse
      extends Effect[
        ConnectionResponse,
        ThreadIdMissingInReceivedMessage | ThreadIdNotFound | InvalidStateForOperation,
        ConnectionRecord
      ]

  override val compose: URLayer[mock.Proxy, ConnectionService] = ZLayer {
    for {
      proxy <- ZIO.service[Proxy]
    } yield new ConnectionService {
      override def createConnectionInvitation(
          label: Option[String],
          goalCode: Option[String],
          goal: Option[String],
          pairwiseDID: DidId
      ): ZIO[WalletAccessContext, UserInputValidationError, ConnectionRecord] =
        proxy(CreateConnectionInvitation, label, goalCode, goal, pairwiseDID)

      override def receiveConnectionInvitation(
          invitation: String
      ): ZIO[WalletAccessContext, InvitationParsingError | InvitationAlreadyReceived, ConnectionRecord] =
        proxy(ReceiveConnectionInvitation, invitation)

      override def acceptConnectionInvitation(
          recordId: UUID,
          pairwiseDid: DidId
      ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
        proxy(AcceptConnectionInvitation, recordId, pairwiseDid)

      override def markConnectionRequestSent(
          recordId: UUID
      ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
        proxy(MarkConnectionRequestSent, recordId)

      override def receiveConnectionRequest(
          request: ConnectionRequest,
          expirationTime: Option[Duration]
      ): ZIO[WalletAccessContext, ThreadIdNotFound | InvalidStateForOperation | InvitationExpired, ConnectionRecord] =
        proxy(ReceiveConnectionRequest, request)

      override def acceptConnectionRequest(
          recordId: UUID
      ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
        proxy(AcceptConnectionRequest, recordId)

      override def markConnectionInvitationExpired(
          recordId: UUID
      ): URIO[WalletAccessContext, ConnectionRecord] =
        proxy(MarkConnectionInvitationExpired, recordId)

      override def markConnectionResponseSent(
          recordId: UUID
      ): ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
        proxy(MarkConnectionResponseSent, recordId)

      override def receiveConnectionResponse(
          response: ConnectionResponse
      ): ZIO[
        WalletAccessContext,
        ThreadIdMissingInReceivedMessage | ThreadIdNotFound | InvalidStateForOperation,
        ConnectionRecord
      ] =
        proxy(ReceiveConnectionResponse, response)

      override def findAllRecords(): URIO[WalletAccessContext, Seq[ConnectionRecord]] = ???

      override def findRecordsByStates(
          ignoreWithZeroRetries: Boolean,
          limit: Int,
          states: ConnectionRecord.ProtocolState*
      ): URIO[WalletAccessContext, Seq[ConnectionRecord]] = ???

      override def findRecordsByStatesForAllWallets(
          ignoreWithZeroRetries: Boolean,
          limit: Int,
          states: ConnectionRecord.ProtocolState*
      ): UIO[Seq[ConnectionRecord]] = ???

      override def findRecordById(
          recordId: UUID
      ): URIO[WalletAccessContext, Option[ConnectionRecord]] = proxy(FindById, recordId)

      override def findRecordByThreadId(
          thid: String
      ): URIO[WalletAccessContext, Option[ConnectionRecord]] =
        ???

      override def deleteRecordById(
          recordId: UUID
      ): URIO[WalletAccessContext, Unit] = ???

      override def reportProcessingFailure(
          recordId: UUID,
          failReason: Option[org.hyperledger.identus.shared.models.Failure]
      ): URIO[WalletAccessContext, Unit] = ???
    }
  }
}
