package io.iohk.atala.iam.oidc.controller

import io.iohk.atala.api.http.ErrorResponse.internalServerError
import io.iohk.atala.api.http.RequestContext
import io.iohk.atala.castor.core.model.did.{CanonicalPrismDID, PrismDID}
import io.iohk.atala.castor.core.service.DIDService
import io.iohk.atala.iam.oidc.CredentialIssuerEndpoints.ExtendedErrorResponse
import io.iohk.atala.iam.oidc.domain.OIDCCredentialIssuerService
import io.iohk.atala.iam.oidc.http.*
import io.iohk.atala.iam.oidc.http.CredentialErrorCode.*
import zio.{IO, URLayer, ZIO, ZLayer}

trait CredentialIssuerController {
  def issueCredential(
      ctx: RequestContext,
      didRef: String,
      credentialRequest: CredentialRequest
  ): IO[ExtendedErrorResponse, CredentialResponse]
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

  def resolveIssuerDID(didRef: String): IO[ExtendedErrorResponse, CanonicalPrismDID] = {
    for {
      prismDID: PrismDID <- ZIO
        .fromEither(PrismDID.fromString(didRef))
        .mapError(didParsingError => badRequestInvalidDID(didRef, didParsingError))
      resolution <- didService
        .resolveDID(prismDID)
        .mapError(didResolutionError => badRequestInvalidDID(didRef, didResolutionError.message))
      canonicalDID <- ZIO
        .fromOption(resolution.map(_._2.id))
        .mapError(_ => badRequestDIDResolutionFailed(didRef, s"The DID $didRef is not resolvable"))
    } yield canonicalDID
  }

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
          canonicalPrismDID: CanonicalPrismDID <- resolveIssuerDID(didRef)
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
        } yield ImmediateCredentialResponse(credential.asInstanceOf[String])
      case None => ZIO.fail(badRequestInvalidProof(jwt = "empty", details = "No proof provided"))
    }
  }
}

object CredentialIssuerControllerImpl {
  val layer: URLayer[DIDService & OIDCCredentialIssuerService, CredentialIssuerController] =
    ZLayer.fromFunction(CredentialIssuerControllerImpl(_, _))
}
