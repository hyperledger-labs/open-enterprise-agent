package org.hyperledger.identus.connect.core.repository

import org.hyperledger.identus.connect.core.model.ConnectionRecord
import org.hyperledger.identus.connect.core.model.ConnectionRecord.ProtocolState
import org.hyperledger.identus.mercury.protocol.connection.*
import org.hyperledger.identus.shared.models.WalletAccessContext
import zio.{UIO, URIO}

import java.util.UUID

trait ConnectionRepository {

  def create(
      record: ConnectionRecord
  ): URIO[WalletAccessContext, Unit]

  def findAll: URIO[WalletAccessContext, Seq[ConnectionRecord]]

  def findByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[ConnectionRecord]]

  def findByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): UIO[Seq[ConnectionRecord]]

  def findById(
      recordId: UUID
  ): URIO[WalletAccessContext, Option[ConnectionRecord]]

  def getById(
      recordId: UUID
  ): URIO[WalletAccessContext, ConnectionRecord]

  def deleteById(
      recordId: UUID
  ): URIO[WalletAccessContext, Unit]

  def findByThreadId(
      thid: String
  ): URIO[WalletAccessContext, Option[ConnectionRecord]]

  def updateWithConnectionRequest(
      recordId: UUID,
      request: ConnectionRequest,
      state: ProtocolState,
      maxRetries: Int,
  ): URIO[WalletAccessContext, Unit]

  def updateWithConnectionResponse(
      recordId: UUID,
      response: ConnectionResponse,
      state: ProtocolState,
      maxRetries: Int,
  ): URIO[WalletAccessContext, Unit]

  def updateProtocolState(
      recordId: UUID,
      from: ProtocolState,
      to: ProtocolState,
      maxRetries: Int,
  ): URIO[WalletAccessContext, Unit]

  def updateAfterFail(
      recordId: UUID,
      failReason: Option[org.hyperledger.identus.shared.models.Failure],
  ): URIO[WalletAccessContext, Unit]

}
