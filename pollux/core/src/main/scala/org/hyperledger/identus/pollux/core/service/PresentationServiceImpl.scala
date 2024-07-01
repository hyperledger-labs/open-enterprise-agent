package org.hyperledger.identus.pollux.core.service

import cats.*
import cats.implicits.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.hyperledger.identus.mercury.model.*
import org.hyperledger.identus.mercury.protocol.issuecredential.IssueCredentialIssuedFormat
import org.hyperledger.identus.mercury.protocol.presentproof.*
import org.hyperledger.identus.pollux.anoncreds.*
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.model.error.PresentationError
import org.hyperledger.identus.pollux.core.model.error.PresentationError.*
import org.hyperledger.identus.pollux.core.model.presentation.*
import org.hyperledger.identus.pollux.core.model.schema.`type`.anoncred.AnoncredSchemaSerDesV1
import org.hyperledger.identus.pollux.core.repository.{CredentialRepository, PresentationRepository}
import org.hyperledger.identus.pollux.core.service.serdes.*
import org.hyperledger.identus.pollux.sdjwt.{CredentialCompact, HolderPrivateKey, PresentationCompact, SDJWT}
import org.hyperledger.identus.pollux.vc.jwt.*
import org.hyperledger.identus.shared.models.*
import org.hyperledger.identus.shared.utils.aspects.CustomMetricsAspect
import zio.*
import zio.json.*

import java.net.URI
import java.time.Instant
import java.util.{Base64 as JBase64, UUID}
import java.util as ju
import scala.util.chaining.*
import scala.util.Try

private class PresentationServiceImpl(
    uriDereferencer: URIDereferencer,
    linkSecretService: LinkSecretService,
    presentationRepository: PresentationRepository,
    credentialRepository: CredentialRepository,
    maxRetries: Int = 5, // TODO move to config
) extends PresentationService {

  import PresentationRecord.*

  override def markPresentationGenerated(
      recordId: DidCommID,
      presentation: Presentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.PresentationPending)
      count <-
        presentationRepository
          .updateWithPresentation(recordId, presentation, ProtocolState.PresentationGenerated)
          @@ CustomMetricsAspect.endRecordingTime(
            s"${record.id}_present_proof_flow_prover_presentation_pending_to_generated_ms_gauge",
            "present_proof_flow_prover_presentation_pending_to_generated_ms_gauge"
          ) @@ CustomMetricsAspect.startRecordingTime(
            s"${record.id}_present_proof_flow_prover_presentation_generated_to_sent_ms_gauge"
          )
      record <- getRecord(recordId)
    } yield record
  }

  override def createJwtPresentationPayloadFromRecord(
      recordId: DidCommID,
      prover: Issuer,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, PresentationPayload] = {

    for {
      record <- getRecord(recordId)
      credentialsToUse <- ZIO
        .fromOption(record.credentialsToUse)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      requestPresentation <- ZIO
        .fromOption(record.requestPresentationData)
        .mapError(_ => InvalidFlowStateError(s"RequestPresentation not found: $recordId"))
      issuedValidCredentials <- credentialRepository
        .findValidIssuedCredentials(credentialsToUse.map(DidCommID(_)))
      signedCredentials = issuedValidCredentials.flatMap(_.issuedCredentialRaw)
      issuedCredentials <- ZIO.fromEither(
        Either.cond(
          signedCredentials.nonEmpty,
          signedCredentials,
          PresentationError.IssuedCredentialNotFoundError(
            "No matching issued credentials found in prover db"
          )
        )
      )
      presentationPayload <- createJwtPresentationPayloadFromCredential(
        issuedCredentials,
        requestPresentation,
        prover
      )
    } yield presentationPayload
  }

  private def createSDJwtPresentationFromRecord(
      recordId: DidCommID,
      optionalHolderPrivateKey: Option[HolderPrivateKey]
  ): ZIO[WalletAccessContext, PresentationError, PresentationCompact] = {

    for {
      record <- getRecord(recordId)
      credentialsToUse <- ZIO
        .fromOption(record.credentialsToUse)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      sdJwtClaimsToDisclose <- ZIO
        .fromOption(record.sdJwtClaimsToDisclose)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      requestPresentation <- ZIO
        .fromOption(record.requestPresentationData)
        .mapError(_ => InvalidFlowStateError(s"RequestPresentation not found: $recordId"))
      issuedValidCredentials <- credentialRepository
        .findValidIssuedCredentials(credentialsToUse.map(DidCommID(_)))
      signedCredentials = issuedValidCredentials.flatMap(_.issuedCredentialRaw)

      issuedCredentials <- ZIO.fromEither(
        Either.cond(
          signedCredentials.nonEmpty,
          signedCredentials,
          PresentationError.IssuedCredentialNotFoundError(
            "No matching issued credentials found in prover db"
          )
        )
      )

      presentationCompact <- createPresentationFromIssuedCredential(
        issuedCredentials,
        sdJwtClaimsToDisclose,
        requestPresentation,
        optionalHolderPrivateKey
      )
      presentationPayload <- ZIO.succeed(presentationCompact)

    } yield presentationCompact
  }
//This can be private remove
  override def createPresentationFromRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationCompact] = {

    for {
      record <- getRecord(recordId)
      credentialsToUse <- ZIO
        .fromOption(record.credentialsToUse)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      sdJwtClaimsToDisclose <- ZIO
        .fromOption(record.sdJwtClaimsToDisclose)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      requestPresentation <- ZIO
        .fromOption(record.requestPresentationData)
        .mapError(_ => InvalidFlowStateError(s"RequestPresentation not found: $recordId"))
      issuedValidCredentials <- credentialRepository
        .findValidIssuedCredentials(credentialsToUse.map(DidCommID(_)))
      signedCredentials = issuedValidCredentials.flatMap(_.issuedCredentialRaw)

      issuedCredentials <- ZIO.fromEither(
        Either.cond(
          signedCredentials.nonEmpty,
          signedCredentials,
          PresentationError.IssuedCredentialNotFoundError(
            "No matching issued credentials found in prover db"
          )
        )
      )

      presentationCompact <- createPresentationFromIssuedCredential(
        issuedCredentials,
        sdJwtClaimsToDisclose,
        requestPresentation,
        None
      )
      presentationPayload <- ZIO.succeed(presentationCompact)

    } yield presentationCompact
  }

  override def createSDJwtPresentation(
      recordId: DidCommID,
      requestPresentation: RequestPresentation,
      optionalHolderPrivateKey: Option[HolderPrivateKey],
  ): ZIO[WalletAccessContext, PresentationError, Presentation] = {
    for {
      presentationPayload <- createSDJwtPresentationFromRecord(recordId, optionalHolderPrivateKey)
      presentation <- ZIO.succeed(
        Presentation(
          body = Presentation.Body(
            goal_code = requestPresentation.body.goal_code,
            comment = requestPresentation.body.comment
          ),
          attachments = Seq(
            AttachmentDescriptor
              .buildBase64Attachment(
                payload = presentationPayload.compact.getBytes,
                mediaType = Some(PresentCredentialFormat.SDJWT.name)
              )
          ),
          thid = requestPresentation.thid.orElse(Some(requestPresentation.id)),
          from = requestPresentation.to,
          to = requestPresentation.from
        )
      )
    } yield presentation

  }

  override def createAnoncredPresentationPayloadFromRecord(
      recordId: DidCommID,
      anoncredCredentialProof: AnoncredCredentialProofsV1,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, AnoncredPresentation] = {

    for {
      record <- getRecord(recordId)
      requestPresentation <- ZIO
        .fromOption(record.requestPresentationData)
        .mapError(_ => InvalidFlowStateError(s"RequestPresentation not found: $recordId"))
      issuedValidCredentials <-
        credentialRepository
          .findValidAnonCredsIssuedCredentials(
            anoncredCredentialProof.credentialProofs.map(credentialProof => DidCommID(credentialProof.credential))
          )
      issuedCredentials <- ZIO.fromEither(
        Either.cond(
          issuedValidCredentials.nonEmpty,
          issuedValidCredentials,
          PresentationError.IssuedCredentialNotFoundError(
            "No matching issued credentials found in prover db"
          )
        )
      )
      presentationPayload <- createAnoncredPresentationPayloadFromCredential(
        issuedCredentials,
        issuedValidCredentials.flatMap(_.schemaUri),
        issuedValidCredentials.flatMap(_.credentialDefinitionUri),
        requestPresentation,
        anoncredCredentialProof.credentialProofs
      )
    } yield presentationPayload
  }

  def createAnoncredPresentation(
      requestPresentation: RequestPresentation,
      recordId: DidCommID,
      anoncredCredentialProof: AnoncredCredentialProofsV1,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, Presentation] = {
    for {
      presentationPayload <-
        createAnoncredPresentationPayloadFromRecord(
          recordId,
          anoncredCredentialProof,
          issuanceDate
        )
      presentation <- ZIO.succeed(
        Presentation(
          body = Presentation.Body(
            goal_code = requestPresentation.body.goal_code,
            comment = requestPresentation.body.comment
          ),
          attachments = Seq(
            AttachmentDescriptor
              .buildBase64Attachment(
                payload = presentationPayload.data.getBytes(),
                mediaType = Some(PresentCredentialFormat.Anoncred.name),
                format = Some(PresentCredentialFormat.Anoncred.name),
              )
          ),
          thid = requestPresentation.thid.orElse(Some(requestPresentation.id)),
          from = requestPresentation.to,
          to = requestPresentation.from
        )
      )
    } yield presentation
  }

  override def extractIdFromCredential(credential: W3cCredentialPayload): Option[UUID] =
    credential.maybeId.map(_.split("/").last).map(UUID.fromString)

  override def getPresentationRecords(
      ignoreWithZeroRetries: Boolean
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]] = presentationRepository
    .getPresentationRecords(ignoreWithZeroRetries)

  override def findPresentationRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] =
    presentationRepository.findPresentationRecord(recordId)

  override def findPresentationRecordByThreadId(
      thid: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] =
    presentationRepository.findPresentationRecordByThreadId(thid)

  override def rejectRequestPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    markRequestPresentationRejected(recordId)
  }

  def rejectPresentation(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    markPresentationRejected(recordId)
  }

  override def createJwtPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      options: Option[org.hyperledger.identus.pollux.core.model.presentation.Options]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    createPresentationRecord(
      pairwiseVerifierDID,
      pairwiseProverDID,
      thid,
      connectionId,
      CredentialFormat.JWT,
      proofTypes,
      options.map(o => Seq(toJWTAttachment(o))).getOrElse(Seq.empty)
    )
  }

  override def createSDJWTPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      claimsToDisclose: ast.Json.Obj,
      options: Option[org.hyperledger.identus.pollux.core.model.presentation.Options],
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    createPresentationRecord(
      pairwiseVerifierDID,
      pairwiseProverDID,
      thid,
      connectionId,
      CredentialFormat.SDJWT,
      proofTypes,
      attachments = Seq(toSDJWTAttachment(options, claimsToDisclose))
    )
  }

  override def createAnoncredPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      presentationRequest: AnoncredPresentationRequestV1
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    createPresentationRecord(
      pairwiseVerifierDID,
      pairwiseProverDID,
      thid,
      connectionId,
      CredentialFormat.AnonCreds,
      Seq.empty,
      Seq(toAnoncredAttachment(presentationRequest))
    )
  }

  private def createPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      format: CredentialFormat,
      proofTypes: Seq[ProofType],
      attachments: Seq[AttachmentDescriptor]
  ) = {
    for {
      request <- ZIO.succeed(
        createDidCommRequestPresentation(
          proofTypes,
          thid,
          pairwiseVerifierDID,
          pairwiseProverDID,
          attachments
        )
      )
      record <- ZIO.succeed(
        PresentationRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = thid,
          connectionId = connectionId,
          schemaId = None, // TODO REMOVE from DB
          role = PresentationRecord.Role.Verifier,
          subjectId = pairwiseProverDID,
          protocolState = PresentationRecord.ProtocolState.RequestPending,
          credentialFormat = format,
          requestPresentationData = Some(request),
          proposePresentationData = None,
          presentationData = None,
          credentialsToUse = None,
          anoncredCredentialsToUseJsonSchemaId = None,
          anoncredCredentialsToUse = None,
          sdJwtClaimsToUseJsonSchemaId = None,
          sdJwtClaimsToDisclose = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      _ <- presentationRepository
        .createPresentationRecord(record)
        @@ CustomMetricsAspect.startRecordingTime(
          s"${record.id}_present_proof_flow_verifier_req_pending_to_sent_ms_gauge"
        )
    } yield record
  }

  override def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]] =
    presentationRepository
      .getPresentationRecordsByStates(ignoreWithZeroRetries, limit, states*)

  override def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): IO[PresentationError, Seq[PresentationRecord]] =
    presentationRepository
      .getPresentationRecordsByStatesForAllWallets(ignoreWithZeroRetries, limit, states*)

  override def receiveRequestPresentation(
      connectionId: Option[String],
      request: RequestPresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      format <- request.attachments match {
        case Seq() => ZIO.fail(PresentationError.MissingCredential)
        case Seq(head) =>
          val jsonF = PresentCredentialRequestFormat.JWT.name // stable identifier
          val anoncredF = PresentCredentialRequestFormat.Anoncred.name // stable identifier
          val sdjwtF = PresentCredentialRequestFormat.SDJWT.name // stable identifier

          head.format match
            case None           => ZIO.fail(PresentationError.MissingCredentialFormat)
            case Some(`jsonF`)  => ZIO.succeed(CredentialFormat.JWT)
            case Some(`sdjwtF`) => ZIO.succeed(CredentialFormat.SDJWT)
            case Some(`anoncredF`) =>
              head.data match
                case Base64(data) =>
                  val decodedData = new String(JBase64.getUrlDecoder.decode(data))
                  AnoncredPresentationRequestV1.schemaSerDes
                    .validate(decodedData)
                    .map(_ => CredentialFormat.AnonCreds)
                    .mapError(error => InvalidAnoncredPresentationRequest(error.error))
                case _ => ZIO.fail(InvalidAnoncredPresentationRequest("Expecting Base64-encoded data"))
            case Some(unsupportedFormat) => ZIO.fail(PresentationError.UnsupportedCredentialFormat(unsupportedFormat))
        case _ => ZIO.fail(PresentationError.RequestPresentationHasMultipleAttachment(request.id))
      }
      record <- ZIO.succeed(
        PresentationRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = DidCommID(request.thid.getOrElse(request.id)),
          connectionId = connectionId,
          schemaId = None,
          role = Role.Prover,
          subjectId = request.to,
          protocolState = PresentationRecord.ProtocolState.RequestReceived,
          credentialFormat = format,
          requestPresentationData = Some(request),
          proposePresentationData = None,
          presentationData = None,
          credentialsToUse = None,
          anoncredCredentialsToUseJsonSchemaId = None,
          anoncredCredentialsToUse = None,
          sdJwtClaimsToUseJsonSchemaId = None,
          sdJwtClaimsToDisclose = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      _ <- presentationRepository.createPresentationRecord(record)
    } yield record
  }

  private def createPresentationFromIssuedCredential(
      issuedCredentials: Seq[String],
      claimsToDisclose: SdJwtCredentialToDisclose,
      requestPresentation: RequestPresentation,
      optionalHolderPrivateKey: Option[HolderPrivateKey],
  ): IO[PresentationError, PresentationCompact] = {

    val verifiableCredentials: Either[
      PresentationError.PresentationDecodingError,
      Seq[CredentialCompact]
    ] = issuedCredentials.map { signedCredential =>
      decode[org.hyperledger.identus.mercury.model.Base64](signedCredential)
        .flatMap(x =>
          Right(CredentialCompact.unsafeFromCompact(new String(java.util.Base64.getUrlDecoder.decode(x.base64))))
        )
        .left
        .map(err => PresentationDecodingError(s"JsonData decoding error: $err"))
    }.sequence

    import io.circe.parser.decode
    import io.circe.syntax.*

    import java.util.Base64

    val result: Either[PresentationDecodingError, SDJwtPresentation] =
      requestPresentation.attachments.headOption
        .map(attachment =>
          decode[org.hyperledger.identus.mercury.model.Base64](attachment.data.asJson.noSpaces)
            .leftMap(err => PresentationDecodingError(s"PresentationAttachment decoding error: $err"))
            .flatMap { base64 =>
              org.hyperledger.identus.pollux.core.service.serdes.SDJwtPresentation.given_JsonDecoder_SDJwtPresentation
                .decodeJson(new String(Base64.getUrlDecoder.decode(base64.base64)))
                .leftMap(err => PresentationDecodingError(s"SDJwtPresentation decoding error: $err"))
            }
        )
        .getOrElse(Left(PresentationDecodingError("Error: No attachment found for SDJwtPresentation")))

    for {
      sdJwtPresentation <- ZIO.fromEither(result)
      vcs <- ZIO.fromEither(verifiableCredentials)
      vc <- ZIO
        .fromOption(vcs.headOption)
        .orElseFail(MissingCredential)
      iss <- ZIO.fromEither(vc.iss).mapError(error => PresentationDecodingError(s"Error: IssuedCredentials $error"))
      sub = vc.sub.toOption // optional
      iat = vc.iat.toOption // optional
      exp <- ZIO.fromEither(vc.exp).mapError(error => PresentationDecodingError(s"Error: IssuedCredentials $error"))
      claimsObject <- ZIO
        .fromOption(claimsToDisclose.asObject)
        .mapError(error => PresentationDecodingError(s"Error: IssuedCredentials claimsToDisclose must be a Json Obj"))
      sdJwtClaimsToDisclose = claimsObject
        .add("iss", ast.Json.Str(iss))
        .pipe(o => sub.map(sub => o.add("sub", ast.Json.Str(sub))).getOrElse(o)) // optional
        .pipe(o => iat.map(iat => o.add("iat", ast.Json.Num(iat))).getOrElse(o)) // optional
        .add("exp", ast.Json.Num(exp))
      presentationPayload = (sdJwtPresentation.options, optionalHolderPrivateKey) match {
        case (Some(options), Some(holderPrivateKey)) =>
          SDJWT.createPresentation(
            vc,
            sdJwtClaimsToDisclose.toJson,
            options.challenge,
            options.domain,
            holderPrivateKey
          )
        case _ => SDJWT.createPresentation(vc, sdJwtClaimsToDisclose.toJson)
      }
    } yield presentationPayload
  }

  /** All credentials MUST be of the same format */
  private def createJwtPresentationPayloadFromCredential(
      issuedCredentials: Seq[String],
      requestPresentation: RequestPresentation,
      prover: Issuer
  ): IO[PresentationError, PresentationPayload] = {

    val verifiableCredentials: Either[
      PresentationError.PresentationDecodingError,
      Seq[JwtVerifiableCredentialPayload]
    ] =
      issuedCredentials.map { signedCredential =>
        decode[org.hyperledger.identus.mercury.model.Base64](signedCredential)
          .flatMap(x => Right(new String(java.util.Base64.getUrlDecoder.decode(x.base64))))
          .flatMap(x => Right(JwtVerifiableCredentialPayload(JWT(x))))
          .left
          .map(err => PresentationDecodingError(s"JsonData decoding error: $err"))
      }.sequence

    val maybePresentationOptions
        : Either[PresentationError, Option[org.hyperledger.identus.pollux.core.model.presentation.Options]] =
      requestPresentation.attachments.headOption
        .map(attachment =>
          decode[org.hyperledger.identus.mercury.model.JsonData](attachment.data.asJson.noSpaces)
            .flatMap(data =>
              org.hyperledger.identus.pollux.core.model.presentation.PresentationAttachment.given_Decoder_PresentationAttachment
                .decodeJson(data.json.asJson)
                .map(_.options)
                .leftMap(err => PresentationDecodingError(s"PresentationAttachment decoding error: $err"))
            )
            .leftMap(err => PresentationDecodingError(s"JsonData decoding error: $err"))
        )
        .getOrElse(Right(None))

    for {
      maybeOptions <- ZIO.fromEither(maybePresentationOptions)
      vcs <- ZIO.fromEither(verifiableCredentials)
      presentationPayload <-
        ZIO.succeed(
          maybeOptions
            .map { options =>
              W3cPresentationPayload(
                `@context` = Vector("https://www.w3.org/2018/presentations/v1"),
                maybeId = None,
                `type` = Vector("VerifiablePresentation"),
                verifiableCredential = vcs.toVector,
                holder = prover.did.value,
                verifier = Vector(options.domain),
                maybeIssuanceDate = None,
                maybeExpirationDate = None
              ).toJwtPresentationPayload.copy(maybeNonce = Some(options.challenge))
            }
            .getOrElse {
              W3cPresentationPayload(
                `@context` = Vector("https://www.w3.org/2018/presentations/v1"),
                maybeId = None,
                `type` = Vector("VerifiablePresentation"),
                verifiableCredential = vcs.toVector,
                holder = prover.did.value,
                verifier = Vector("https://example.verifier"), // TODO Fix this
                maybeIssuanceDate = None,
                maybeExpirationDate = None
              ).toJwtPresentationPayload
            }
        )
    } yield presentationPayload
  }

  private case class AnoncredCredentialProof(
      credential: String,
      requestedAttribute: Seq[String],
      requestedPredicate: Seq[String]
  )

  private def createAnoncredPresentationPayloadFromCredential(
      issuedCredentialRecords: Seq[ValidFullIssuedCredentialRecord],
      schemaIds: Seq[String],
      credentialDefinitionIds: Seq[String],
      requestPresentation: RequestPresentation,
      credentialProofs: List[AnoncredCredentialProofV1],
  ): ZIO[WalletAccessContext, PresentationError, AnoncredPresentation] = {
    for {
      schemaMap <-
        ZIO
          .collectAll(schemaIds.map { schemaUri =>
            resolveSchema(schemaUri)
          })
          .map(_.toMap)
      credentialDefinitionMap <-
        ZIO
          .collectAll(credentialDefinitionIds.map { credentialDefinitionUri =>
            resolveCredentialDefinition(credentialDefinitionUri)
          })
          .map(_.toMap)
      credentialProofsMap = credentialProofs.map(credentialProof => (credentialProof.credential, credentialProof)).toMap
      verifiableCredentials <-
        ZIO.collectAll(
          issuedCredentialRecords
            .flatMap(issuedCredentialRecord => {
              issuedCredentialRecord.issuedCredential
                .map(issuedCredential =>
                  issuedCredential.attachments
                    .filter(attachment => attachment.format.contains(IssueCredentialIssuedFormat.Anoncred.name))
                    .map(_.data)
                    .map {
                      case Base64(data) =>
                        Right(
                          AnoncredCredentialProof(
                            new String(JBase64.getUrlDecoder.decode(data)),
                            credentialProofsMap(issuedCredentialRecord.id.value).requestedAttribute,
                            credentialProofsMap(issuedCredentialRecord.id.value).requestedPredicate
                          )
                        )
                      case _ => Left(InvalidAnoncredPresentationRequest("Expecting Base64-encoded data"))
                    }
                    .map(ZIO.fromEither(_))
                )
                .toSeq
                .flatten
            })
        )
      presentationRequestAttachment <- ZIO.fromEither(
        requestPresentation.attachments.headOption.toRight(InvalidAnoncredPresentationRequest("Missing Presentation"))
      )
      presentationRequestData <-
        presentationRequestAttachment.data match
          case Base64(data) => ZIO.succeed(new String(JBase64.getUrlDecoder.decode(data)))
          case _            => ZIO.fail(InvalidAnoncredPresentationRequest("Expecting Base64-encoded data"))
      _ <-
        AnoncredPresentationRequestV1.schemaSerDes
          .deserialize(presentationRequestData)
          .mapError(error => InvalidAnoncredPresentationRequest(error.error))
      linkSecret <-
        linkSecretService
          .fetchOrCreate()
          .map(_.secret)
      credentialRequest =
        verifiableCredentials.map(verifiableCredential =>
          AnoncredCredentialRequests(
            AnoncredCredential(verifiableCredential.credential),
            verifiableCredential.requestedAttribute,
            verifiableCredential.requestedPredicate
          )
        )
      presentation <-
        ZIO
          .fromEither(
            AnoncredLib.createPresentation(
              AnoncredPresentationRequest(presentationRequestData),
              credentialRequest,
              Map.empty, // TO FIX
              linkSecret,
              schemaMap,
              credentialDefinitionMap
            )
          )
          .mapError((t: Throwable) => AnoncredPresentationCreationError(t))
    } yield presentation
  }

  private def resolveSchema(schemaUri: String): IO[PresentationError, (String, AnoncredSchemaDef)] = {
    for {
      uri <- ZIO.attempt(new URI(schemaUri)).mapError(e => InvalidSchemaURIError(schemaUri, e))
      content <- uriDereferencer.dereference(uri).mapError(e => SchemaURIDereferencingError(e))
      anoncredSchema <-
        AnoncredSchemaSerDesV1.schemaSerDes
          .deserialize(content)
          .mapError(error => AnoncredPresentationParsingError(error))
      anoncredLibSchema =
        AnoncredSchemaDef(
          schemaUri,
          anoncredSchema.version,
          anoncredSchema.attrNames,
          anoncredSchema.issuerId
        )
    } yield (schemaUri, anoncredLibSchema)
  }

  private def resolveCredentialDefinition(
      credentialDefinitionUri: String
  ): IO[PresentationError, (String, AnoncredCredentialDefinition)] = {
    for {
      uri <- ZIO
        .attempt(new URI(credentialDefinitionUri))
        .mapError(e => InvalidCredentialDefinitionURIError(credentialDefinitionUri, e))
      content <- uriDereferencer.dereference(uri).mapError(e => CredentialDefinitionURIDereferencingError(e))
      _ <-
        PublicCredentialDefinitionSerDesV1.schemaSerDes
          .validate(content)
          .mapError(error => AnoncredPresentationParsingError(error))
      anoncredCredentialDefinition = AnoncredCredentialDefinition(content)
    } yield (credentialDefinitionUri, anoncredCredentialDefinition)
  }
  def acceptRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {

    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestReceived)
      issuedCredentials <- credentialRepository
        .findValidIssuedCredentials(credentialsToUse.map(DidCommID(_)))
      validatedCredentialsFormat <- validateCredentialsFormat(record, issuedCredentials)
      _ <- validateCredentials(
        s"No matching issued credentials found in prover db from the given: $credentialsToUse",
        validatedCredentialsFormat
      )
      _ <- presentationRepository
        .updatePresentationWithCredentialsToUse(recordId, Option(credentialsToUse), ProtocolState.PresentationPending)
        @@ CustomMetricsAspect.startRecordingTime(
          s"${record.id}_present_proof_flow_prover_presentation_pending_to_generated_ms_gauge"
        )
      record <- getRecord(recordId)
    } yield record
  }
  def acceptSDJWTRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String],
      claimsToDisclose: Option[ast.Json.Obj]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {

    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestReceived)
      issuedCredentials <- credentialRepository
        .findValidIssuedCredentials(credentialsToUse.map(DidCommID(_)))
      validatedCredentialsFormat <- validateCredentialsFormat(record, issuedCredentials)
      _ <- validateCredentials(
        s"No matching issued credentials found in prover db from the given: $credentialsToUse",
        validatedCredentialsFormat
      )
      _ <-
        presentationRepository
          .updateSDJWTPresentationWithCredentialsToUse(
            recordId,
            Option(credentialsToUse),
            claimsToDisclose,
            ProtocolState.PresentationPending
          ) @@ CustomMetricsAspect.startRecordingTime(
          s"${record.id}_present_proof_flow_prover_presentation_pending_to_generated_ms_gauge"
        )
      record <- getRecord(recordId)
    } yield record
  }

  override def acceptAnoncredRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: AnoncredCredentialProofsV1
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {

    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestReceived)
      issuedCredentials <-
        credentialRepository
          .findValidAnonCredsIssuedCredentials(
            credentialsToUse.credentialProofs.map(credentialProof => DidCommID(credentialProof.credential))
          )
      _ <- validateFullCredentialsFormat(
        record,
        issuedCredentials
      )
      anoncredCredentialProofsV1AsJson <- ZIO
        .fromEither(
          AnoncredCredentialProofsV1.schemaSerDes.serialize(credentialsToUse)
        )
        .mapError(error => PresentationError.AnoncredCredentialProofParsingError(error))
      count <- presentationRepository
        .updateAnoncredPresentationWithCredentialsToUse(
          recordId,
          Option(AnoncredCredentialProofsV1.version),
          Option(anoncredCredentialProofsV1AsJson),
          ProtocolState.PresentationPending
        ) @@ CustomMetricsAspect.startRecordingTime(
        s"${record.id}_present_proof_flow_prover_presentation_pending_to_generated_ms_gauge"
      )
      record <- getRecord(record.id)
    } yield record
  }

  private def validateCredentials(
      errorMessage: String,
      issuedCredentials: Seq[ValidIssuedCredentialRecord]
  ) = {
    val issuedCredentialRaw = issuedCredentials.flatMap(_.issuedCredentialRaw)
    for {
      _ <- ZIO.fromEither(
        Either.cond(
          issuedCredentialRaw.nonEmpty,
          issuedCredentialRaw,
          PresentationError.IssuedCredentialNotFoundError(errorMessage)
        )
      )
    } yield ()
  }

  private def validateCredentialsFormat(
      record: PresentationRecord,
      issuedCredentials: Seq[ValidIssuedCredentialRecord]
  ) = {
    for {
      _ <- ZIO.cond(
        issuedCredentials.map(_.subjectId).toSet.size == 1,
        (),
        PresentationError.HolderBindingError(
          s"Creating a Verifiable Presentation for credential with different subject DID is not supported, found : ${issuedCredentials
              .map(_.subjectId)}"
        )
      )
      validatedCredentials <- ZIO.fromEither(
        Either.cond(
          issuedCredentials.forall(issuedValidCredential =>
            issuedValidCredential.credentialFormat == record.credentialFormat
          ),
          issuedCredentials,
          PresentationError.NotMatchingPresentationCredentialFormat(
            new IllegalArgumentException(
              s"No matching issued credentials format: expectedFormat=${record.credentialFormat}"
            )
          )
        )
      )
    } yield validatedCredentials
  }

  private def validateFullCredentialsFormat(
      record: PresentationRecord,
      issuedCredentials: Seq[ValidFullIssuedCredentialRecord]
  ) = validateCredentialsFormat(
    record,
    issuedCredentials.map(cred =>
      ValidIssuedCredentialRecord(cred.id, None, cred.credentialFormat, cred.subjectId, cred.keyId)
    )
  )

  override def acceptPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      record <- getRecord(recordId)
      _ <- ZIO
        .fromOption(record.presentationData)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      recordUpdated <- markPresentationAccepted(record.id)
    } yield recordUpdated
  }

  override def receivePresentation(
      presentation: Presentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      thid <-
        ZIO
          .fromOption(presentation.thid)
          .map(DidCommID(_))
          .mapError(_ => NoThreadIdFoundInRecord(presentation.id))
      record <- getRecordFromThreadId(thid)
      _ <- presentationRepository
        .updateWithPresentation(record.id, presentation, ProtocolState.PresentationReceived) @@ CustomMetricsAspect
        .startRecordingTime(
          s"${record.id}_present_proof_flow_verifier_presentation_received_to_verification_success_or_failure_ms_gauge"
        )
      record <- getRecord(record.id)
    } yield record
  }

  override def acceptProposePresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      record <- getRecord(recordId)
      request <- ZIO
        .fromOption(record.proposePresentationData)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      // TODO: Generate the JWT credential and use it to create the Presentation object
      requestPresentation = createDidCommRequestPresentationFromProposal(request)
      _ <- presentationRepository
        .updateWithRequestPresentation(recordId, requestPresentation, ProtocolState.PresentationPending)
      record <- getRecord(recordId)
    } yield record
  }

  override def receiveProposePresentation(
      proposePresentation: ProposePresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      thid <-
        ZIO
          .fromOption(proposePresentation.thid)
          .map(DidCommID(_))
          .mapError(_ => NoThreadIdFoundInRecord(proposePresentation.id))
      record <- getRecordFromThreadId(thid)
      _ <- presentationRepository
        .updateWithProposePresentation(record.id, proposePresentation, ProtocolState.ProposalReceived)
      record <- getRecord(record.id)
    } yield record
  }

  private def getRecord(recordId: DidCommID) = {
    presentationRepository
      .findPresentationRecord(recordId)
      .flatMap {
        case None        => ZIO.fail(RecordIdNotFound(recordId))
        case Some(value) => ZIO.succeed(value)
      }
  }

  private def getRecordWithState(
      recordId: DidCommID,
      state: ProtocolState
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      record <- getRecord(recordId)
      _ <- record.protocolState match {
        case s if s == state => ZIO.unit
        case state           => ZIO.fail(InvalidFlowStateError(s"Invalid protocol state for operation: $state"))
      }
    } yield record
  }

  override def markRequestPresentationSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.RequestPending,
      PresentationRecord.ProtocolState.RequestSent
    )

  override def markProposePresentationSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.ProposalPending,
      PresentationRecord.ProtocolState.ProposalSent
    )
  override def markPresentationVerified(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.PresentationReceived,
      PresentationRecord.ProtocolState.PresentationVerified
    )

  override def markPresentationAccepted(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.PresentationVerified,
      PresentationRecord.ProtocolState.PresentationAccepted
    )

  override def markPresentationSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.PresentationGenerated,
      PresentationRecord.ProtocolState.PresentationSent
    )

  override def markPresentationRejected(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.PresentationVerified,
      PresentationRecord.ProtocolState.PresentationRejected
    )

  override def markRequestPresentationRejected(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.RequestReceived,
      PresentationRecord.ProtocolState.RequestRejected
    )

  override def markPresentationVerificationFailed(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    updatePresentationRecordProtocolState(
      recordId,
      PresentationRecord.ProtocolState.PresentationReceived,
      PresentationRecord.ProtocolState.PresentationVerificationFailed
    )

  override def verifyAnoncredPresentation(
      presentation: Presentation,
      requestPresentation: RequestPresentation,
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      serializedPresentation <- presentation.attachments.head.data match {
        case Base64(data) => ZIO.succeed(AnoncredPresentation(new String(JBase64.getUrlDecoder.decode(data))))
        case _            => ZIO.fail(InvalidAnoncredPresentation("Expecting Base64-encoded data"))
      }
      deserializedPresentation <-
        AnoncredPresentationV1.schemaSerDes
          .deserialize(serializedPresentation.data)
          .mapError(error => AnoncredPresentationParsingError(error))
      schemaIds = deserializedPresentation.identifiers.map(_.schema_id)
      schemaMap <-
        ZIO
          .collectAll(schemaIds.map { schemaId =>
            resolveSchema(schemaId)
          })
          .map(_.toMap)
      credentialDefinitionIds = deserializedPresentation.identifiers.map(_.cred_def_id)
      credentialDefinitionMap <-
        ZIO
          .collectAll(credentialDefinitionIds.map { credentialDefinitionId =>
            resolveCredentialDefinition(credentialDefinitionId)
          })
          .map(_.toMap)
      serializedPresentationRequest <- requestPresentation.attachments.head.data match {
        case Base64(data) => ZIO.succeed(AnoncredPresentationRequest(new String(JBase64.getUrlDecoder.decode(data))))
        case _            => ZIO.fail(InvalidAnoncredPresentationRequest("Expecting Base64-encoded data"))
      }
      isValid <-
        ZIO
          .fromTry(
            Try(
              AnoncredLib.verifyPresentation(
                serializedPresentation,
                serializedPresentationRequest,
                schemaMap,
                credentialDefinitionMap
              )
            )
          )
          .mapError((t: Throwable) => AnoncredPresentationVerificationError(t))
          .flatMapError(e =>
            for {
              _ <- markPresentationVerificationFailed(recordId)
            } yield ()
            ZIO.succeed(e)
          )
      result <-
        if isValid then markPresentationVerified(recordId)
        else markPresentationVerificationFailed(recordId)
    } yield result
  }

  override def reportProcessingFailure(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): IO[PresentationError, Unit] =
    presentationRepository.updateAfterFail(recordId, failReason)

  private def getRecordFromThreadId(
      thid: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      maybeRecord <- presentationRepository
        .findPresentationRecordByThreadId(thid)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => ThreadIdNotFound(thid))
    } yield record
  }

  private def toJWTAttachment(options: Options): AttachmentDescriptor = {
    AttachmentDescriptor.buildJsonAttachment(
      payload = PresentationAttachment.build(Some(options)),
      format = Some(PresentCredentialRequestFormat.JWT.name)
    )
  }

  private def toSDJWTAttachment(
      options: Option[Options],
      claimsToDsiclose: ast.Json.Obj
  ): AttachmentDescriptor = {
    AttachmentDescriptor.buildBase64Attachment(
      mediaType = Some("application/json"),
      format = Some(PresentCredentialRequestFormat.SDJWT.name),
      payload = SDJwtPresentation(options, claimsToDsiclose).toJson.getBytes
    )
  }

  private def toAnoncredAttachment(
      presentationRequest: AnoncredPresentationRequestV1
  ): AttachmentDescriptor = {
    AttachmentDescriptor.buildBase64Attachment(
      mediaType = Some("application/json"),
      format = Some(PresentCredentialRequestFormat.Anoncred.name),
      payload = AnoncredPresentationRequestV1.schemaSerDes.serializeToJsonString(presentationRequest).getBytes()
    )
  }

  private def createDidCommRequestPresentation(
      proofTypes: Seq[ProofType],
      thid: DidCommID,
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      attachments: Seq[AttachmentDescriptor]
  ): RequestPresentation = {
    RequestPresentation(
      body = RequestPresentation.Body(
        goal_code = Some("Request Proof Presentation"),
        proof_types = proofTypes
      ),
      attachments = attachments,
      from = pairwiseVerifierDID,
      to = pairwiseProverDID,
      thid = Some(thid.toString)
    )
  }

  private def createDidCommRequestPresentationFromProposal(
      proposePresentation: ProposePresentation
  ): RequestPresentation = {
    // TODO to review what is needed
    val body = RequestPresentation.Body(goal_code = proposePresentation.body.goal_code)

    RequestPresentation(
      body = body,
      attachments = proposePresentation.attachments,
      from = proposePresentation.to,
      to = proposePresentation.from,
      thid = proposePresentation.thid
    )
  }

  private def updatePresentationRecordProtocolState(
      id: DidCommID,
      from: PresentationRecord.ProtocolState,
      to: PresentationRecord.ProtocolState
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    for {
      _ <- getRecord(id)
      _ <- presentationRepository.updatePresentationRecordProtocolState(id, from, to)
      record <- getRecord(id)
    } yield record

}

object PresentationServiceImpl {
  val layer: URLayer[
    URIDereferencer & LinkSecretService & PresentationRepository & CredentialRepository,
    PresentationService
  ] =
    ZLayer.fromFunction(PresentationServiceImpl(_, _, _, _))
}
