package io.iohk.atala.iam.oidc.service

import io.circe.Json
import io.iohk.atala.agent.walletapi.storage.DIDNonSecretStorage
import io.iohk.atala.castor.core.model.did.CanonicalPrismDID
import io.iohk.atala.castor.core.model.did.{PrismDID, VerificationRelationship}
import io.iohk.atala.castor.core.service.DIDService
import io.iohk.atala.iam.oidc.domain.IssuanceSession
import io.iohk.atala.iam.oidc.http.*
import io.iohk.atala.iam.oidc.storage.IssuanceSessionStorage
import io.iohk.atala.pollux.core.service.CredentialService
import io.iohk.atala.pollux.vc.jwt.{DID, Issuer, JWT, JwtCredential, W3cCredentialPayload}
import io.iohk.atala.shared.models.{WalletAccessContext, WalletId}
import zio.*

import java.time.Instant
import scala.util.Try

// OIDC prefix is added to the service name to avoid name conflicts with a similar service CredentialIssuerService
// It would be nice to refactor these services and merge them into one
trait OIDCCredentialIssuerService {

  import OIDCCredentialIssuerService.Error
  import OIDCCredentialIssuerService.Errors.*

  def verifyJwtProof(jwt: String): IO[InvalidProof, Boolean]

  def validateCredentialDefinition(
      credentialDefinition: CredentialDefinition
  ): IO[UnexpectedError, CredentialDefinition]

  def issueJwtCredential(
      prismDID: PrismDID,
      credentialIdentifier: Option[String],
      credentialDefinition: CredentialDefinition,
  ): IO[Error, JWT]

  def createCredentialOffer(
      issuerDID: PrismDID,
      claims: zio.json.ast.Json
  ): ZIO[WalletAccessContext, Error, CredentialOffer]

  def getIssuanceSessionNonce(issuerState: String): ZIO[WalletAccessContext, Error, String]
}

object OIDCCredentialIssuerService {
  trait Error {
    def message: String
  }

  object Errors {
    case class InvalidProof(message: String) extends Error

    case class DIDResolutionError(message: String) extends Error

    case class ServiceError(message: String) extends Error

    case class UnexpectedError(cause: Throwable) extends Error {
      override def message: String = cause.getMessage
    }
  }
}

case class OIDCCredentialIssuerServiceImpl(
    didService: DIDService,
    didNonSecretStorage: DIDNonSecretStorage,
    credentialService: CredentialService,
    issuanceSessionStorage: IssuanceSessionStorage
) extends OIDCCredentialIssuerService {

  import OIDCCredentialIssuerService.Error
  import OIDCCredentialIssuerService.Errors.*
  override def verifyJwtProof(jwt: String): IO[InvalidProof, Boolean] = {
    ZIO.succeed(true) // TODO: implement
  }

  override def validateCredentialDefinition(
      credentialDefinition: CredentialDefinition
  ): IO[UnexpectedError, CredentialDefinition] = {
    ZIO.succeed(credentialDefinition) // TODO: implement
  }

  override def issueJwtCredential(
      prismDID: PrismDID,
      credentialIdentifier: Option[String],
      credentialDefinition: CredentialDefinition
  ): IO[OIDCCredentialIssuerService.Error, JWT] = {
    for {
      wac <- didNonSecretStorage
        .getPrismDidWalletId(prismDID)
        .flatMap(ZIO.fromOption)
        .mapError(_ => ServiceError(s"Failed to get wallet ID for DID: $prismDID"))
        .map(WalletAccessContext.apply)

      jwtIssuer <- credentialService
        .createJwtIssuer(prismDID, VerificationRelationship.AssertionMethod)
        .mapError(_ => ServiceError(s"Failed to create JWT issuer for DID: $prismDID"))
        .provideSomeLayer(ZLayer.succeed(wac))

      jwtVC <- buildJwtVerifiableCredential(jwtIssuer.did, credentialIdentifier, credentialDefinition)
      jwt <- issueJwtVC(jwtIssuer, jwtVC)
    } yield jwt
  }

  def buildJwtVerifiableCredential(
      issuerDid: DID,
      credentialIdentifier: Option[String],
      credentialDefinition: CredentialDefinition
  ): IO[Error, W3cCredentialPayload] = {
    val credential = W3cCredentialPayload(
      `@context` = Set(
        "https://www.w3.org/2018/credentials/v1"
        // TODO: Add schemaID from schema registry
      ) ++ credentialDefinition.`@context`.getOrElse(Nil), // TODO: Figure out how to validate the context ^^^
      maybeId = credentialIdentifier,
      `type` = Set(
        "VerifiableCredential"
      ) ++ credentialDefinition.`type`, // TODO: This information should come from Schema registry by record.schemaId
      issuer = issuerDid,
      issuanceDate = Instant.now(),
      maybeExpirationDate = None, // TODO: Add expiration date
      maybeCredentialSchema = None, // TODO: Add schema from schema registry
      credentialSubject = buildCredentialSubject(credentialDefinition.credentialSubject),
      maybeCredentialStatus = None, // TODO: Add credential status
      maybeRefreshService = None, // TODO: Add refresh service
      maybeEvidence = None, // TODO: Add evidence
      maybeTermsOfUse = None // TODO: Add terms of use
    )

    ZIO.succeed(credential) // TODO: there might be other calls to fill the VC claims from the session, etc
  }

  def buildCredentialSubject(credentialSubjectOption: Option[CredentialSubject]): Json = {
    val claimsOpt = credentialSubjectOption.map(credentialSubject =>
      credentialSubject.map { case (claim, descriptor) =>
        claim -> Json.fromString("lorem ipsum") // TODO: Add real data (e.g. from the user record, etc
      }.toSeq
    )
    claimsOpt.fold(Json.obj())(claims => Json.obj(claims: _*))
  }

  def issueJwtVC(issuer: Issuer, payload: W3cCredentialPayload): IO[Error, JWT] = {
    ZIO
      .fromTry(Try(JwtCredential.encodeJwt(payload.toJwtCredentialPayload, issuer)))
      .mapError(e => ServiceError(s"Failed to issue JWT: ${e.getMessage}"))
  }

  override def getIssuanceSessionNonce(
      issuerState: String
  ): ZIO[WalletAccessContext, OIDCCredentialIssuerService.Error, String] =
    issuanceSessionStorage
      .getByIssuerState(issuerState)
      .mapBoth(e => ServiceError(s"Failed to start issuance session: ${e.message}"), _.map(_.nonce))
      .someOrFail(ServiceError(s"The IssuanceSession with the issuerState $issuerState does not exist"))

  override def createCredentialOffer(
      issuingDid: PrismDID,
      claims: zio.json.ast.Json
  ): ZIO[WalletAccessContext, OIDCCredentialIssuerService.Error, CredentialOffer] =
    val canonicalIssuingDid = issuingDid.asCanonical
    for {
      session <- buildNewIssuanceSession(canonicalIssuingDid, claims)
      _ <- issuanceSessionStorage
        .start(session)
        .mapError(e => ServiceError(s"Failed to start issuance session: ${e.message}"))
    } yield CredentialOffer(
      credential_issuer =
        s"http://localhost:8080/prism-agent/${canonicalIssuingDid.toString}", // TODO: add issuer metadata endpoint
      credential_configuration_ids = Seq("UniversityDegreeCredential"), // TODO: allow credential configuration CRUD
      grants = Some(
        CredentialOfferGrant(
          authorization_code = CredentialOfferAuthorizationGrant(issuer_state = Some(session.issuerState))
        )
      )
    )

  private def buildNewIssuanceSession(
      issuerDid: CanonicalPrismDID,
      claims: zio.json.ast.Json
  ): UIO[IssuanceSession] = {
    for {
      id <- ZIO.random.flatMap(_.nextUUID)
      nonce <- ZIO.random.flatMap(_.nextUUID)
      issuerState <- ZIO.random.flatMap(_.nextUUID)
    } yield IssuanceSession(
      id = id,
      nonce = nonce.toString,
      issuerState = issuerState.toString,
      claims = claims,
      schemaId = None, // FIXME: populate correct value
      subjectDid = None, // FIXME: populate correct value
      issuingDid = issuerDid,
    )
  }
}

object OIDCCredentialIssuerServiceImpl {
  val layer: URLayer[
    DIDService & DIDNonSecretStorage & CredentialService & IssuanceSessionStorage,
    OIDCCredentialIssuerService
  ] =
    ZLayer.fromFunction(OIDCCredentialIssuerServiceImpl(_, _, _, _))
}
