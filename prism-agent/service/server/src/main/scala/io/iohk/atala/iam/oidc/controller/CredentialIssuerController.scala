package io.iohk.atala.iam.oidc.controller

import io.iohk.atala.api.http.ErrorResponse.internalServerError
import io.iohk.atala.api.http.{ErrorResponse, RequestContext}
import io.iohk.atala.castor.core.model.did.{CanonicalPrismDID, PrismDID}
import io.iohk.atala.castor.core.service.DIDService
import io.iohk.atala.iam.oidc.CredentialIssuerEndpoints.ExtendedErrorResponse
import io.iohk.atala.iam.oidc.http.*
import io.iohk.atala.iam.oidc.http.CredentialErrorCode.*
import io.iohk.atala.iam.oidc.service.OIDCCredentialIssuerService
import io.iohk.atala.shared.models.WalletAccessContext
import zio.{IO, URLayer, ZIO, ZLayer}

trait CredentialIssuerController {
  def issueCredential(
      ctx: RequestContext,
      didRef: String,
      credentialRequest: CredentialRequest
  ): IO[ExtendedErrorResponse, CredentialResponse]

  def createCredentialOffer(
      ctx: RequestContext,
      didRef: String,
      credentialOfferRequest: CredentialOfferRequest
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialOfferResponse]

  def getNonce(
      ctx: RequestContext,
      didRef: String,
      request: NonceRequest
  ): ZIO[WalletAccessContext, ErrorResponse, NonceResponse]

  def getIssuerMetadata(
      ctx: RequestContext,
      didRef: String
  ): IO[ErrorResponse, IssuerMetadata]
}

object CredentialIssuerController {
  object Errors {
    def badRequestInvalidDID(didRef: String, details: String): ExtendedErrorResponse =
      Right(
        CredentialErrorResponse(
          error = invalid_request,
          errorDescription = Some(s"Invalid DID input: $didRef. Error: $details")
        )
      )

    def badRequestDIDResolutionFailed(didRef: String, details: String): ExtendedErrorResponse =
      Right(
        CredentialErrorResponse(
          error = invalid_request,
          errorDescription = Some(s"Failed to resolve DID: $didRef. Error: $details")
        )
      )

    def badRequestInvalidProof(jwt: String, details: String): ExtendedErrorResponse =
      Right(
        CredentialErrorResponse(error = invalid_proof, errorDescription = Some(s"Invalid proof: $jwt. Error: $details"))
      )

    def badRequestUnsupportedCredentialFormat(format: CredentialFormat): ExtendedErrorResponse =
      Right(
        CredentialErrorResponse(
          error = unsupported_credential_format,
          errorDescription = Some(s"Unsupported credential format: $format")
        )
      )

    def badRequestUnsupportedCredentialType(details: String): ExtendedErrorResponse =
      Right(
        CredentialErrorResponse(
          error = unsupported_credential_type,
          errorDescription = Some(s"Unsupported credential type. Error: $details")
        )
      )

    def serverError(details: Option[String]): ExtendedErrorResponse =
      Left(internalServerError("InternalServerError", details, instance = "CredentialIssuerController"))
  }
}

case class CredentialIssuerControllerImpl(didService: DIDService, credentialIssuerService: OIDCCredentialIssuerService)
    extends CredentialIssuerController {

  import CredentialIssuerController.Errors.*
  import OIDCCredentialIssuerService.Errors.*

  private def parseIssuerDID[E](didRef: String, errorFn: (String, String) => E): IO[E, CanonicalPrismDID] = {
    for {
      prismDID <- ZIO
        .fromEither(PrismDID.fromString(didRef))
        .mapError[E](didParsingError => errorFn(didRef, didParsingError))
    } yield prismDID.asCanonical
  }

  private def parseIssuerDIDBasicError(didRef: String): IO[ErrorResponse, CanonicalPrismDID] =
    parseIssuerDID(
      didRef,
      (didRef, detail) => ErrorResponse.badRequest(detail = Some(s"Invalid DID input $didRef. $detail"))
    )

  private def parseIssuerDIDOidc4vcError(difRef: String): IO[ExtendedErrorResponse, CanonicalPrismDID] =
    parseIssuerDID(difRef, badRequestInvalidDID)

  def issueCredential(
      ctx: RequestContext,
      didRef: String,
      credentialRequest: CredentialRequest
  ): IO[ExtendedErrorResponse, CredentialResponse] = {
    credentialRequest match
      case JwtCredentialRequest(
            format,
            proof,
            credentialIdentifier,
            credentialResponseEncryption,
            credentialDefinition
          ) =>
        issueJwtCredential(didRef, proof, credentialIdentifier, credentialDefinition, credentialResponseEncryption)
      case other: CredentialRequest => // add other formats here
        ZIO.fail(badRequestUnsupportedCredentialFormat(credentialRequest.format))
  }

  def issueJwtCredential(
      didRef: String,
      maybeProof: Option[Proof],
      maybeCredentialIdentifier: Option[String],
      maybeCredentialDefinition: Option[CredentialDefinition],
      maybeEncryption: Option[CredentialResponseEncryption]
  ): IO[ExtendedErrorResponse, CredentialResponse] = {
    maybeProof match {
      case Some(JwtProof(proofType, jwt)) =>
        for {
          canonicalPrismDID: CanonicalPrismDID <- parseIssuerDIDOidc4vcError(didRef)
          _ <- ZIO
            .ifZIO(credentialIssuerService.verifyJwtProof(jwt))(
              ZIO.unit,
              ZIO.fail(OIDCCredentialIssuerService.Errors.InvalidProof("Invalid proof"))
            )
            .mapError { case InvalidProof(message) =>
              badRequestInvalidProof(jwt, message)
            }
          credentialDefinition <- ZIO
            .fromOption(maybeCredentialDefinition)
            .mapError(_ => badRequestUnsupportedCredentialType("No credential definition provided"))
          validatedCredentialDefinition <- credentialIssuerService
            .validateCredentialDefinition(credentialDefinition)
            .mapError(ue =>
              serverError(Some(s"Unexpected error while validating credential definition: ${ue.message}"))
            )
          credential <- credentialIssuerService
            .issueJwtCredential(
              canonicalPrismDID,
              maybeCredentialIdentifier,
              validatedCredentialDefinition
            )
            .mapError(ue => serverError(Some(s"Unexpected error while issuing credential: ${ue.message}")))
        } yield ImmediateCredentialResponse(credential.value)
      case None => ZIO.fail(badRequestInvalidProof(jwt = "empty", details = "No proof provided"))
    }
  }

  override def createCredentialOffer(
      ctx: RequestContext,
      didRef: String,
      credentialOfferRequest: CredentialOfferRequest
  ): ZIO[WalletAccessContext, ErrorResponse, CredentialOfferResponse] = {
    for {
      canonicalPrismDID <- parseIssuerDIDBasicError(didRef)
      resp <- credentialIssuerService
        .createCredentialOffer(canonicalPrismDID, credentialOfferRequest.claims)
        .map(offer => CredentialOfferResponse(offer.offerUri))
        .mapError(ue =>
          internalServerError(detail = Some(s"Unexpected error while creating credential offer: ${ue.message}"))
        )
    } yield resp
  }

  override def getNonce(
      ctx: RequestContext,
      didRef: String,
      request: NonceRequest
  ): ZIO[WalletAccessContext, ErrorResponse, NonceResponse] = {
    credentialIssuerService
      .getIssuanceSessionNonce(request.issuerState)
      .map(nonce => NonceResponse(nonce))
      .mapError(ue =>
        internalServerError(detail = Some(s"Unexpected error while creating credential offer: ${ue.message}"))
      )
  }

  // TODO: implement
  override def getIssuerMetadata(ctx: RequestContext, didRef: String): IO[ErrorResponse, IssuerMetadata] = {
    for {
      canonicalPrismDID <- parseIssuerDIDBasicError(didRef)
      credentialIssuerBaseUrl = s"http://localhost:8080/prism-agent/oidc4vc/${canonicalPrismDID.toString}"
    } yield IssuerMetadata(
      credential_issuer = credentialIssuerBaseUrl,
      authorization_servers = Some(Seq("TODO: return url")),
      credential_endpoint = s"$credentialIssuerBaseUrl/credentials",
    )
  }
}

object CredentialIssuerControllerImpl {
  val layer: URLayer[DIDService & OIDCCredentialIssuerService, CredentialIssuerController] =
    ZLayer.fromFunction(CredentialIssuerControllerImpl(_, _))
}
