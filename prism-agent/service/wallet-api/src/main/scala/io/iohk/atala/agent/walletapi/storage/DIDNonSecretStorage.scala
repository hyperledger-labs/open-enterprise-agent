package io.iohk.atala.agent.walletapi.storage

import io.iohk.atala.agent.walletapi.model.*
import io.iohk.atala.castor.core.model.did.{PrismDID, ScheduledDIDOperationStatus}
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.shared.models.{WalletAccessContext, WalletId}
import zio.*

import scala.collection.immutable.ArraySeq

trait DIDNonSecretStorage {

  def getManagedDIDState(did: PrismDID): RIO[WalletAccessContext, Option[ManagedDIDState]]

  def insertManagedDID(
      did: PrismDID,
      state: ManagedDIDState,
      hdKey: Map[String, ManagedDIDHdKeyPath]
  ): RIO[WalletAccessContext, Unit]

  def updateManagedDID(did: PrismDID, patch: ManagedDIDStatePatch): RIO[WalletAccessContext, Unit]

  def getMaxDIDIndex(): RIO[WalletAccessContext, Option[Int]]

  def getHdKeyCounter(did: PrismDID): RIO[WalletAccessContext, Option[HdKeyIndexCounter]]

  def getHdKeyPath(did: PrismDID, keyId: String): RIO[WalletAccessContext, Option[ManagedDIDHdKeyPath]]

  def insertHdKeyPath(
      did: PrismDID,
      keyId: String,
      hdKeyPath: ManagedDIDHdKeyPath,
      operationHash: Array[Byte]
  ): RIO[WalletAccessContext, Unit]

  def listHdKeyPath(did: PrismDID): RIO[WalletAccessContext, Seq[(String, ArraySeq[Byte], ManagedDIDHdKeyPath)]]

  /** Return a list of Managed DID as well as a count of all filtered items */
  def listManagedDID(
      offset: Option[Int],
      limit: Option[Int]
  ): RIO[WalletAccessContext, (Seq[(PrismDID, ManagedDIDState)], Int)]

  def insertDIDUpdateLineage(did: PrismDID, updateLineage: DIDUpdateLineage): RIO[WalletAccessContext, Unit]

  def listUpdateLineage(
      did: Option[PrismDID],
      status: Option[ScheduledDIDOperationStatus]
  ): RIO[WalletAccessContext, Seq[DIDUpdateLineage]]

  def setDIDUpdateLineageStatus(
      operationId: Array[Byte],
      status: ScheduledDIDOperationStatus
  ): RIO[WalletAccessContext, Unit]

  def createPeerDIDRecord(did: DidId): RIO[WalletAccessContext, Int]

  def getPeerDIDRecord(did: DidId): Task[Option[PeerDIDRecord]]

  def getPrismDidWalletId(prismDid: PrismDID): Task[Option[WalletId]]

}
