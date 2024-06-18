package org.hyperledger.identus.agent.walletapi.sql

import cats.data.NonEmptyList
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.transactor.Transactor
import org.hyperledger.identus.agent.walletapi.model.Wallet
import org.hyperledger.identus.agent.walletapi.storage.WalletNonSecretStorage
import org.hyperledger.identus.event.notification.EventNotificationConfig
import org.hyperledger.identus.shared.db.ContextAwareTask
import org.hyperledger.identus.shared.db.Implicits.{*, given}
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletId}
import zio.*

import java.net.URL
import java.time.Instant
import java.util.UUID

class JdbcWalletNonSecretStorage(xa: Transactor[ContextAwareTask]) extends WalletNonSecretStorage {

  override def createWallet(wallet: Wallet, seedDigest: Array[Byte]): UIO[Wallet] = {
    val cxnIO = (row: WalletRow) => sql"""
        | INSERT INTO public.wallet(
        |   wallet_id,
        |   name,
        |   created_at,
        |   updated_at,
        |   seed_digest
        | )
        | VALUES (
        |   ${row.id},
        |   ${row.name},
        |   ${row.createdAt},
        |   ${row.updatedAt},
        |   ${seedDigest}
        | )
        """.stripMargin.update

    val row = WalletRow.from(wallet)
    cxnIO(row).run
      .transactWithoutContext(xa)
      .as(wallet)
      .orDie
  }

  override def findWalletById(walletId: WalletId): UIO[Option[Wallet]] = {
    val cxnIO =
      sql"""
        | SELECT
        |   wallet_id,
        |   name,
        |   created_at,
        |   updated_at
        | FROM public.wallet
        | WHERE wallet_id = $walletId
        """.stripMargin
        .query[WalletRow]
        .option

    cxnIO
      .transactWithoutContext(xa)
      .map(_.map(_.toDomain))
      .orDie
  }

  override def findWalletBySeed(seedDigest: Array[Byte]): UIO[Option[Wallet]] = {
    val cxnIO =
      sql"""
           | SELECT
           |   wallet_id,
           |   name,
           |   created_at,
           |   updated_at
           | FROM public.wallet
           | WHERE seed_digest = $seedDigest
        """.stripMargin
        .query[WalletRow]
        .option

    cxnIO
      .transactWithoutContext(xa)
      .map(_.map(_.toDomain))
      .orDie
  }

  override def getWallets(walletIds: Seq[WalletId]): UIO[Seq[Wallet]] = {
    walletIds match
      case Nil => ZIO.succeed(Nil)
      case head +: tail =>
        val nel = NonEmptyList.of(head, tail*)
        val conditionFragment = Fragments.in(fr"wallet_id", nel)
        val cxnIO =
          sql"""
            | SELECT
            |   wallet_id,
            |   name,
            |   created_at,
            |   updated_at
            | FROM public.wallet
            | WHERE $conditionFragment
            """.stripMargin
            .query[WalletRow]
            .to[List]

        cxnIO
          .transactWithoutContext(xa)
          .map(_.map(_.toDomain))
          .orDie
  }

  override def listWallet(
      offset: Option[Int],
      limit: Option[Int]
  ): UIO[(Seq[Wallet], RuntimeFlags)] = {
    val countCxnIO =
      sql"""
        | SELECT COUNT(*)
        | FROM public.wallet
        """.stripMargin
        .query[Int]
        .unique

    val baseFr =
      sql"""
        | SELECT
        |   wallet_id,
        |   name,
        |   created_at,
        |   updated_at
        | FROM public.wallet
        | ORDER BY created_at
        """.stripMargin
    val withOffsetFr = offset.fold(baseFr)(offsetValue => baseFr ++ fr"OFFSET $offsetValue")
    val withOffsetAndLimitFr = limit.fold(withOffsetFr)(limitValue => withOffsetFr ++ fr"LIMIT $limitValue")
    val walletsCxnIO = withOffsetAndLimitFr.query[WalletRow].to[List]

    val effect = for {
      totalCount <- countCxnIO
      rows <- walletsCxnIO.map(_.map(_.toDomain))
    } yield (rows, totalCount)

    effect
      .transactWithoutContext(xa)
      .orDie
  }

  override def countWalletNotification: URIO[WalletAccessContext, Int] = {
    val countIO = sql"""
        | SELECT COUNT(*)
        | FROM public.wallet_notification
        """.stripMargin
      .query[Int]
      .unique

    countIO
      .transactWallet(xa)
      .orDie
  }

  override def createWalletNotification(
      config: EventNotificationConfig
  ): URIO[WalletAccessContext, Unit] = {
    val insertIO = (row: WalletNofiticationRow) => sql"""
        | INSERT INTO public.wallet_notification (
        |   id,
        |   wallet_id,
        |   url,
        |   custom_headers,
        |   created_at
        | ) VALUES (
        |   ${row.id},
        |   ${row.walletId},
        |   ${row.url},
        |   ${row.customHeaders},
        |   ${row.createdAt}
        | )
        """.stripMargin.update

    val row = WalletNofiticationRow.from(config)

    insertIO(row).run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

  override def walletNotification: URIO[WalletAccessContext, Seq[EventNotificationConfig]] = {
    val cxn =
      sql"""
        | SELECT
        |   id,
        |   wallet_id,
        |   url,
        |   custom_headers,
        |   created_at
        | FROM public.wallet_notification
        """.stripMargin
        .query[WalletNofiticationRow]
        .to[List]

    cxn
      .transactWallet(xa)
      .flatMap(rows => ZIO.foreach(rows) { row => ZIO.fromTry(row.toDomain) })
      .orDie
  }

  override def deleteWalletNotification(id: UUID): URIO[WalletAccessContext, Unit] = {
    val cxn =
      sql"""
        | DELETE FROM public.wallet_notification
        | WHERE id = $id
        """.stripMargin.update

    cxn.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }

}

object JdbcWalletNonSecretStorage {

  val layer: URLayer[Transactor[ContextAwareTask], WalletNonSecretStorage] =
    ZLayer.fromFunction(new JdbcWalletNonSecretStorage(_))

}
