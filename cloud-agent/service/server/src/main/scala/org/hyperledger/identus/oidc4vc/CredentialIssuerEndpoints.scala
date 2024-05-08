package org.hyperledger.identus.oidc4vc

import org.hyperledger.identus.api.http.{EndpointOutputs, ErrorResponse, RequestContext}
import org.hyperledger.identus.castor.controller.http.DIDInput
import org.hyperledger.identus.castor.controller.http.DIDInput.didRefPathSegment
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyCredentials
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyEndpointSecurityLogic.apiKeyHeader
import org.hyperledger.identus.iam.authentication.oidc.JwtCredentials
import org.hyperledger.identus.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import org.hyperledger.identus.oidc4vc.http.*
import sttp.apispec.Tag
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody

import java.util.UUID

object CredentialIssuerEndpoints {

  private val tagName = "OpenID for Verifiable Credential"
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

  private val issuerIdPathSegment = path[UUID]("issuerId")
    .description("An issuer identifier in the oidc4vc protocol")
    .example(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))

  private val credentialConfigIdSegment = path[String]("credentialConfigId")
    .description("An identifier for the credential configuration")
    .example("UniversityDegree")

  private val baseEndpoint = endpoint
    .tag(tagName)
    .in(extractFromRequest[RequestContext](RequestContext.apply))
    .in("oidc4vc" / "issuers")

  private val baseIssuerPrivateEndpoint = baseEndpoint
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
    .in(didRefPathSegment / "credentials")
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

  val createCredentialOfferEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String, CredentialOfferRequest),
    ErrorResponse,
    CredentialOfferResponse,
    Any
  ] = baseIssuerPrivateEndpoint.post
    .in(didRefPathSegment / "credential-offers")
    .in(jsonBody[CredentialOfferRequest])
    .out(
      statusCode(StatusCode.Created).description("CredentialOffer created successfully"),
    )
    .out(jsonBody[CredentialOfferResponse])
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .name("createCredentialOffer")
    .summary("Create a new credential offer")
    .description(
      """Create a new credential offer and return a compliant `CredentialOffer` for the holder's
        |[Credential Offer Endpoint](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-offer-endpoint).""".stripMargin
    )

  val nonceEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, NonceRequest),
    ErrorResponse,
    NonceResponse,
    Any
  ] = baseIssuerPrivateEndpoint.post
    .in(issuerIdPathSegment / "nonces")
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

  val createCredentialIssuerEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, CreateCredentialIssuerRequest),
    ErrorResponse,
    CredentialIssuer,
    Any
  ] = baseIssuerPrivateEndpoint.post
    .in(jsonBody[CreateCredentialIssuerRequest])
    .out(
      statusCode(StatusCode.Created).description("Credential issuer created successfully")
    )
    .out(jsonBody[CredentialIssuer])
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .name("createCredentialIssuer")
    .summary("Create a new  credential issuer")

  val getCredentialIssuersEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    RequestContext,
    ErrorResponse,
    CredentialIssuerPage,
    Any
  ] = baseIssuerPrivateEndpoint.get
    .errorOut(EndpointOutputs.basicFailuresAndForbidden)
    .out(statusCode(StatusCode.Ok).description("List the credential issuers"))
    .out(jsonBody[CredentialIssuerPage])
    .name("getCredentialIssuers")
    .summary("List all credential issuers")

  val updateCredentialIssuerEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, PatchCredentialIssuerRequest),
    ErrorResponse,
    CredentialIssuer,
    Any
  ] = baseIssuerPrivateEndpoint.patch
    .in(issuerIdPathSegment)
    .in(jsonBody[PatchCredentialIssuerRequest])
    .out(
      statusCode(StatusCode.Ok).description("Credential issuer updated successfully")
    )
    .out(jsonBody[CredentialIssuer])
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .name("updateCredentialIssuer")
    .summary("Update the credential issuer")

  val deleteCredentialIssuerEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID),
    ErrorResponse,
    Unit,
    Any
  ] = baseIssuerPrivateEndpoint.delete
    .in(issuerIdPathSegment)
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .out(statusCode(StatusCode.Ok).description("Credential issuer deleted successfully"))
    .name("deleteCredentialIssuer")
    .summary("Delete the credential issuer")

  val createCredentialConfigurationEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, CreateCredentialConfigurationRequest),
    ErrorResponse,
    CredentialConfiguration,
    Any
  ] = baseIssuerPrivateEndpoint.post
    .in(issuerIdPathSegment / "credential-configurations")
    .in(jsonBody[CreateCredentialConfigurationRequest])
    .out(
      statusCode(StatusCode.Created).description("Credential configuration created successfully")
    )
    .out(jsonBody[CredentialConfiguration])
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .name("createCredentialConfiguration")
    .summary("Create a new  credential configuration")
    .description(
      """Create a new credential configuration for the issuer.
        |It represents the configuration of the credential that can be issued by the issuer.
        |This credential configuration object will be displayed in the OIDC4VC credential issuer metadata.""".stripMargin
    )

  val getCredentialConfigurationEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, String),
    ErrorResponse,
    CredentialConfiguration,
    Any
  ] = baseIssuerPrivateEndpoint.get
    .in(issuerIdPathSegment / "credential-configurations" / credentialConfigIdSegment)
    .out(
      statusCode(StatusCode.Ok).description("Get credential configuration successfully")
    )
    .out(jsonBody[CredentialConfiguration])
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .name("getCredentialConfiguration")
    .summary("Get the credential configuration")

  val deleteCredentialConfigurationEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, String),
    ErrorResponse,
    Unit,
    Any
  ] = baseIssuerPrivateEndpoint.delete
    .in(issuerIdPathSegment / "credential-configurations" / credentialConfigIdSegment)
    .out(
      statusCode(StatusCode.Ok).description("Credential configuration deleted successfully")
    )
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .name("deleteCredentialConfiguration")
    .summary("Delete the credential configuration")

  val issuerMetadataEndpoint: Endpoint[
    Unit,
    (RequestContext, UUID),
    ErrorResponse,
    IssuerMetadata,
    Any
  ] = baseEndpoint.get
    .in(issuerIdPathSegment / ".well-known" / "openid-credential-issuer")
    .out(
      statusCode(StatusCode.Ok).description("Issuer Metadata successfully retrieved")
    )
    .out(jsonBody[IssuerMetadata])
    .errorOut(EndpointOutputs.basicFailuresAndNotFound)
    .name("getIssuerMetadata")
    .summary("Get oidc4vc credential issuer metadata")

}
