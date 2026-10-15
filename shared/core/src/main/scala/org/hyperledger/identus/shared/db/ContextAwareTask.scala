package org.hyperledger.identus.shared.db

import cats.data.NonEmptyList
import cats.effect.Async
import cats.Show
import doobie.*
import doobie.postgres.implicits.*
import doobie.syntax.ConnectionIOOps
import doobie.util.transactor.Transactor
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletId}
import org.postgresql.util.PGobject
import zio.*
import zio.interop.catz.*
import zio.json.{DecoderOps, EncoderOps}
import zio.json.ast.Json

import java.util.UUID

/** Distinguishes the wallet transactor from the system transactor in ZIO environments. The runtime representation is
  * Task, not a ZIO value mixed with a marker trait.
  */
opaque type ContextAwareTask[T] <: Task[T] = Task[T]

object ContextAwareTask {
  given Async[ContextAwareTask] = zio.interop.catz.asyncInstance[Any]
}

object Errors {
  final case class UnexpectedAffectedRow(count: Long) extends RuntimeException(s"Unexpected affected row count: $count")
}

object Implicits {

  private implicit val showPGobject: Show[PGobject] = Show.show(_.getValue.take(250))
  implicit val jsonPut: Put[Json] = Put.Advanced
    .other[PGobject](NonEmptyList.of("json"))
    .tcontramap[Json] { a =>
      val o = new PGobject
      o.setType("json")
      o.setValue(a.toJson)
      o
    }
  implicit val jsonGet: Get[Json] = Get.Advanced
    .other[PGobject](NonEmptyList.of("json"))
    .temap[Json](a => a.getValue.fromJson[Json])

  given walletIdGet: Get[WalletId] = Get[UUID].map(WalletId.fromUUID)
  given walletIdPut: Put[WalletId] = Put[UUID].contramap(_.toUUID)

  private val WalletIdVariableName = "app.current_wallet_id"

  extension [A](ma: ConnectionIO[A]) {
    def transactWithoutContext(xa: Transactor[ContextAwareTask]): Task[A] = {
      ConnectionIOOps(ma).transact(xa.asInstanceOf[Transactor[Task]])
    }

    def transactWallet(xa: Transactor[ContextAwareTask]): RIO[WalletAccessContext, A] = {
      def walletCxnIO(ctx: WalletAccessContext) = {
        for {
          _ <- doobie.free.connection.createStatement.map { statement =>
            statement.execute(s"SET LOCAL $WalletIdVariableName TO '${ctx.walletId}'")
          }
          result <- ma
        } yield result
      }

      for {
        ctx <- ZIO.service[WalletAccessContext]
        result <- ConnectionIOOps(walletCxnIO(ctx)).transact(xa.asInstanceOf[Transactor[Task]])
      } yield result
    }

  }

  extension [R, A: Numeric](ma: ZIO[R, Throwable, A]) {
    def ensureOneAffectedRowOrDie: URIO[R, Unit] = ma.flatMap {
      case 1     => ZIO.unit
      case count => ZIO.fail(Errors.UnexpectedAffectedRow(summon[Numeric[A]].toLong(count)))
    }.orDie
  }

}
