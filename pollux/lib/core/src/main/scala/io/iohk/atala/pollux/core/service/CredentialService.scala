package io.iohk.atala.pollux.core.service

import io.circe.syntax.*
import io.circe.{Json, JsonObject}
import io.iohk.atala.castor.core.model.did.{CanonicalPrismDID, PrismDID, VerificationRelationship}
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.issuecredential.{Attribute, IssueCredential, OfferCredential, RequestCredential}
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.model.error.CredentialServiceError
import io.iohk.atala.pollux.core.model.error.CredentialServiceError.*
import io.iohk.atala.pollux.vc.jwt.Issuer
import io.iohk.atala.shared.models.WalletAccessContext
import zio.{IO, ZIO}

import java.nio.charset.StandardCharsets
import java.util.UUID

trait CredentialService {

  def createJWTIssueCredentialRecord(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      thid: DidCommID,
      maybeSchemaId: Option[String],
      claims: io.circe.Json,
      validityPeriod: Option[Double] = None,
      automaticIssuance: Option[Boolean],
      issuingDID: CanonicalPrismDID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def createAnonCredsIssueCredentialRecord(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      thid: DidCommID,
      credentialDefinitionGUID: UUID,
      credentialDefinitionId: String,
      claims: io.circe.Json,
      validityPeriod: Option[Double] = None,
      automaticIssuance: Option[Boolean]
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  /** Return a list of records as well as a count of all filtered items */
  def getIssueCredentialRecords(
      ignoreWithZeroRetries: Boolean,
      offset: Option[Int] = None,
      limit: Option[Int] = None
  ): ZIO[WalletAccessContext, CredentialServiceError, (Seq[IssueCredentialRecord], Int)]

  def getIssueCredentialRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): ZIO[WalletAccessContext, CredentialServiceError, Seq[IssueCredentialRecord]]

  def getIssueCredentialRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): IO[CredentialServiceError, Seq[IssueCredentialRecord]]

  def getIssueCredentialRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, Option[IssueCredentialRecord]]

  def getIssueCredentialRecordByThreadId(
      thid: DidCommID,
      ignoreWithZeroRetries: Boolean
  ): ZIO[WalletAccessContext, CredentialServiceError, Option[IssueCredentialRecord]]

  def receiveCredentialOffer(
      offer: OfferCredential
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def acceptCredentialOffer(
      recordId: DidCommID,
      subjectId: Option[String]
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def generateJWTCredentialRequest(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def generateAnonCredsCredentialRequest(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def receiveCredentialRequest(
      request: RequestCredential
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def acceptCredentialRequest(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def generateJWTCredential(
      recordId: DidCommID,
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def generateAnonCredsCredential(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def receiveCredentialIssue(
      issueCredential: IssueCredential
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def markOfferSent(recordId: DidCommID): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def markRequestSent(recordId: DidCommID): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def markCredentialSent(recordId: DidCommID): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord]

  def reportProcessingFailure(
      recordId: DidCommID,
      failReason: Option[String]
  ): ZIO[WalletAccessContext, CredentialServiceError, Unit]

  def createJwtIssuer(
      jwtIssuerDID: PrismDID,
      verificationRelationship: VerificationRelationship
  ): ZIO[WalletAccessContext, CredentialServiceError, Issuer]

}

object CredentialService {
  def convertJsonClaimsToAttributes(
      claims: io.circe.Json
  ): IO[CredentialServiceError, Seq[Attribute]] = {
    for {
      fields <- ZIO.succeed(claims.asObject.map(_.toMap).getOrElse(Map.empty).toList)
      res <- ZIO.foreach(fields) {
        case (k, v) if v.isString =>
          ZIO.succeed(Attribute(name = k, value = v.asString.get))
        case (k, v) =>
          ZIO.succeed {
            val jsonValue = v.noSpaces
            Attribute(
              name = k,
              value = java.util.Base64.getUrlEncoder.encodeToString(jsonValue.getBytes(StandardCharsets.UTF_8)),
              media_type = Some("application/json"),
            )
          }
      }
    } yield res
  }

  def convertAttributesToJsonClaims(
      attributes: Seq[Attribute]
  ): IO[CredentialServiceError, JsonObject] = {
    for {
      claims <- ZIO.foldLeft(attributes)(JsonObject()) { case (jsonObject, attr) =>
        attr.media_type match
          case None =>
            ZIO.succeed(jsonObject.add(attr.name, attr.value.asJson))

          case Some("application/json") =>
            val jsonBytes = java.util.Base64.getUrlDecoder.decode(attr.value.getBytes(StandardCharsets.UTF_8))
            io.circe.parser.parse(new String(jsonBytes, StandardCharsets.UTF_8)) match
              case Right(value) => ZIO.succeed(jsonObject.add(attr.name, value))
              case Left(error)  => ZIO.fail(UnsupportedVCClaimsValue(error.message))

          case Some(media_type) =>
            ZIO.fail(UnsupportedVCClaimsMediaType(media_type))
      }
    } yield claims
  }
}
