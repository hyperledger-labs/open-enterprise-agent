package io.iohk.atala.pollux.core.service

import io.circe.Json
import io.iohk.atala.agent.walletapi.memory.GenericSecretStorageInMemory
import io.iohk.atala.agent.walletapi.service.ManagedDIDService
import io.iohk.atala.agent.walletapi.storage.GenericSecretStorage
import io.iohk.atala.castor.core.model.did.PrismDID
import io.iohk.atala.castor.core.service.DIDService
import io.iohk.atala.mercury.model.{AttachmentDescriptor, DidId}
import io.iohk.atala.mercury.protocol.issuecredential.*
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.model.presentation.{ClaimFormat, Ldp, Options, PresentationDefinition}
import io.iohk.atala.pollux.core.repository.{CredentialDefinitionRepositoryInMemory, CredentialRepositoryInMemory}
import io.iohk.atala.pollux.vc.jwt.*
import io.iohk.atala.shared.models.{WalletAccessContext, WalletId}
import zio.*

import java.util.UUID

trait CredentialServiceSpecHelper {

  protected val didResolverLayer = ZLayer.fromZIO(ZIO.succeed(makeResolver(Map.empty)))

  protected val defaultWalletLayer = ZLayer.succeed(WalletAccessContext(WalletId.default))

  protected val credentialDefinitionServiceLayer =
    CredentialDefinitionRepositoryInMemory.layer ++ ResourceURIDereferencerImpl.layer >+>
      CredentialDefinitionServiceImpl.layer ++ defaultWalletLayer

  protected val credentialServiceLayer
      : URLayer[DIDService & ManagedDIDService & URIDereferencer, CredentialService & CredentialDefinitionService] =
    ZLayer.makeSome[DIDService & ManagedDIDService & URIDereferencer, CredentialService & CredentialDefinitionService](
      CredentialRepositoryInMemory.layer,
      ZLayer.fromFunction(PrismDidResolver(_)),
      credentialDefinitionServiceLayer,
      GenericSecretStorageInMemory.layer,
      LinkSecretServiceImpl.layer,
      CredentialServiceImpl.layer
    )

  protected def offerCredential(
      thid: Option[UUID] = Some(UUID.randomUUID())
  ) = OfferCredential(
    from = DidId("did:prism:issuer"),
    to = DidId("did:prism:holder"),
    thid = thid.map(_.toString),
    attachments = Seq(
      AttachmentDescriptor.buildJsonAttachment(
        format = Some(IssueCredentialOfferFormat.JWT.name),
        payload = CredentialOfferAttachment(
          Options(UUID.randomUUID().toString(), "my-domain"),
          PresentationDefinition(format = Some(ClaimFormat(ldp = Some(Ldp(Seq("EcdsaSecp256k1Signature2019"))))))
        )
      )
    ),
    body = OfferCredential.Body(
      goal_code = Some("Offer Credential"),
      credential_preview = CredentialPreview(attributes = Seq(Attribute("name", "Alice")))
    )
  )

  protected def requestCredential(thid: Option[DidCommID] = Some(DidCommID())) = RequestCredential(
    from = DidId("did:prism:holder"),
    to = DidId("did:prism:issuer"),
    thid = thid.map(_.toString),
    attachments = Nil,
    body = RequestCredential.Body()
  )

  protected def issueCredential(thid: Option[DidCommID] = Some(DidCommID())) = IssueCredential(
    from = DidId("did:prism:issuer"),
    to = DidId("did:prism:holder"),
    thid = thid.map(_.toString),
    attachments = Seq(
      AttachmentDescriptor.buildBase64Attachment(
        UUID.randomUUID().toString,
        payload = "VC content".getBytes,
        mediaType = Some("application/json"),
        format = Some(IssueCredentialIssuedFormat.JWT.name)
      )
    ),
    body = IssueCredential.Body()
  )

  protected def makeResolver(lookup: Map[String, DIDDocument]): DidResolver = (didUrl: String) => {
    lookup
      .get(didUrl)
      .fold(
        ZIO.succeed(DIDResolutionFailed(NotFound(s"DIDDocument not found for $didUrl")))
      )((didDocument: DIDDocument) => {
        ZIO.succeed(
          DIDResolutionSucceeded(
            didDocument,
            DIDDocumentMetadata()
          )
        )
      })
  }

  extension (svc: CredentialService)
    def createJWTIssueCredentialRecord(
        pairwiseIssuerDID: DidId = DidId("did:prism:issuer"),
        pairwiseHolderDID: DidId = DidId("did:prism:holder-pairwise"),
        thid: DidCommID = DidCommID(),
        maybeSchemaId: Option[String] = None,
        claims: Json = io.circe.parser
          .parse("""
              |{
              | "name":"Alice",
              | "address": {
              |   "street": "Street Name",
              |   "number": "12"
              | }
              |}
              |""".stripMargin)
          .getOrElse(Json.Null),
        validityPeriod: Option[Double] = None,
        automaticIssuance: Option[Boolean] = None
    ) = for {
      issuingDID <- ZIO.fromEither(
        PrismDID.buildCanonicalFromSuffix("5c2576867a5544e5ad05cdc94f02c664b99ff65c28e8b62aada767244c2199fe")
      )
      record <- svc.createJWTIssueCredentialRecord(
        pairwiseIssuerDID = pairwiseIssuerDID,
        pairwiseHolderDID = pairwiseHolderDID,
        thid = thid,
        maybeSchemaId = maybeSchemaId,
        claims = claims,
        validityPeriod = validityPeriod,
        automaticIssuance = automaticIssuance,
        issuingDID = issuingDID
      )
    } yield record

    def createAnonCredsIssueCredentialRecord(
        credentialDefinitionGUID: UUID,
        pairwiseIssuerDID: DidId = DidId("did:prism:issuer"),
        pairwiseHolderDID: DidId = DidId("did:prism:holder-pairwise"),
        thid: DidCommID = DidCommID(),
        claims: Json = io.circe.parser
          .parse("""
                |{
                |  "emailAddress": "alice@wonderland.com",
                |  "familyName": "Wonderland",
                |  "dateOfIssuance": "2020-11-13T20:20:39+00:00",
                |  "drivingLicenseID": "12345",
                |  "drivingClass": "3"
                |}
                |""".stripMargin)
          .getOrElse(Json.Null),
        validityPeriod: Option[Double] = None,
        automaticIssuance: Option[Boolean] = None,
        credentialDefinitionId: String = "http://test.com/cred-def/1234",
    ) = for {
      record <- svc.createAnonCredsIssueCredentialRecord(
        pairwiseIssuerDID = pairwiseIssuerDID,
        pairwiseHolderDID = pairwiseHolderDID,
        thid = thid,
        claims = claims,
        validityPeriod = validityPeriod,
        automaticIssuance = automaticIssuance,
        credentialDefinitionGUID = credentialDefinitionGUID,
        credentialDefinitionId = credentialDefinitionId
      )
    } yield record

}
