package io.iohk.atala.agent.walletapi.sql

import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import io.iohk.atala.agent.walletapi.storage.WalletNonSecretStorage
import io.iohk.atala.shared.db.Implicits.*
import io.iohk.atala.shared.db.ContextfulTask
import io.iohk.atala.shared.models.WalletId
import java.time.Instant
import zio.*

class JdbcWalletNonSecretStorage(xa: Transactor[ContextfulTask]) extends WalletNonSecretStorage {

  override def createWallet: Task[WalletId] = {
    val cxnIO = (walletId: WalletId, now: Instant) => sql"""
        | INSERT INTO public.wallet(wallet_id, created_at)
        | VALUES ($walletId, $now)
        """.stripMargin.update

    for {
      now <- Clock.instant
      walletId = WalletId.random
      _ <- cxnIO(walletId, now).run.transactAny(xa)
    } yield walletId
  }

  override def listWallet: Task[Seq[WalletId]] = {
    val cxnIO =
      sql"""
           | SELECT wallet_id
           | FROM public.wallet
           """.stripMargin
        .query[WalletId]
        .to[List]

    cxnIO.transactAny(xa)
  }

}

object JdbcWalletNonSecretStorage {
  val layer: URLayer[Transactor[ContextfulTask], WalletNonSecretStorage] =
    ZLayer.fromFunction(new JdbcWalletNonSecretStorage(_))
}
