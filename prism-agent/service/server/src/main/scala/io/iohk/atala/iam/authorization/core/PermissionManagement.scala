package io.iohk.atala.iam.authorization.core

import io.iohk.atala.agent.walletapi.model.BaseEntity
import io.iohk.atala.agent.walletapi.model.error.EntityServiceError
import io.iohk.atala.agent.walletapi.model.error.EntityServiceError.EntityAlreadyExists
import io.iohk.atala.agent.walletapi.model.error.EntityServiceError.EntityNotFound
import io.iohk.atala.agent.walletapi.model.error.EntityServiceError.EntityStorageError
import io.iohk.atala.agent.walletapi.model.error.EntityServiceError.EntityWalletNotFound
import io.iohk.atala.shared.models.WalletId
import zio.IO

import java.util.UUID

object PermissionManagement {
  trait Service[E <: BaseEntity] {
    def grantWalletToUser(walletId: WalletId, entity: E): IO[Error, Unit]
    def revokeWalletFromUser(walletId: WalletId, entity: E): IO[Error, Unit]
  }

  sealed trait Error(val message: String)

  object Error {
    case class UserNotFoundById(userId: UUID, cause: Option[Throwable] = None)
        extends Error(s"User $userId is not found" + cause.map(t => s" Cause: ${t.getMessage}"))
    case class WalletNotFoundByUserId(userId: UUID) extends Error(s"Wallet for user $userId is not found")

    case class WalletNotFoundById(walletId: WalletId) extends Error(s"Wallet not found by ${walletId.toUUID}")

    case class WalletResourceNotFoundById(walletId: WalletId)
        extends Error(s"Wallet resource not found by ${walletId.toUUID}")

    case class PermissionNotFoundById(userId: UUID, walletId: WalletId, walletResourceId: String)
        extends Error(
          s"Permission not found by userId: $userId, walletId: ${walletId.toUUID}, walletResourceId: $walletResourceId"
        )

    case class UnexpectedError(cause: Throwable) extends Error(cause.getMessage)

    case class ServiceError(cause: String) extends Error(cause)

    given Conversion[EntityServiceError, Error] = {
      case e: EntityNotFound       => UserNotFoundById(e.id)
      case e: EntityAlreadyExists  => UnexpectedError(Exception(s"Entity with id ${e.id} already exists."))
      case e: EntityStorageError   => UnexpectedError(Exception(s"Entity storage error: ${e.message}"))
      case e: EntityWalletNotFound => WalletNotFoundById(WalletId.fromUUID(e.walletId))
    }
  }
}