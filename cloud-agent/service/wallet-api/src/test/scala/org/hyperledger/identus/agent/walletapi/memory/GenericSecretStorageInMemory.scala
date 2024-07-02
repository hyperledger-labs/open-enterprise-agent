package org.hyperledger.identus.agent.walletapi.memory

import org.hyperledger.identus.agent.walletapi.storage.{GenericSecret, GenericSecretStorage}
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletId}
import zio.*
import zio.json.ast.Json

class GenericSecretStorageInMemory(walletRefs: Ref[Map[WalletId, Ref[Map[String, Json]]]])
    extends GenericSecretStorage {

  private def walletStoreRef: URIO[WalletAccessContext, Ref[Map[String, Json]]] =
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      refs <- walletRefs.get
      maybeWalletRef = refs.get(walletId)
      walletRef <- maybeWalletRef
        .fold {
          for {
            ref <- Ref.make(Map.empty[String, Json])
            _ <- walletRefs.set(refs.updated(walletId, ref))
          } yield ref
        }(ZIO.succeed)
    } yield walletRef

  override def set[K, V](key: K, secret: V)(implicit ev: GenericSecret[K, V]): RIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      _ <- storeRef.modify { store =>
        val keyPath = ev.keyPath(key)
        if (store.contains(keyPath))
          (
            ZIO.fail(Exception(s"Unique constaint violation, key $key already exists.")),
            store
          )
        else (ZIO.unit, store.updated(keyPath, ev.encodeValue(secret)))
      }.flatten
    } yield ()
  }

  override def get[K, V](key: K)(implicit ev: GenericSecret[K, V]): RIO[WalletAccessContext, Option[V]] = {
    val keyPath = ev.keyPath(key)
    for {
      storeRef <- walletStoreRef
      json <- storeRef.get.map(_.get(keyPath))
      result <- json.fold(ZIO.none)(json => ZIO.fromTry(ev.decodeValue(json)).asSome)
    } yield result
  }

}

object GenericSecretStorageInMemory {
  val layer: ULayer[GenericSecretStorage] =
    ZLayer.fromZIO(
      Ref
        .make(Map.empty[WalletId, Ref[Map[String, Json]]])
        .map(GenericSecretStorageInMemory(_))
    )
}
