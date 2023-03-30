package io.iohk.atala.agent.server.http.model

import akka.http.scaladsl.server.StandardRoute
import io.iohk.atala.agent.openapi.model.ErrorResponse
import io.iohk.atala.agent.walletapi.model.error.{
  CreateManagedDIDError,
  GetManagedDIDError,
  PublishManagedDIDError,
  UpdateManagedDIDError
}
import io.iohk.atala.castor.core.model.did.w3c.DIDResolutionErrorRepr
import io.iohk.atala.castor.core.model.error.DIDOperationError
import io.iohk.atala.castor.core.model.error.OperationValidationError
import io.iohk.atala.castor.core.model.error.DIDResolutionError
import io.iohk.atala.connect.core.model.error.ConnectionServiceError
import io.iohk.atala.pollux.core.model.error.CredentialServiceError
import io.iohk.atala.pollux.core.model.error.PresentationError

import java.util.UUID

trait ToErrorResponse[E] {
  def toErrorResponse(e: E): ErrorResponse
}

// TODO: properly define error representation for error models
trait OASErrorModelHelper {

  extension [E](e: HttpServiceError[E]) {
    def toOAS(using te: ToErrorResponse[E]): ErrorResponse = {
      e match
        case e: HttpServiceError.InvalidPayload  => e.toOAS
        case HttpServiceError.DomainError(cause) => te.toErrorResponse(cause)
    }
  }

  extension (e: HttpServiceError.InvalidPayload) {
    def toOAS: ErrorResponse = {
      ErrorResponse(
        `type` = "InvalidPayload",
        title = "error-title",
        status = 422,
        detail = Some(e.msg),
        instance = "error-instance"
      )
    }
  }

  given ToErrorResponse[GetManagedDIDError] with {
    override def toErrorResponse(e: GetManagedDIDError): ErrorResponse = {
      ErrorResponse(
        `type` = "error-type",
        title = "error-title",
        status = 500,
        detail = Some(e.toString),
        instance = "error-instance"
      )
    }
  }

  given ToErrorResponse[PublishManagedDIDError] with {
    override def toErrorResponse(e: PublishManagedDIDError): ErrorResponse = {
      ErrorResponse(
        `type` = "error-type",
        title = "error-title",
        status = 500,
        detail = Some(e.toString),
        instance = "error-instance"
      )
    }
  }

  given ToErrorResponse[CreateManagedDIDError] with {
    override def toErrorResponse(e: CreateManagedDIDError): ErrorResponse = {
      val (status, detail) = e match {
        case CreateManagedDIDError.InvalidArgument(msg)    => (422, s"Unable to construct a DID operation: $msg")
        case CreateManagedDIDError.DIDAlreadyExists(did)   => (409, s"DID ${did.toString} already exists")
        case CreateManagedDIDError.KeyGenerationError(_)   => (500, s"Internal server error (key-pair generation)")
        case CreateManagedDIDError.WalletStorageError(_)   => (500, s"Internal server error (storage)")
        case CreateManagedDIDError.InvalidOperation(cause) => (422, s"Create DID payload is invalid: ${cause.toString}")
      }
      ErrorResponse(
        `type` = "error-type",
        title = "error-title",
        status = status,
        detail = Some(detail),
        instance = "error-instance"
      )
    }
  }

  given ToErrorResponse[UpdateManagedDIDError] with {
    override def toErrorResponse(e: UpdateManagedDIDError): ErrorResponse = {
      ErrorResponse(
        `type` = "error-type",
        title = "error-title",
        status = 500,
        detail = Some(e.toString),
        instance = "error-instance"
      )
    }
  }

  given ToErrorResponse[CredentialServiceError] with {
    def toErrorResponse(error: CredentialServiceError): ErrorResponse = {
      ErrorResponse(
        `type` = "error-type",
        title = "error-title",
        status = 500,
        detail = Some(error.toString),
        instance = "error-instance"
      )
    }
  }

  given ToErrorResponse[ConnectionServiceError] with {
    def toErrorResponse(error: ConnectionServiceError): ErrorResponse = {
      ErrorResponse(
        `type` = "error-type",
        title = "error-title",
        status = 500,
        detail = Some(error.toString),
        instance = "error-instance"
      )
    }
  }

  given ToErrorResponse[PresentationError] with {
    def toErrorResponse(error: PresentationError): ErrorResponse = {
      ErrorResponse(
        `type` = "error-type",
        title = "error-title",
        status = 500,
        detail = Some(error.toString),
        instance = "error-instance"
      )
    }
  }

  def notFoundErrorResponse(detail: Option[String] = None) = ErrorResponse(
    `type` = "not-found",
    title = "Resource not found",
    status = 404,
    detail = detail,
    instance = UUID.randomUUID().toString
  )

  def badRequestErrorResponse(detail: Option[String] = None) = ErrorResponse(
    `type` = "bad-request",
    title = "Bad request",
    status = 400,
    detail = detail,
    instance = UUID.randomUUID().toString
  )

}
