package io.iohk.atala.iam.oidc

import io.iohk.atala.api.http.{EndpointOutputs, ErrorResponse, RequestContext}
import io.iohk.atala.castor.controller.http.DIDInput
import io.iohk.atala.castor.controller.http.DIDInput.didRefPathSegment
import io.iohk.atala.iam.authentication.apikey.ApiKeyCredentials
import io.iohk.atala.iam.authentication.apikey.ApiKeyEndpointSecurityLogic.apiKeyHeader
import io.iohk.atala.iam.authentication.oidc.JwtCredentials
import io.iohk.atala.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import io.iohk.atala.iam.oidc.http.*
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
    .in(extractFromRequest[RequestContext](RequestContext.apply))
    .in("oidc4vc" / didRefPathSegment)

  private val baseIssuerFacingEndpoint = baseEndpoint
    .securityIn(apiKeyHeader)
    .securityIn(jwtAuthHeader)

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
    .in("credentials")
    .in(jsonBody[CredentialRequest])
    .securityIn(jwtAuthHeader)
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

  // TODO: implement
  val createCredentialOfferEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String, CredentialOfferRequest),
    ErrorResponse,
    CredentialOfferResponse,
    Any
  ] = baseIssuerFacingEndpoint.post
    .in("credential-offers")
    .in(jsonBody[CredentialOfferRequest])
    .out(
      statusCode(StatusCode.Created).description("CredentialOffer created successfully"),
    )
    .out(jsonBody[CredentialOfferResponse])
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)

  val nonceEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String, NonceRequest),
    ErrorResponse,
    NonceResponse,
    Any
  ] = baseIssuerFacingEndpoint.post
    .in("nonces")
    .in(jsonBody[NonceRequest])
    .out(
      statusCode(StatusCode.Ok).description("Nonce issued successfully"),
    )
    .out(jsonBody[NonceResponse])
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .name("getNonce")
    .summary("Nonce Endpoint")
    .description(
      """The endpoint that returns a `nonce` value for the [Token Endpoint](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-nonce-endpoint)""".stripMargin
    )

  val issuerMetadataEndpoint: Endpoint[
    Unit,
    (RequestContext, String),
    ErrorResponse,
    IssuerMetadata,
    Any
  ] = baseEndpoint.get
    .in(".well-known" / "openid-credential-issuer")
    .out(
      statusCode(StatusCode.Ok).description("Issuer Metadata successfully retrieved")
    )
    .out(jsonBody[IssuerMetadata])
    .errorOut(EndpointOutputs.basicFailuresAndNotFound)
    .name("getIssuerMetadata") // TODO: add endpoint documentation

}
