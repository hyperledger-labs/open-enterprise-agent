package io.iohk.atala.agent.walletapi.storage

import io.iohk.atala.agent.walletapi.model.*
import io.iohk.atala.castor.core.model.did.{PrismDID, ScheduledDIDOperationStatus}
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.shared.models.{WalletAccessContext, WalletId}
import zio.mock.{Expectation, Mock, Proxy}
import zio.test.Assertion.equalTo
import zio.{RIO, Task, URLayer, ZIO, ZLayer, mock}

import scala.collection.immutable.ArraySeq

case class MockDIDNonSecretStorage(proxy: Proxy) extends DIDNonSecretStorage {
  override def insertDIDUpdateLineage(did: PrismDID, updateLineage: DIDUpdateLineage): RIO[WalletAccessContext, Unit] =
    proxy(MockDIDNonSecretStorage.InsertDIDUpdateLineage, (did, updateLineage))

  override def listUpdateLineage(
      did: Option[PrismDID],
      status: Option[ScheduledDIDOperationStatus]
  ): RIO[WalletAccessContext, Seq[DIDUpdateLineage]] = proxy(MockDIDNonSecretStorage.ListUpdateLineage, (did, status))

  override def setDIDUpdateLineageStatus(
      operationId: Array[Byte],
      status: ScheduledDIDOperationStatus
  ): RIO[WalletAccessContext, Unit] = proxy(MockDIDNonSecretStorage.SetDIDUpdateLineageStatus, (operationId, status))

  override def createPeerDIDRecord(did: DidId): RIO[WalletAccessContext, Int] =
    proxy(MockDIDNonSecretStorage.CreatePeerDIDRecord, did)

  override def getPeerDIDRecord(did: DidId): Task[Option[PeerDIDRecord]] =
    proxy(MockDIDNonSecretStorage.GetPeerDIDRecord, did)

  override def getPrismDidWalletId(prismDid: PrismDID): Task[Option[WalletId]] =
    proxy(MockDIDNonSecretStorage.GetPrismDidWalletId, prismDid)

  override def getManagedDIDState(did: PrismDID): RIO[WalletAccessContext, Option[ManagedDIDState]] =
    proxy(MockDIDNonSecretStorage.GetManagedDIDState, did)

  override def insertManagedDID(
      did: PrismDID,
      state: ManagedDIDState,
      hdKey: Map[String, ManagedDIDHdKeyPath]
  ): RIO[WalletAccessContext, Unit] = proxy(MockDIDNonSecretStorage.InsertManagedDID, (did, state, hdKey))

  override def updateManagedDID(did: PrismDID, patch: ManagedDIDStatePatch): RIO[WalletAccessContext, Unit] =
    proxy(MockDIDNonSecretStorage.UpdateManagedDID, (did, patch))

  override def getMaxDIDIndex(): RIO[WalletAccessContext, Option[Int]] =
    proxy(MockDIDNonSecretStorage.GetMaxDIDIndex)

  override def getHdKeyCounter(did: PrismDID): RIO[WalletAccessContext, Option[HdKeyIndexCounter]] =
    proxy(MockDIDNonSecretStorage.GetHdKeyCounter, did)

  override def getHdKeyPath(did: PrismDID, keyId: String): RIO[WalletAccessContext, Option[ManagedDIDHdKeyPath]] =
    proxy(MockDIDNonSecretStorage.GetHdKeyPath, (did, keyId))

  override def insertHdKeyPath(
      did: PrismDID,
      keyId: String,
      hdKeyPath: ManagedDIDHdKeyPath,
      operationHash: Array[Byte]
  ): RIO[WalletAccessContext, Unit] =
    proxy(MockDIDNonSecretStorage.InsertHdKeyPath, (did, keyId, hdKeyPath, operationHash))

  override def listHdKeyPath(
      did: PrismDID
  ): RIO[WalletAccessContext, Seq[(String, ArraySeq[Byte], ManagedDIDHdKeyPath)]] =
    proxy(MockDIDNonSecretStorage.ListHdKeyPath, did)

  override def listManagedDID(
      offset: Option[Int],
      limit: Option[Int]
  ): RIO[WalletAccessContext, (Seq[(PrismDID, ManagedDIDState)], Int)] =
    proxy(MockDIDNonSecretStorage.ListManagedDID, (offset, limit))
}
object MockDIDNonSecretStorage extends Mock[DIDNonSecretStorage] {
  object InsertDIDUpdateLineage extends Effect[(PrismDID, DIDUpdateLineage), Throwable, Unit]
  object ListUpdateLineage
      extends Effect[(Option[PrismDID], Option[ScheduledDIDOperationStatus]), Throwable, Seq[DIDUpdateLineage]]
  object SetDIDUpdateLineageStatus extends Effect[(Array[Byte], ScheduledDIDOperationStatus), Throwable, Unit]
  object CreatePeerDIDRecord extends Effect[DidId, Throwable, Int]
  object GetPeerDIDRecord extends Effect[DidId, Throwable, Option[PeerDIDRecord]]
  object GetPrismDidWalletId extends Effect[PrismDID, Throwable, Option[WalletId]]
  object GetManagedDIDState extends Effect[PrismDID, Throwable, Option[ManagedDIDState]]
  object InsertManagedDID extends Effect[(PrismDID, ManagedDIDState, Map[String, ManagedDIDHdKeyPath]), Throwable, Unit]
  object UpdateManagedDID extends Effect[(PrismDID, ManagedDIDStatePatch), Throwable, Unit]
  object GetMaxDIDIndex extends Effect[Unit, Throwable, Option[Int]]
  object GetHdKeyCounter extends Effect[PrismDID, Throwable, Option[HdKeyIndexCounter]]
  object GetHdKeyPath extends Effect[(PrismDID, String), Throwable, Option[ManagedDIDHdKeyPath]]
  object InsertHdKeyPath extends Effect[(PrismDID, String, ManagedDIDHdKeyPath, Array[Byte]), Throwable, Unit]
  object ListHdKeyPath extends Effect[PrismDID, Throwable, Seq[(String, ArraySeq[Byte], ManagedDIDHdKeyPath)]]
  object ListManagedDID extends Effect[(Option[Int], Option[Int]), Throwable, (Seq[(PrismDID, ManagedDIDState)], Int)]

  override val compose: URLayer[mock.Proxy, DIDNonSecretStorage] = ZLayer.fromFunction(MockDIDNonSecretStorage(_))

  def getPrismDidWalletIdExpectation(prismDID: PrismDID, walletId: WalletId): Expectation[DIDNonSecretStorage] =
    MockDIDNonSecretStorage.GetPrismDidWalletId(
      assertion = equalTo(prismDID),
      result = Expectation.value(Some(walletId))
    )
}
