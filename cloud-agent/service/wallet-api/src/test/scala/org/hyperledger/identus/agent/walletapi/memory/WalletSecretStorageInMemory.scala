package org.hyperledger.identus.agent.walletapi.memory

import org.hyperledger.identus.agent.walletapi.model.WalletSeed
import org.hyperledger.identus.agent.walletapi.storage.WalletSecretStorage
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletId}
import zio.*

class WalletSecretStorageInMemory(storeRef: Ref[Map[WalletId, WalletSeed]]) extends WalletSecretStorage {

  override def setWalletSeed(seed: WalletSeed): URIO[WalletAccessContext, Unit] = {
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      _ <- storeRef.update(_.updated(walletId, seed))
    } yield ()
  }

  override def getWalletSeed: URIO[WalletAccessContext, Option[WalletSeed]] = {
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      seed <- storeRef.get.map(_.get(walletId))
    } yield seed
  }

}

object WalletSecretStorageInMemory {
  val layer: ULayer[WalletSecretStorage] =
    ZLayer.fromZIO(
      Ref
        .make(Map.empty[WalletId, WalletSeed])
        .map(WalletSecretStorageInMemory(_))
    )
}
