package io.iohk.atala.shared.db

import doobie.*
import doobie.postgres.implicits.*
import doobie.syntax.ConnectionIOOps
import doobie.util.transactor.Transactor
import io.iohk.atala.shared.models.WalletAccessContext
import io.iohk.atala.shared.models.WalletId
import java.util.UUID
import zio.*
import zio.interop.catz.*

trait ContextAware
type ContextAwareTask[T] = Task[T] with ContextAware

object Implicits {

  given walletIdGet: Get[WalletId] = Get[UUID].map(WalletId.fromUUID)
  given walletIdPut: Put[WalletId] = Put[UUID].contramap(_.toUUID)

  // given logHandler: LogHandler = LogHandler.jdkLogHandler

  private val WalletIdVariableName = "app.current_wallet_id"

  extension [A](ma: ConnectionIO[A]) {
    def transact(xa: Transactor[ContextAwareTask]): Task[A] = {
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
        _ <- ZIO.debug {
          s"""
          | ##### WalletAccessContext #####
          | ${ctx.toString()}
          """.stripMargin
        }
        result <- ConnectionIOOps(walletCxnIO(ctx)).transact(xa.asInstanceOf[Transactor[Task]])
      } yield result
    }.debug("transactWallet")
  }

}
