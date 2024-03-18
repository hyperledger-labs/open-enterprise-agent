package io.iohk.atala.iam.oidc.http

import io.iohk.atala.api.http.EndpointOutputs.statusCodeMatcher
import io.iohk.atala.api.http.ErrorResponse
import sttp.model.StatusCode
import sttp.tapir.Schema.annotations.encodedName
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.{Schema, oneOfVariantValueMatcher}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, jsonField}

// According to OIDC spec and RFC6750, the following errors are expected to be returned by the authorization server
// https://www.rfc-editor.org/rfc/rfc6750.html#section-3.1
object AuthorizationErrors {

  val invalidRequest = oneOfVariantValueMatcher(
    StatusCode.BadRequest,
    jsonBody[CredentialErrorResponse].description(
      """The request is missing a required parameter, includes an unsupported parameter or parameter value, repeats the same parameter, uses more than one method for including an access token, or is otherwise malformed.""".stripMargin
    )
  )(statusCodeMatcher(StatusCode.BadRequest))

  val invalidToken = oneOfVariantValueMatcher(
    StatusCode.Unauthorized,
    jsonBody[CredentialErrorResponse].description(
      "The access token provided is expired, revoked, malformed, or invalid for other reason"
    )
  )(statusCodeMatcher(StatusCode.Unauthorized))

  val insufficientScope = oneOfVariantValueMatcher(
    StatusCode.Forbidden,
    jsonBody[CredentialErrorResponse].description(
      "The request requires higher privileges than provided by the access token"
    )
  )(statusCodeMatcher(StatusCode.Forbidden))
}

case class CredentialErrorResponse(
    error: CredentialErrorCode,
    @jsonField("error_description")
    @encodedName("error_description")
    errorDescription: Option[String] = None,
    @jsonField("c_nonce")
    @encodedName("c_nonce")
    nonce: Option[String] = None,
    @jsonField("c_nonce_expires_in")
    @encodedName("c_nonce_expires_in")
    nonceExpiresIs: Option[Long] = None
)

object CredentialErrorResponse {
  given schema: Schema[CredentialErrorResponse] = Schema.derived
  given encoder: JsonEncoder[CredentialErrorResponse] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialErrorResponse] = DeriveJsonDecoder.gen
}

enum CredentialErrorCode {
  case invalid_request
  case invalid_token
  case insufficient_scope
  case invalid_credential_request
  case unsupported_credential_type
  case unsupported_credential_format
  case invalid_proof
  case invalid_encryption_parameters
}

object CredentialErrorCode {
  given schema: Schema[CredentialErrorCode] = Schema.derivedEnumeration.defaultStringBased
  given encoder: JsonEncoder[CredentialErrorCode] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialErrorCode] = DeriveJsonDecoder.gen

  implicit class CredentialErrorCodeOps(val credentialErrorCode: CredentialErrorCode) extends AnyVal {
    def toHttpStatusCode: StatusCode = CredentialErrorCode.toHttpStatusCode(credentialErrorCode)
  }
  val toHttpStatusCode: PartialFunction[CredentialErrorCode, StatusCode] = {
    case CredentialErrorCode.invalid_request               => StatusCode.BadRequest
    case CredentialErrorCode.invalid_token                 => StatusCode.Unauthorized
    case CredentialErrorCode.insufficient_scope            => StatusCode.Forbidden
    case CredentialErrorCode.invalid_credential_request    => StatusCode.BadRequest
    case CredentialErrorCode.unsupported_credential_type   => StatusCode.BadRequest
    case CredentialErrorCode.invalid_proof                 => StatusCode.BadRequest
    case CredentialErrorCode.invalid_encryption_parameters => StatusCode.BadRequest
  }
}

object CredentialRequestErrors {
  def errorCodeMatcher(
      credentialErrorCode: CredentialErrorCode
  ): PartialFunction[Any, Boolean] = {
    case CredentialErrorResponse(code, _, _, _) if code == credentialErrorCode => true
  }

  val badRequest = oneOfVariantValueMatcher(
    StatusCode.BadRequest,
    jsonBody[ErrorResponse].description(
      "The request is missing a required parameter, includes an unsupported parameter or parameter value, repeats the same parameter, or is otherwise malformed"
    )
  )(statusCodeMatcher(StatusCode.BadRequest))

}
