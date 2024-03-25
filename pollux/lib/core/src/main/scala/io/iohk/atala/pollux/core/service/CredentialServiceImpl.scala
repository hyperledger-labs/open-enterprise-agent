package io.iohk.atala.pollux.core.service

import io.circe.Json
import io.circe.syntax.*
import io.iohk.atala.agent.walletapi.model.{ManagedDIDState, PublicationState}
import io.iohk.atala.agent.walletapi.service.ManagedDIDService
import io.iohk.atala.agent.walletapi.storage.GenericSecretStorage
import io.iohk.atala.castor.core.model.did.{CanonicalPrismDID, PrismDID, VerificationRelationship}
import io.iohk.atala.castor.core.service.DIDService
import io.iohk.atala.mercury.model.*
import io.iohk.atala.mercury.protocol.issuecredential.*
import io.iohk.atala.pollux.*
import io.iohk.atala.pollux.anoncreds.{
  AnoncredCreateCredentialDefinition,
  AnoncredCredential,
  AnoncredCredentialOffer,
  AnoncredLib
}
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.model.CredentialFormat.AnonCreds
import io.iohk.atala.pollux.core.model.IssueCredentialRecord.ProtocolState.OfferReceived
import io.iohk.atala.pollux.core.model.error.CredentialServiceError
import io.iohk.atala.pollux.core.model.error.CredentialServiceError.*
import io.iohk.atala.pollux.core.model.presentation.*
import io.iohk.atala.pollux.core.model.schema.CredentialSchema
import io.iohk.atala.pollux.core.model.secret.CredentialDefinitionSecret
import io.iohk.atala.pollux.core.repository.{CredentialRepository, CredentialStatusListRepository}
import io.iohk.atala.pollux.vc.jwt.{ES256KSigner, Issuer as JwtIssuer, *}
import io.iohk.atala.shared.http.{DataUrlResolver, GenericUriResolver}
import io.iohk.atala.shared.models.WalletAccessContext
import io.iohk.atala.shared.utils.aspects.CustomMetricsAspect
import zio.*
import zio.prelude.ZValidation

import java.net.URI
import java.rmi.UnexpectedException
import java.time.{Instant, ZoneId}
import java.util.UUID
import scala.language.implicitConversions

object CredentialServiceImpl {
  val layer: URLayer[
    CredentialRepository & CredentialStatusListRepository & DidResolver & URIDereferencer & GenericSecretStorage &
      CredentialDefinitionService & LinkSecretService & DIDService & ManagedDIDService,
    CredentialService
  ] = {
    ZLayer.fromZIO {
      for {
        credentialRepo <- ZIO.service[CredentialRepository]
        credentialStatusListRepo <- ZIO.service[CredentialStatusListRepository]
        didResolver <- ZIO.service[DidResolver]
        uriDereferencer <- ZIO.service[URIDereferencer]
        genericSecretStorage <- ZIO.service[GenericSecretStorage]
        credDefenitionService <- ZIO.service[CredentialDefinitionService]
        linkSecretService <- ZIO.service[LinkSecretService]
        didService <- ZIO.service[DIDService]
        manageDidService <- ZIO.service[ManagedDIDService]
        issueCredentialSem <- Semaphore.make(1)
      } yield CredentialServiceImpl(
        credentialRepo,
        credentialStatusListRepo,
        didResolver,
        uriDereferencer,
        genericSecretStorage,
        credDefenitionService,
        linkSecretService,
        didService,
        manageDidService,
        5,
        issueCredentialSem
      )
    }
  }

  //  private val VC_JSON_SCHEMA_URI = "https://w3c-ccg.github.io/vc-json-schemas/schema/2.0/schema.json"
  private val VC_JSON_SCHEMA_TYPE = "CredentialSchema2022"
}

private class CredentialServiceImpl(
    credentialRepository: CredentialRepository,
    credentialStatusListRepository: CredentialStatusListRepository,
    didResolver: DidResolver,
    uriDereferencer: URIDereferencer,
    genericSecretStorage: GenericSecretStorage,
    credentialDefinitionService: CredentialDefinitionService,
    linkSecretService: LinkSecretService,
    didService: DIDService,
    managedDIDService: ManagedDIDService,
    maxRetries: Int = 5, // TODO move to config
    issueCredentialSem: Semaphore
) extends CredentialService {

  import CredentialServiceImpl.*
  import IssueCredentialRecord.*

  override def getIssueCredentialRecords(
      ignoreWithZeroRetries: Boolean,
      offset: Option[Int],
      limit: Option[Int]
  ): ZIO[WalletAccessContext, CredentialServiceError, (Seq[IssueCredentialRecord], Int)] = {
    for {
      records <- credentialRepository
        .getIssueCredentialRecords(ignoreWithZeroRetries = ignoreWithZeroRetries, offset = offset, limit = limit)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def getIssueCredentialRecordByThreadId(
      thid: DidCommID,
      ignoreWithZeroRetries: Boolean
  ): ZIO[WalletAccessContext, CredentialServiceError, Option[IssueCredentialRecord]] =
    for {
      record <- credentialRepository
        .getIssueCredentialRecordByThreadId(thid, ignoreWithZeroRetries)
        .mapError(RepositoryError.apply)
    } yield record

  override def getIssueCredentialRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, Option[IssueCredentialRecord]] = {
    for {
      record <- credentialRepository
        .getIssueCredentialRecord(recordId)
        .mapError(RepositoryError.apply)
    } yield record
  }

  override def createJWTIssueCredentialRecord(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      thid: DidCommID,
      maybeSchemaId: Option[String],
      claims: Json,
      validityPeriod: Option[Double],
      automaticIssuance: Option[Boolean],
      issuingDID: CanonicalPrismDID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      _ <- maybeSchemaId match
        case Some(schemaId) =>
          CredentialSchema
            .validateJWTClaims(schemaId, claims.noSpaces, uriDereferencer)
            .mapError(e => CredentialSchemaError(e))
        case None =>
          ZIO.unit
      attributes <- CredentialService.convertJsonClaimsToAttributes(claims)
      offer <- createJWTDidCommOfferCredential(
        pairwiseIssuerDID = pairwiseIssuerDID,
        pairwiseHolderDID = pairwiseHolderDID,
        maybeSchemaId = maybeSchemaId,
        claims = attributes,
        thid = thid,
        UUID.randomUUID().toString,
        "domain"
      )
      record <- ZIO.succeed(
        IssueCredentialRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = thid,
          schemaUri = maybeSchemaId,
          credentialDefinitionId = None,
          credentialDefinitionUri = None,
          credentialFormat = CredentialFormat.JWT,
          role = IssueCredentialRecord.Role.Issuer,
          subjectId = None,
          validityPeriod = validityPeriod,
          automaticIssuance = automaticIssuance,
          protocolState = IssueCredentialRecord.ProtocolState.OfferPending,
          offerCredentialData = Some(offer),
          requestCredentialData = None,
          anonCredsRequestMetadata = None,
          issueCredentialData = None,
          issuedCredentialRaw = None,
          issuingDID = Some(issuingDID),
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      count <- credentialRepository
        .createIssueCredentialRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect
        .startRecordingTime(s"${record.id}_issuer_offer_pending_to_sent_ms_gauge")
    } yield record
  }

  override def createAnonCredsIssueCredentialRecord(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      thid: DidCommID,
      credentialDefinitionGUID: UUID,
      credentialDefinitionId: String,
      claims: Json,
      validityPeriod: Option[Double],
      automaticIssuance: Option[Boolean]
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      credentialDefinition <- credentialDefinitionService
        .getByGUID(credentialDefinitionGUID)
        .mapError(e => CredentialServiceError.UnexpectedError(e.toString))
      _ <- CredentialSchema
        .validateAnonCredsClaims(credentialDefinition.schemaId, claims.noSpaces, uriDereferencer)
        .mapError(e => CredentialSchemaError(e))
      attributes <- CredentialService.convertJsonClaimsToAttributes(claims)
      offer <- createAnonCredsDidCommOfferCredential(
        pairwiseIssuerDID = pairwiseIssuerDID,
        pairwiseHolderDID = pairwiseHolderDID,
        schemaUri = credentialDefinition.schemaId,
        credentialDefinitionGUID = credentialDefinitionGUID,
        credentialDefinitionId = credentialDefinitionId,
        claims = attributes,
        thid = thid,
      )
      record <- ZIO.succeed(
        IssueCredentialRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = thid,
          schemaUri = Some(credentialDefinition.schemaId),
          credentialDefinitionId = Some(credentialDefinitionGUID),
          credentialDefinitionUri = Some(credentialDefinitionId),
          credentialFormat = CredentialFormat.AnonCreds,
          role = IssueCredentialRecord.Role.Issuer,
          subjectId = None,
          validityPeriod = validityPeriod,
          automaticIssuance = automaticIssuance,
          protocolState = IssueCredentialRecord.ProtocolState.OfferPending,
          offerCredentialData = Some(offer),
          requestCredentialData = None,
          anonCredsRequestMetadata = None,
          issueCredentialData = None,
          issuedCredentialRaw = None,
          issuingDID = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      count <- credentialRepository
        .createIssueCredentialRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect
        .startRecordingTime(s"${record.id}_issuer_offer_pending_to_sent_ms_gauge")
    } yield record
  }

  override def getIssueCredentialRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): ZIO[WalletAccessContext, CredentialServiceError, Seq[IssueCredentialRecord]] = {
    for {
      records <- credentialRepository
        .getIssueCredentialRecordsByStates(ignoreWithZeroRetries, limit, states: _*)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def getIssueCredentialRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: IssueCredentialRecord.ProtocolState*
  ): IO[CredentialServiceError, Seq[IssueCredentialRecord]] = {
    for {
      records <- credentialRepository
        .getIssueCredentialRecordsByStatesForAllWallets(ignoreWithZeroRetries, limit, states: _*)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def receiveCredentialOffer(
      offer: OfferCredential
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      attachment <- ZIO
        .fromOption(offer.attachments.headOption)
        .mapError(_ => CredentialServiceError.UnexpectedError("Missing attachment in credential offer"))

      format <- ZIO.fromOption(attachment.format).mapError(_ => MissingCredentialFormat)

      credentialFormat <- format match
        case value if value == IssueCredentialOfferFormat.JWT.name      => ZIO.succeed(CredentialFormat.JWT)
        case value if value == IssueCredentialOfferFormat.Anoncred.name => ZIO.succeed(CredentialFormat.AnonCreds)
        case value                                                      => ZIO.fail(UnsupportedCredentialFormat(value))

      _ <- validateCredentialOfferAttachment(credentialFormat, attachment)
      record <- ZIO.succeed(
        IssueCredentialRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = DidCommID(offer.thid.getOrElse(offer.id)),
          schemaUri = None,
          credentialDefinitionId = None,
          credentialDefinitionUri = None,
          credentialFormat = credentialFormat,
          role = Role.Holder,
          subjectId = None,
          validityPeriod = None,
          automaticIssuance = None,
          protocolState = IssueCredentialRecord.ProtocolState.OfferReceived,
          offerCredentialData = Some(offer),
          requestCredentialData = None,
          anonCredsRequestMetadata = None,
          issueCredentialData = None,
          issuedCredentialRaw = None,
          issuingDID = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      count <- credentialRepository
        .createIssueCredentialRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
    } yield record
  }

  private[this] def validateCredentialOfferAttachment(
      credentialFormat: CredentialFormat,
      attachment: AttachmentDescriptor
  ) = for {
    _ <- credentialFormat match
      case CredentialFormat.JWT =>
        attachment.data match
          case JsonData(json) =>
            ZIO
              .attempt(json.asJson.hcursor.downField("json").as[CredentialOfferAttachment])
              .mapError(e =>
                CredentialServiceError
                  .UnexpectedError(s"Unexpected error parsing credential offer attachment: ${e.toString}")
              )
          case _ =>
            ZIO.fail(
              CredentialServiceError
                .UnexpectedError(s"A JSON attachment is expected in the credential offer")
            )
      case CredentialFormat.AnonCreds =>
        attachment.data match
          case Base64(value) =>
            for {
              _ <- ZIO
                .attempt(AnoncredCredentialOffer(value))
                .mapError(e =>
                  CredentialServiceError.UnexpectedError(
                    s"Unexpected error parsing credential offer attachment: ${e.toString}"
                  )
                )
            } yield ()
          case _ =>
            ZIO.fail(
              CredentialServiceError
                .UnexpectedError(s"A Base64 attachment is expected in the credential offer")
            )
  } yield ()

  override def acceptCredentialOffer(
      recordId: DidCommID,
      maybeSubjectId: Option[String]
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.OfferReceived)
      count <- (record.credentialFormat, maybeSubjectId) match
        case (CredentialFormat.JWT, Some(subjectId)) =>
          for {
            _ <- ZIO
              .fromEither(PrismDID.fromString(subjectId))
              .mapError(_ => CredentialServiceError.UnsupportedDidFormat(subjectId))
            count <- credentialRepository
              .updateWithSubjectId(recordId, subjectId, ProtocolState.RequestPending)
              .mapError(RepositoryError.apply) @@ CustomMetricsAspect.startRecordingTime(
              s"${record.id}_issuance_flow_holder_req_pending_to_generated"
            )
          } yield count
        case (CredentialFormat.AnonCreds, None) =>
          credentialRepository
            .updateCredentialRecordProtocolState(recordId, ProtocolState.OfferReceived, ProtocolState.RequestPending)
            .mapError(RepositoryError.apply) @@ CustomMetricsAspect.startRecordingTime(
            s"${record.id}_issuance_flow_holder_req_pending_to_generated"
          )
        case (format, _) =>
          ZIO.fail(
            CredentialServiceError.UnexpectedError(
              s"Invalid subjectId input for $format offer acceptance: $maybeSubjectId"
            )
          )
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- credentialRepository
        .getIssueCredentialRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(recordId))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  private[this] def createPresentationPayload(
      record: IssueCredentialRecord,
      subject: JwtIssuer
  ): ZIO[WalletAccessContext, CredentialServiceError, PresentationPayload] = {
    for {
      maybeOptions <- getOptionsFromOfferCredentialData(record)
    } yield {
      W3cPresentationPayload(
        `@context` = Vector("https://www.w3.org/2018/presentations/v1"),
        maybeId = None,
        `type` = Vector("VerifiablePresentation"),
        verifiableCredential = IndexedSeq.empty,
        holder = subject.did.value,
        verifier = IndexedSeq.empty ++ maybeOptions.map(_.domain),
        maybeIssuanceDate = None,
        maybeExpirationDate = None
      ).toJwtPresentationPayload.copy(maybeNonce = maybeOptions.map(_.challenge))
    }
  }

  private[this] def getLongForm(
      did: PrismDID,
      allowUnpublishedIssuingDID: Boolean = false
  ) = {
    for {
      didState <- managedDIDService
        .getManagedDIDState(did.asCanonical)
        .mapError(e => RuntimeException(s"Error occurred while getting did from wallet: ${e.toString}"))
        .someOrFail(RuntimeException(s"Issuer DID does not exist in the wallet: $did"))
        .flatMap {
          case s @ ManagedDIDState(_, _, PublicationState.Published(_)) => ZIO.succeed(s)
          case s => ZIO.cond(allowUnpublishedIssuingDID, s, RuntimeException(s"Issuer DID must be published: $did"))
        }
      longFormPrismDID = PrismDID.buildLongFormFromOperation(didState.createOperation)
    } yield longFormPrismDID
  }

  private[this] def createJwtIssuer(
      jwtIssuerDID: PrismDID,
      verificationRelationship: VerificationRelationship
  ) = {
    for {
      // Automatically infer keyId to use by resolving DID and choose the corresponding VerificationRelationship
      issuingKeyId <- didService
        .resolveDID(jwtIssuerDID)
        .mapError(e => UnexpectedError(s"Error occured while resolving Issuing DID during VC creation: ${e.toString}"))
        .someOrFail(UnexpectedError(s"Issuing DID resolution result is not found"))
        .map { case (_, didData) => didData.publicKeys.find(_.purpose == verificationRelationship).map(_.id) }
        .someOrFail(
          UnexpectedError(s"Issuing DID doesn't have a key in ${verificationRelationship.name} to use: $jwtIssuerDID")
        )
      ecKeyPair <- managedDIDService
        .javaKeyPairWithDID(jwtIssuerDID.asCanonical, issuingKeyId)
        .mapError(e => UnexpectedError(s"Error occurred while getting issuer key-pair: ${e.toString}"))
        .someOrFail(
          UnexpectedError(s"Issuer key-pair does not exist in the wallet: ${jwtIssuerDID.toString}#$issuingKeyId")
        )
      (privateKey, publicKey) = ecKeyPair
      jwtIssuer = JwtIssuer(
        io.iohk.atala.pollux.vc.jwt.DID(jwtIssuerDID.toString),
        ES256KSigner(privateKey),
        publicKey
      )
    } yield jwtIssuer
  }

  override def generateJWTCredentialRequest(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestPending)
      subjectId <- ZIO
        .fromOption(record.subjectId)
        .mapError(_ => CredentialServiceError.UnexpectedError(s"Subject Id not found in record: ${recordId.value}"))
      subjectDID <- ZIO
        .fromEither(PrismDID.fromString(subjectId))
        .mapError(_ => CredentialServiceError.UnsupportedDidFormat(subjectId))
      longFormPrismDID <- getLongForm(subjectDID, true).mapError(err => UnexpectedError(err.getMessage))
      jwtIssuer <- createJwtIssuer(longFormPrismDID, VerificationRelationship.Authentication)
      presentationPayload <- createPresentationPayload(record, jwtIssuer)
      signedPayload = JwtPresentation.encodeJwt(presentationPayload.toJwtPresentationPayload, jwtIssuer)
      formatAndOffer <- ZIO
        .fromOption(record.offerCredentialFormatAndData)
        .mapError(_ => InvalidFlowStateError(s"No offer found for this record: $recordId"))
      request = createDidCommRequestCredential(formatAndOffer._1, formatAndOffer._2, signedPayload)
      count <- credentialRepository
        .updateWithJWTRequestCredential(recordId, request, ProtocolState.RequestGenerated)
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.endRecordingTime(
        s"${record.id}_issuance_flow_holder_req_pending_to_generated",
        "issuance_flow_holder_req_pending_to_generated_ms_gauge"
      ) @@ CustomMetricsAspect.startRecordingTime(s"${record.id}_issuance_flow_holder_req_generated_to_sent")
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- credentialRepository
        .getIssueCredentialRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(recordId))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  override def generateAnonCredsCredentialRequest(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestPending)
      offerCredential <- ZIO
        .fromOption(record.offerCredentialData)
        .mapError(_ => InvalidFlowStateError(s"No offer found for this record: ${record.id}"))
      body = RequestCredential.Body(goal_code = Some("Request Credential"))
      createCredentialRequest <- createAnonCredsRequestCredential(offerCredential)
      attachments = Seq(
        AttachmentDescriptor.buildBase64Attachment(
          mediaType = Some("application/json"),
          format = Some(IssueCredentialRequestFormat.Anoncred.name),
          payload = createCredentialRequest.request.data.getBytes()
        )
      )
      requestMetadata = createCredentialRequest.metadata
      request = RequestCredential(
        body = body,
        attachments = attachments,
        from = offerCredential.to,
        to = offerCredential.from,
        thid = offerCredential.thid
      )
      count <- credentialRepository
        .updateWithAnonCredsRequestCredential(recordId, request, requestMetadata, ProtocolState.RequestGenerated)
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.endRecordingTime(
        s"${record.id}_issuance_flow_holder_req_pending_to_generated",
        "issuance_flow_holder_req_pending_to_generated_ms_gauge"
      ) @@ CustomMetricsAspect.startRecordingTime(s"${record.id}_issuance_flow_holder_req_generated_to_sent")
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- credentialRepository
        .getIssueCredentialRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(recordId))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  private[this] def createAnonCredsRequestCredential(offerCredential: OfferCredential) = {
    for {
      attachmentData <- ZIO
        .fromOption(
          offerCredential.attachments
            .find(_.format.contains(IssueCredentialOfferFormat.Anoncred.name))
            .map(_.data)
            .flatMap {
              case Base64(value) => Some(new String(java.util.Base64.getDecoder.decode(value)))
              case _             => None
            }
        )
        .mapError(_ => InvalidFlowStateError(s"No AnonCreds offer attachment found"))
      credentialOffer = anoncreds.AnoncredCredentialOffer(attachmentData)
      _ <- ZIO.logInfo(s"Cred def ID => ${credentialOffer.getCredDefId}")
      credDefContent <- uriDereferencer
        .dereference(new URI(credentialOffer.getCredDefId))
        .mapError(err => UnexpectedError(err.toString))
      credentialDefinition = anoncreds.AnoncredCredentialDefinition(credDefContent)
      linkSecret <- linkSecretService
        .fetchOrCreate()
        .mapError(e => CredentialServiceError.LinkSecretError.apply(e.cause))
      createCredentialRequest = AnoncredLib.createCredentialRequest(linkSecret, credentialDefinition, credentialOffer)
    } yield createCredentialRequest
  }

  override def receiveCredentialRequest(
      request: RequestCredential
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordFromThreadIdWithState(
        request.thid.map(DidCommID(_)),
        ignoreWithZeroRetries = true,
        ProtocolState.OfferPending,
        ProtocolState.OfferSent
      )
      _ <- credentialRepository
        .updateWithJWTRequestCredential(record.id, request, ProtocolState.RequestReceived)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
      record <- credentialRepository
        .getIssueCredentialRecord(record.id)
        .mapError(RepositoryError.apply)
        .someOrFail(RecordIdNotFound(record.id))
    } yield record
  }

  override def acceptCredentialRequest(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestReceived)
      request <- ZIO
        .fromOption(record.requestCredentialData)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      issue = createDidCommIssueCredential(request)
      count <- credentialRepository
        .updateWithIssueCredential(recordId, issue, ProtocolState.CredentialPending)
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.startRecordingTime(
        s"${record.id}_issuance_flow_issuer_credential_pending_to_generated"
      )
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- credentialRepository
        .getIssueCredentialRecord(record.id)
        .mapError(RepositoryError.apply)
        .someOrFail(RecordIdNotFound(record.id))
    } yield record
  }

  override def receiveCredentialIssue(
      issueCredential: IssueCredential
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = for {
    // TODO Move this type of generic/reusable code to a helper trait
    record <- getRecordFromThreadIdWithState(
      issueCredential.thid.map(DidCommID(_)),
      ignoreWithZeroRetries = true,
      ProtocolState.RequestPending,
      ProtocolState.RequestSent
    )
    attachment <- ZIO
      .fromOption(issueCredential.attachments.headOption)
      .mapError(_ => CredentialServiceError.UnexpectedError("Missing attachment in credential issued credential"))

    _ <- {
      val result = attachment match {
        case AttachmentDescriptor(
              id,
              media_type,
              Base64(v),
              Some(IssueCredentialIssuedFormat.Anoncred.name),
              _,
              _,
              _,
              _
            ) =>
          for {
            processedCredential <- processAnonCredsCredential(record, java.util.Base64.getUrlDecoder.decode(v))
            attachment = AttachmentDescriptor.buildBase64Attachment(
              id = id,
              mediaType = media_type,
              format = Some(IssueCredentialIssuedFormat.Anoncred.name),
              payload = processedCredential.data.getBytes
            )
            processedIssuedCredential = issueCredential.copy(attachments = Seq(attachment))
            result <-
              updateWithCredential(
                processedIssuedCredential,
                record,
                attachment,
                Some(processedCredential.getSchemaId),
                Some(processedCredential.getCredDefId)
              )
          } yield result
        case attachment =>
          updateWithCredential(issueCredential, record, attachment, None, None)
      }
      result
    }
    record <- credentialRepository
      .getIssueCredentialRecord(record.id)
      .mapError(RepositoryError.apply)
      .someOrFail(RecordIdNotFound(record.id))
  } yield record

  private def updateWithCredential(
      issueCredential: IssueCredential,
      record: IssueCredentialRecord,
      attachment: AttachmentDescriptor,
      schemaId: Option[String],
      credDefId: Option[String]
  ) = {
    credentialRepository
      .updateWithIssuedRawCredential(
        record.id,
        issueCredential,
        attachment.data.asJson.noSpaces,
        schemaId,
        credDefId,
        ProtocolState.CredentialReceived
      )
      .flatMap {
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
      }
      .mapError(RepositoryError.apply)
  }

  private[this] def processAnonCredsCredential(
      record: IssueCredentialRecord,
      credentialBytes: Array[Byte]
  ): ZIO[WalletAccessContext, CredentialServiceError, anoncreds.AnoncredCredential] = {
    for {
      credential <- ZIO.succeed(anoncreds.AnoncredCredential(new String(credentialBytes)))
      credDefContent <- uriDereferencer
        .dereference(new URI(credential.getCredDefId))
        .mapError(err => UnexpectedError(err.toString))
      credentialDefinition = anoncreds.AnoncredCredentialDefinition(credDefContent)
      metadata <- ZIO
        .fromOption(record.anonCredsRequestMetadata)
        .mapError(_ => CredentialServiceError.UnexpectedError(s"No request metadata Id found un record: ${record.id}"))
      linkSecret <- linkSecretService
        .fetchOrCreate()
        .mapError(e => CredentialServiceError.LinkSecretError.apply(e.cause))
      credential <- ZIO
        .attempt(
          AnoncredLib.processCredential(
            anoncreds.AnoncredCredential(new String(credentialBytes)),
            metadata,
            linkSecret,
            credentialDefinition
          )
        )
        .mapError(error => UnexpectedError(s"AnonCreds credential processing error: ${error.getMessage}"))
    } yield credential
  }

  override def markOfferSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordProtocolState(
      recordId,
      IssueCredentialRecord.ProtocolState.OfferPending,
      IssueCredentialRecord.ProtocolState.OfferSent
    )

  override def markRequestSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordProtocolState(
      recordId,
      IssueCredentialRecord.ProtocolState.RequestGenerated,
      IssueCredentialRecord.ProtocolState.RequestSent
    ) @@ CustomMetricsAspect.endRecordingTime(
      s"${recordId}_issuance_flow_holder_req_generated_to_sent",
      "issuance_flow_holder_req_generated_to_sent_ms_gauge"
    )

  private[this] def markCredentialGenerated(
      record: IssueCredentialRecord,
      issueCredential: IssueCredential
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      count <- credentialRepository
        .updateWithIssueCredential(
          record.id,
          issueCredential,
          IssueCredentialRecord.ProtocolState.CredentialGenerated
        )
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.endRecordingTime(
        s"${record.id}_issuance_flow_issuer_credential_pending_to_generated",
        "issuance_flow_issuer_credential_pending_to_generated_ms_gauge"
      ) @@ CustomMetricsAspect.startRecordingTime(s"${record.id}_issuance_flow_issuer_credential_generated_to_sent")
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(record.id))
      record <- credentialRepository
        .getIssueCredentialRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }

    } yield record
  }

  override def markCredentialSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordProtocolState(
      recordId,
      IssueCredentialRecord.ProtocolState.CredentialGenerated,
      IssueCredentialRecord.ProtocolState.CredentialSent
    ) @@ CustomMetricsAspect.endRecordingTime(
      s"${recordId}_issuance_flow_issuer_credential_generated_to_sent",
      "issuance_flow_issuer_credential_generated_to_sent_ms_gauge"
    )

  override def reportProcessingFailure(
      recordId: DidCommID,
      failReason: Option[String]
  ): ZIO[WalletAccessContext, CredentialServiceError, Unit] =
    credentialRepository
      .updateAfterFail(recordId, failReason)
      .mapError(RepositoryError.apply)
      .flatMap {
        case 1 => ZIO.unit
        case n => ZIO.fail(UnexpectedError(s"Invalid number of records updated: $n"))
      }

  private[this] def getRecordWithState(
      recordId: DidCommID,
      state: ProtocolState
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      maybeRecord <- credentialRepository
        .getIssueCredentialRecord(recordId)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => RecordIdNotFound(recordId))
      _ <- record.protocolState match {
        case s if s == state => ZIO.unit
        case state           => ZIO.fail(InvalidFlowStateError(s"Invalid protocol state for operation: $state"))
      }
    } yield record
  }

  private[this] def getRecordFromThreadIdWithState(
      thid: Option[DidCommID],
      ignoreWithZeroRetries: Boolean,
      states: ProtocolState*
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      thid <- ZIO
        .fromOption(thid)
        .mapError(_ => UnexpectedError("No `thid` found in credential request"))
      maybeRecord <- credentialRepository
        .getIssueCredentialRecordByThreadId(thid, ignoreWithZeroRetries)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => ThreadIdNotFound(thid))
      _ <- record.protocolState match {
        case s if states.contains(s) => ZIO.unit
        case state                   => ZIO.fail(InvalidFlowStateError(s"Invalid protocol state for operation: $state"))
      }
    } yield record
  }

  private[this] def createJWTDidCommOfferCredential(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      maybeSchemaId: Option[String],
      claims: Seq[Attribute],
      thid: DidCommID,
      challenge: String,
      domain: String
  ) = {
    for {
      credentialPreview <- ZIO.succeed(CredentialPreview(schema_id = maybeSchemaId, attributes = claims))
      body = OfferCredential.Body(
        goal_code = Some("Offer Credential"),
        credential_preview = credentialPreview,
      )
      attachments <- ZIO.succeed(
        Seq(
          AttachmentDescriptor.buildJsonAttachment(
            mediaType = Some("application/json"),
            format = Some(IssueCredentialOfferFormat.JWT.name),
            payload = PresentationAttachment(
              Some(Options(challenge, domain)),
              PresentationDefinition(format = Some(ClaimFormat(jwt = Some(Jwt(alg = Seq("ES256K"), proof_type = Nil)))))
            )
          )
        )
      )
    } yield OfferCredential(
      body = body,
      attachments = attachments,
      from = pairwiseIssuerDID,
      to = pairwiseHolderDID,
      thid = Some(thid.value)
    )
  }

  private[this] def createAnonCredsDidCommOfferCredential(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      schemaUri: String,
      credentialDefinitionGUID: UUID,
      credentialDefinitionId: String,
      claims: Seq[Attribute],
      thid: DidCommID
  ) = {
    for {
      credentialPreview <- ZIO.succeed(CredentialPreview(schema_id = Some(schemaUri), attributes = claims))
      body = OfferCredential.Body(
        goal_code = Some("Offer Credential"),
        credential_preview = credentialPreview,
      )
      attachments <- createAnonCredsCredentialOffer(credentialDefinitionGUID, credentialDefinitionId).map { offer =>
        Seq(
          AttachmentDescriptor.buildBase64Attachment(
            mediaType = Some("application/json"),
            format = Some(IssueCredentialOfferFormat.Anoncred.name),
            payload = offer.data.getBytes()
          )
        )
      }
    } yield OfferCredential(
      body = body,
      attachments = attachments,
      from = pairwiseIssuerDID,
      to = pairwiseHolderDID,
      thid = Some(thid.value)
    )
  }

  private[this] def createAnonCredsCredentialOffer(credentialDefinitionGUID: UUID, credentialDefinitionId: String) =
    for {
      credentialDefinition <- credentialDefinitionService
        .getByGUID(credentialDefinitionGUID)
        .mapError(e => CredentialServiceError.UnexpectedError(e.toString))
      cd = anoncreds.AnoncredCredentialDefinition(credentialDefinition.definition.toString)
      kcp = anoncreds.AnoncredCredentialKeyCorrectnessProof(credentialDefinition.keyCorrectnessProof.toString)
      maybeCredentialDefinitionSecret <- genericSecretStorage
        .get[UUID, CredentialDefinitionSecret](credentialDefinition.guid)
        .orDie
      credentialDefinitionSecret <- ZIO
        .fromOption(maybeCredentialDefinitionSecret)
        .mapError(_ => CredentialServiceError.CredentialDefinitionPrivatePartNotFound(credentialDefinition.guid))
      cdp = anoncreds.AnoncredCredentialDefinitionPrivate(credentialDefinitionSecret.json.toString)
      createCredentialDefinition = AnoncredCreateCredentialDefinition(cd, cdp, kcp)
      offer = AnoncredLib.createOffer(createCredentialDefinition, credentialDefinitionId)
    } yield offer

  private[this] def createDidCommRequestCredential(
      format: IssueCredentialOfferFormat,
      offer: OfferCredential,
      signedPresentation: JWT
  ): RequestCredential = {
    RequestCredential(
      body = RequestCredential.Body(
        goal_code = offer.body.goal_code,
        comment = offer.body.comment,
      ),
      attachments = Seq(
        AttachmentDescriptor
          .buildBase64Attachment(
            mediaType = Some("application/json"),
            format = Some(format.name),
            // FIXME copy payload will probably not work for anoncreds!
            payload = signedPresentation.value.getBytes(),
          )
      ),
      thid = offer.thid.orElse(Some(offer.id)),
      from = offer.to,
      to = offer.from
    )
  }

  private[this] def createDidCommIssueCredential(request: RequestCredential): IssueCredential = {
    IssueCredential(
      body = IssueCredential.Body(
        goal_code = request.body.goal_code,
        comment = request.body.comment,
        replacement_id = None,
        more_available = None,
      ),
      attachments = Seq(), // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      thid = request.thid.orElse(Some(request.id)),
      from = request.to,
      to = request.from
    )
  }

  /** this is an auxiliary function.
    *
    * @note
    *   Between updating and getting the CredentialRecord back the CredentialRecord can be updated by other operations
    *   in the middle.
    *
    * TODO: this should be improved to behave exactly like atomic operation.
    */
  private[this] def updateCredentialRecordProtocolState(
      id: DidCommID,
      from: IssueCredentialRecord.ProtocolState,
      to: IssueCredentialRecord.ProtocolState
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- credentialRepository
        .updateCredentialRecordProtocolState(id, from, to)
        .mapError(RepositoryError.apply)
        .flatMap {
          case 0 =>
            credentialRepository
              .getIssueCredentialRecord(id)
              .mapError(RepositoryError.apply)
              .flatMap {
                case None => ZIO.fail(RecordIdNotFound(id))
                case Some(record) if record.protocolState == to => // Not update by is alredy on the desirable state
                  ZIO.succeed(record)
                case Some(record) =>
                  ZIO.fail(
                    OperationNotExecuted(
                      id,
                      s"CredentialRecord was not updated because have the ProtocolState ${record.protocolState}"
                    )
                  )
              }
          case 1 =>
            credentialRepository
              .getIssueCredentialRecord(id)
              .mapError(RepositoryError.apply)
              .flatMap {
                case None => ZIO.fail(RecordIdNotFound(id))
                case Some(record) =>
                  ZIO
                    .logError(
                      s"The CredentialRecord ($id) is expected to be on the ProtocolState '$to' after updating it"
                    )
                    .when(record.protocolState != to)
                    .as(record)
              }
          case n => ZIO.fail(UnexpectedError(s"Invalid row count result: $n"))
        }
    } yield record
  }

  override def generateJWTCredential(
      recordId: DidCommID,
      statusListRegistryUrl: String,
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.CredentialPending)
      issuingDID <- ZIO
        .fromOption(record.issuingDID)
        .mapError(_ => CredentialServiceError.UnexpectedError(s"Issuing Id not found in record: ${recordId.value}"))
      issue <- ZIO
        .fromOption(record.issueCredentialData)
        .mapError(_ =>
          CredentialServiceError.UnexpectedError(s"Issue credential data not found in record: ${recordId.value}")
        )
      longFormPrismDID <- getLongForm(issuingDID, true).mapError(err => UnexpectedError(err.getMessage))
      jwtIssuer <- createJwtIssuer(longFormPrismDID, VerificationRelationship.AssertionMethod)
      offerCredentialData <- ZIO
        .fromOption(record.offerCredentialData)
        .mapError(_ =>
          CredentialServiceError.CreateCredentialPayloadFromRecordError(
            new Throwable("Could not extract claims from \"requestCredential\" DIDComm message")
          )
        )
      preview = offerCredentialData.body.credential_preview
      claims <- CredentialService.convertAttributesToJsonClaims(preview.body.attributes)
      maybeOfferOptions <- getOptionsFromOfferCredentialData(record)
      requestJwt <- getJwtFromRequestCredentialData(record)

      // domain/challenge validation + JWT verification
      jwtPresentation <- validateRequestCredentialDataProof(maybeOfferOptions, requestJwt).tapBoth(
        error =>
          ZIO.logErrorCause("JWT Presentation Validation Failed!!", Cause.fail(error)) *> credentialRepository
            .updateCredentialRecordProtocolState(
              record.id,
              ProtocolState.CredentialPending,
              ProtocolState.ProblemReportPending
            )
            .mapError(t => RepositoryError(t)),
        payload => ZIO.logInfo("JWT Presentation Validation Successful!")
      )
      issuanceDate = Instant.now()
      credentialStatus <- allocateNewCredentialInStatusListForWallet(record, statusListRegistryUrl, jwtIssuer)
      // TODO: get schema when schema registry is available if schema ID is provided
      w3Credential = W3cCredentialPayload(
        `@context` = Set(
          "https://www.w3.org/2018/credentials/v1"
        ), // TODO: his information should come from Schema registry by record.schemaId
        maybeId = None,
        `type` =
          Set("VerifiableCredential"), // TODO: This information should come from Schema registry by record.schemaId
        issuer = jwtIssuer.did,
        issuanceDate = issuanceDate,
        maybeExpirationDate = record.validityPeriod.map(sec => issuanceDate.plusSeconds(sec.toLong)),
        maybeCredentialSchema =
          record.schemaUri.map(id => io.iohk.atala.pollux.vc.jwt.CredentialSchema(id, VC_JSON_SCHEMA_TYPE)),
        maybeCredentialStatus = Some(credentialStatus),
        credentialSubject = claims.add("id", jwtPresentation.iss.asJson).asJson,
        maybeRefreshService = None,
        maybeEvidence = None,
        maybeTermsOfUse = None
      )
      signedJwtCredential = W3CCredential.toEncodedJwt(w3Credential, jwtIssuer)
      issueCredential = IssueCredential.build(
        fromDID = issue.from,
        toDID = issue.to,
        thid = issue.thid,
        credentials = Seq(IssueCredentialIssuedFormat.JWT -> signedJwtCredential.value.getBytes)
      )
      record <- markCredentialGenerated(record, issueCredential)
    } yield record
  }

  private[this] def allocateNewCredentialInStatusListForWallet(
      record: IssueCredentialRecord,
      statusListRegistryUrl: String,
      jwtIssuer: JwtIssuer
  ): ZIO[WalletAccessContext, CredentialServiceError, CredentialStatus] = {
    val effect = for {
      lastStatusList <- credentialStatusListRepository.getLatestOfTheWallet.mapError(RepositoryError.apply)
      currentStatusList <- lastStatusList
        .fold(credentialStatusListRepository.createNewForTheWallet(jwtIssuer, statusListRegistryUrl))(
          ZIO.succeed(_)
        )
        .mapError(RepositoryError.apply)
      size = currentStatusList.size
      lastUsedIndex = currentStatusList.lastUsedIndex
      statusListToBeUsed <-
        if lastUsedIndex < size then ZIO.succeed(currentStatusList)
        else
          credentialStatusListRepository
            .createNewForTheWallet(jwtIssuer, statusListRegistryUrl)
            .mapError(RepositoryError.apply)
      _ <- credentialStatusListRepository
        .allocateSpaceForCredential(
          issueCredentialRecordId = record.id,
          credentialStatusListId = statusListToBeUsed.id,
          statusListIndex = statusListToBeUsed.lastUsedIndex + 1
        )
        .mapError(RepositoryError.apply)
    } yield CredentialStatus(
      id = s"$statusListRegistryUrl/credential-status/${statusListToBeUsed.id}#${statusListToBeUsed.lastUsedIndex + 1}",
      `type` = "StatusList2021Entry",
      statusPurpose = StatusPurpose.Revocation,
      statusListIndex = lastUsedIndex + 1,
      statusListCredential = s"$statusListRegistryUrl/credential-status/${statusListToBeUsed.id}"
    )
    issueCredentialSem.withPermit(effect)
  }

  override def generateAnonCredsCredential(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.CredentialPending)
      requestCredential <- ZIO
        .fromOption(record.requestCredentialData)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: ${record.id}"))
      body = IssueCredential.Body(goal_code = Some("Issue Credential"))
      attachments <- createAnonCredsCredential(record).map { credential =>
        Seq(
          AttachmentDescriptor.buildBase64Attachment(
            mediaType = Some("application/json"),
            format = Some(IssueCredentialIssuedFormat.Anoncred.name),
            payload = credential.data.getBytes()
          )
        )
      }
      issueCredential = IssueCredential(
        body = body,
        attachments = attachments,
        from = requestCredential.to,
        to = requestCredential.from,
        thid = requestCredential.thid
      )
      record <- markCredentialGenerated(record, issueCredential)
    } yield record
  }

  private[this] def createAnonCredsCredential(record: IssueCredentialRecord) = {
    for {
      credentialDefinitionId <- ZIO
        .fromOption(record.credentialDefinitionId)
        .mapError(_ => CredentialServiceError.UnexpectedError(s"No cred def Id found un record: ${record.id}"))
      credentialDefinition <- credentialDefinitionService
        .getByGUID(credentialDefinitionId)
        .mapError(e => CredentialServiceError.UnexpectedError(e.toString))
      cd = anoncreds.AnoncredCredentialDefinition(credentialDefinition.definition.toString)
      offerCredential <- ZIO
        .fromOption(record.offerCredentialData)
        .mapError(_ => InvalidFlowStateError(s"No offer found for this record: ${record.id}"))
      offerCredentialAttachmentData <- ZIO
        .fromOption(
          offerCredential.attachments
            .find(_.format.contains(IssueCredentialOfferFormat.Anoncred.name))
            .map(_.data)
            .flatMap {
              case Base64(value) => Some(new String(java.util.Base64.getUrlDecoder.decode(value)))
              case _             => None
            }
        )
        .mapError(_ => InvalidFlowStateError(s"No AnonCreds offer attachment found"))
      credentialOffer = anoncreds.AnoncredCredentialOffer(offerCredentialAttachmentData)
      requestCredential <- ZIO
        .fromOption(record.requestCredentialData)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: ${record.id}"))
      requestCredentialAttachmentData <- ZIO
        .fromOption(
          requestCredential.attachments
            .find(_.format.contains(IssueCredentialRequestFormat.Anoncred.name))
            .map(_.data)
            .flatMap {
              case Base64(value) => Some(new String(java.util.Base64.getUrlDecoder.decode(value)))
              case _             => None
            }
        )
        .mapError(_ => InvalidFlowStateError(s"No AnonCreds request attachment found"))
      credentialRequest = anoncreds.AnoncredCredentialRequest(requestCredentialAttachmentData)
      attrValues = offerCredential.body.credential_preview.body.attributes.map { attr =>
        (attr.name, attr.value)
      }
      maybeCredentialDefinitionSecret <- genericSecretStorage
        .get[UUID, CredentialDefinitionSecret](credentialDefinition.guid)
        .orDie
      credentialDefinitionSecret <- ZIO
        .fromOption(maybeCredentialDefinitionSecret)
        .mapError(_ => CredentialServiceError.CredentialDefinitionPrivatePartNotFound(credentialDefinition.guid))
      cdp = anoncreds.AnoncredCredentialDefinitionPrivate(credentialDefinitionSecret.json.toString)
      credential =
        AnoncredLib.createCredential(
          cd,
          cdp,
          credentialOffer,
          credentialRequest,
          attrValues
        )
    } yield credential
  }

  private[this] def getOptionsFromOfferCredentialData(record: IssueCredentialRecord) = {
    for {
      offer <- ZIO
        .fromOption(record.offerCredentialData)
        .mapError(_ => CredentialServiceError.UnexpectedError(s"Offer data not found in record: ${record.id}"))
      attachmentDescriptor <- ZIO
        .fromOption(offer.attachments.headOption)
        .mapError(_ => UnexpectedError(s"Attachments not found in record: ${record.id}"))
      json <- attachmentDescriptor.data match
        case JsonData(json) => ZIO.succeed(json.asJson)
        case _              => ZIO.fail(UnexpectedError(s"Attachment doesn't contain JsonData: ${record.id}"))
      maybeOptions <- ZIO
        .fromEither(json.as[PresentationAttachment].map(_.options))
        .mapError(df => UnexpectedError(df.getMessage))
    } yield maybeOptions
  }

  private[this] def getJwtFromRequestCredentialData(record: IssueCredentialRecord) = {
    for {
      request <- ZIO
        .fromOption(record.requestCredentialData)
        .mapError(_ => CredentialServiceError.UnexpectedError(s"Request data not found in record: ${record.id}"))
      attachmentDescriptor <- ZIO
        .fromOption(request.attachments.headOption)
        .mapError(_ => UnexpectedError(s"Attachments not found in record: ${record.id}"))
      jwt <- attachmentDescriptor.data match
        case Base64(b64) =>
          ZIO.succeed {
            val base64Decoded = new String(java.util.Base64.getDecoder().decode(b64))
            JWT(base64Decoded)
          }
        case _ => ZIO.fail(UnexpectedError(s"Attachment doesn't contain Base64Data: ${record.id}"))
    } yield jwt
  }

  private[this] def validateRequestCredentialDataProof(maybeOptions: Option[Options], jwt: JWT) = {
    for {
      _ <- maybeOptions match
        case None => ZIO.unit
        case Some(options) =>
          JwtPresentation.validatePresentation(jwt, options.domain, options.challenge) match
            case ZValidation.Success(log, value) => ZIO.unit
            case ZValidation.Failure(log, error) =>
              ZIO.fail(CredentialRequestValidationError("JWT presentation domain/validation validation failed"))

      clock = java.time.Clock.system(ZoneId.systemDefault)

      genericUriResolver = GenericUriResolver(
        Map(
          "data" -> DataUrlResolver(),
        )
      )
      verificationResult <- JwtPresentation
        .verify(
          jwt,
          JwtPresentation.PresentationVerificationOptions(
            maybeProofPurpose = Some(VerificationRelationship.Authentication),
            verifySignature = true,
            verifyDates = false,
            leeway = Duration.Zero
          )
        )(didResolver, genericUriResolver)(clock)
        .mapError(errors => CredentialRequestValidationError(s"JWT presentation verification failed: $errors"))

      result <- verificationResult match
        case ZValidation.Success(log, value) => ZIO.unit
        case ZValidation.Failure(log, error) =>
          ZIO.fail(CredentialRequestValidationError(s"JWT presentation verification failed: $error"))

      jwtPresentation <- ZIO
        .fromTry(JwtPresentation.decodeJwt(jwt))
        .mapError(t => CredentialRequestValidationError(s"JWT presentation decoding failed: ${t.getMessage()}"))
    } yield jwtPresentation
  }

}
