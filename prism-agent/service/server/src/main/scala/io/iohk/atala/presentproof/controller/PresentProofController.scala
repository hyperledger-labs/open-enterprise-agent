package io.iohk.atala.presentproof.controller

import io.iohk.atala.api.http.model.PaginationInput
import io.iohk.atala.api.http.{ErrorResponse, RequestContext}
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.presentproof.controller.http.*
import io.iohk.atala.shared.models.WalletAccessContext
import zio.ZIO

import java.util.UUID
import scala.util.Try

trait PresentProofController {
  def requestPresentation(
      requestPresentationInput: RequestPresentationInput
  )(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatus]

  def getPresentations(
      paginationInput: PaginationInput,
      thid: Option[String]
  )(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatusPage]

  def getPresentation(id: UUID)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatus]

  def updatePresentation(id: UUID, requestPresentationAction: RequestPresentationAction)(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatus]

  def createOOBRequestPresentation(
      request: OOBRequestPresentation
  )(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, OOBPresentation]

  def acceptRequestPresentationInvitation(
      request: AcceptRequestPresentationInvitationRequest
  )(implicit
      rc: RequestContext
  ): ZIO[WalletAccessContext, ErrorResponse, PresentationStatus]
}

object PresentProofController {
  def toHttpError(error: PresentationError): ErrorResponse =
    error match
      case PresentationError.RepositoryError(cause) =>
        ErrorResponse.internalServerError(title = "RepositoryError", detail = Some(cause.toString))
      case PresentationError.RecordIdNotFound(recordId) =>
        ErrorResponse.notFound(detail = Some(s"Record Id not found: $recordId"))
      case PresentationError.ThreadIdNotFound(thid) =>
        ErrorResponse.notFound(detail = Some(s"Thread Id not found: $thid"))
      case PresentationError.InvalidFlowStateError(msg) =>
        ErrorResponse.badRequest(title = "InvalidFlowState", detail = Some(msg))
      case PresentationError.UnexpectedError(msg) =>
        ErrorResponse.internalServerError(detail = Some(msg))
      case PresentationError.IssuedCredentialNotFoundError(_) =>
        ErrorResponse.internalServerError(detail = Some("Issued credential not found"))
      case PresentationError.PresentationDecodingError(_) =>
        ErrorResponse.internalServerError(detail = Some("Presentation decoding error"))
      case PresentationError.PresentationNotFoundError(_) =>
        ErrorResponse.notFound(detail = Some("Presentation no found"))
      case PresentationError.HolderBindingError(msg) =>
        ErrorResponse.internalServerError(detail = Some(s"Holder binding error: $msg"))
      case PresentationError.MissingCredential =>
        ErrorResponse.badRequest(
          title = "MissingCredential",
          detail = Some("The Credential is missing from attachments")
        )
      case PresentationError.MissingCredentialFormat =>
        ErrorResponse.badRequest(
          title = "MissingCredentialFormat",
          detail = Some("The Credential format is missing from the credential in attachment")
        )
      case PresentationError.UnsupportedCredentialFormat(format) =>
        ErrorResponse.badRequest(
          title = "UnsupportedCredentialFormat",
          detail = Some(s"The Credential format '$format' is not Unsupported")
        )

  def toDidCommID(str: String): ZIO[Any, ErrorResponse, io.iohk.atala.pollux.core.model.DidCommID] =
    ZIO
      .fromTry(Try(io.iohk.atala.pollux.core.model.DidCommID(str)))
      .mapError(e => ErrorResponse.badRequest(s"Error parsing string as DidCommID: ${e.getMessage}"))
}
