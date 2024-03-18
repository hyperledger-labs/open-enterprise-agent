package io.iohk.atala.iam.oidc

import io.iohk.atala.api.http.{ErrorResponse, RequestContext}
import io.iohk.atala.castor.controller.http.DIDInput
import io.iohk.atala.castor.controller.http.DIDInput.didRefPathSegment
import io.iohk.atala.iam.authentication.oidc.JwtCredentials
import io.iohk.atala.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import io.iohk.atala.iam.oidc.http.{CredentialErrorResponse, CredentialRequest, CredentialResponse, NonceResponse}
import sttp.apispec.Tag
import sttp.model.StatusCode
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.{Endpoint, endpoint, extractFromRequest, oneOf, oneOfVariantValueMatcher, statusCode, stringToPath}

object CredentialIssuerEndpoints {

  private val tagName = "OIDC Credential Issuer"
  private val tagDescription =
    s"""
       |The __${tagName}__ is a service that issues credentials to users by implementing the [OIDC for Credential Issuance](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html) specification.
       |It exposes the following endpoints:
       |- Credential Endpoint
       |- Credential Issuer Metadata Endpoint
       |- Credential Offer Endpoint
       |""".stripMargin

  val tag = Tag(tagName, Some(tagDescription))

  type ExtendedErrorResponse = Either[ErrorResponse, CredentialErrorResponse]

  private val baseEndpoint = endpoint
    .tag(tagName)
    .securityIn(jwtAuthHeader)
    .in(extractFromRequest[RequestContext](RequestContext.apply))
    .in("oidc" / didRefPathSegment)

  val credentialEndpointErrorOutput = oneOf[Either[ErrorResponse, CredentialErrorResponse]](
    oneOfVariantValueMatcher(StatusCode.BadRequest, jsonBody[Either[ErrorResponse, CredentialErrorResponse]]) {
      case Right(CredentialErrorResponse(code, _, _, _)) if code.toHttpStatusCode == StatusCode.BadRequest => true
    },
    oneOfVariantValueMatcher(StatusCode.Unauthorized, jsonBody[Either[ErrorResponse, CredentialErrorResponse]]) {
      case Right(CredentialErrorResponse(code, _, _, _)) if code.toHttpStatusCode == StatusCode.Unauthorized => true
    },
    oneOfVariantValueMatcher(StatusCode.Forbidden, jsonBody[Either[ErrorResponse, CredentialErrorResponse]]) {
      case Right(CredentialErrorResponse(code, _, _, _)) if code.toHttpStatusCode == StatusCode.Forbidden => true
    },
    oneOfVariantValueMatcher(StatusCode.InternalServerError, jsonBody[Either[ErrorResponse, CredentialErrorResponse]]) {
      case Left(ErrorResponse(status, _, _, _, _)) if status == StatusCode.InternalServerError.code => true
    }
  )

  val credentialEndpoint: Endpoint[
    JwtCredentials,
    (RequestContext, String, CredentialRequest),
    ExtendedErrorResponse,
    CredentialResponse,
    Any
  ] = baseEndpoint.post
    .in("credential-issuer")
    .in(jsonBody[CredentialRequest])
    .out(
      statusCode(StatusCode.Ok).description("Credential issued successfully"),
    )
    .out(jsonBody[CredentialResponse])
    .errorOut(credentialEndpointErrorOutput)
    .name("issueCredential")
    .summary("Credential Endpoint")
    .description(
      """OIDC for VC [Credential Endpoint](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-endpoint)""".stripMargin
    )

  val nonceEndpoint: Endpoint[
    JwtCredentials,
    (RequestContext, String),
    ExtendedErrorResponse,
    NonceResponse,
    Any
  ] = baseEndpoint.get
    .in("nonce")
    .out(
      statusCode(StatusCode.Ok).description("Nonce issued successfully"),
    )
    .out(jsonBody[NonceResponse])
    .errorOut(credentialEndpointErrorOutput)
    .name("getNonce")
    .summary("Nonce Endpoint")
    .description(
      """The endpoint that returns a `nonce` value for the [Token Endpoint](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-nonce-endpoint)""".stripMargin
    )

}
