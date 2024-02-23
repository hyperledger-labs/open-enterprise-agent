package io.iohk.atala.pollux.core.service

import cats.*
import cats.implicits.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.iohk.atala.mercury.model.*
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation
import io.iohk.atala.mercury.protocol.presentproof.*
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.pollux.core.model.error.PresentationError.*
import io.iohk.atala.pollux.core.model.presentation.*
import io.iohk.atala.pollux.core.repository.{CredentialRepository, PresentationRepository}
import io.iohk.atala.pollux.vc.jwt.*
import io.iohk.atala.shared.models.WalletAccessContext
import io.iohk.atala.shared.utils.Base64Utils
import io.iohk.atala.shared.utils.aspects.CustomMetricsAspect
import zio.*

import java.rmi.UnexpectedException
import java.time.Instant
import java.util as ju
import java.util.UUID

private class PresentationServiceImpl(
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
      count <- presentationRepository
        .updateWithPresentation(recordId, presentation, ProtocolState.PresentationGenerated)
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.endRecordingTime(
        s"${record.id}_present_proof_flow_prover_presentation_pending_to_generated_ms_gauge",
        "present_proof_flow_prover_presentation_pending_to_generated_ms_gauge"
      ) @@ CustomMetricsAspect.startRecordingTime(
        s"${record.id}_present_proof_flow_prover_presentation_generated_to_sent_ms_gauge"
      )
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- presentationRepository
        .getPresentationRecord(recordId)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  override def createPresentationPayloadFromRecord(
      recordId: DidCommID,
      prover: Issuer,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, PresentationPayload] = {

    for {
      maybeRecord <- presentationRepository
        .getPresentationRecord(recordId)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => RecordIdNotFound(recordId))
      credentialsToUse <- ZIO
        .fromOption(record.credentialsToUse)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      requestPresentation <- ZIO
        .fromOption(record.requestPresentationData)
        .mapError(_ => InvalidFlowStateError(s"RequestPresentation not found: $recordId"))
      issuedValidCredentials <- credentialRepository
        .getValidIssuedCredentials(credentialsToUse.map(DidCommID(_)))
        .mapError(RepositoryError.apply)
      signedCredentials = issuedValidCredentials.flatMap(_.issuedCredentialRaw)
      issuedCredentials <- ZIO.fromEither(
        Either.cond(
          signedCredentials.nonEmpty,
          signedCredentials,
          PresentationError.IssuedCredentialNotFoundError(
            new Throwable("No matching issued credentials found in prover db")
          )
        )
      )

      presentationPayload <- createPresentationPayloadFromCredential(
        issuedCredentials,
        record.credentialFormat,
        requestPresentation,
        prover
      )
    } yield presentationPayload
  }

  override def extractIdFromCredential(credential: W3cCredentialPayload): Option[UUID] =
    credential.maybeId.map(_.split("/").last).map(UUID.fromString)

  override def getPresentationRecords(
      ignoreWithZeroRetries: Boolean
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]] = {
    for {
      records <- presentationRepository
        .getPresentationRecords(ignoreWithZeroRetries)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def getPresentationRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] = {
    for {
      record <- presentationRepository
        .getPresentationRecord(recordId)
        .mapError(RepositoryError.apply)
    } yield record
  }

  override def getPresentationRecordByThreadId(
      thid: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] =
    for {
      record <- presentationRepository
        .getPresentationRecordByThreadId(thid)
        .mapError(RepositoryError.apply)
    } yield record

  override def rejectRequestPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    markRequestPresentationRejected(recordId)
  }

  def rejectPresentation(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    markPresentationRejected(recordId)
  }

  override def createPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      maybeOptions: Option[io.iohk.atala.pollux.core.model.presentation.Options],
      format: CredentialFormat,
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      request <- ZIO.succeed(
        createDidCommRequestPresentation(
          proofTypes,
          thid,
          Some(pairwiseVerifierDID),
          Some(pairwiseProverDID),
          format match {
            case CredentialFormat.JWT => maybeOptions.map(options => Seq(toJWTAttachment(options))).getOrElse(Seq.empty)
            case CredentialFormat.AnonCreds =>
              maybeOptions
                .map(options => Seq(toAnoncredAttachment(options)))
                .getOrElse(Seq.empty) // TODO ATL-5945 Create Actual Anoncred Request
          }
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
          invitation = None,
          requestPresentationData = Some(request),
          proposePresentationData = None,
          presentationData = None,
          credentialsToUse = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      count <- presentationRepository
        .createPresentationRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.startRecordingTime(
        s"${record.id}_present_proof_flow_verifier_req_pending_to_sent_ms_gauge"
      )
    } yield record
  }

  override def createOOBPresentationRecord(
      goalCode: Option[String],
      goal: Option[String],
      pairwiseVerifierDID: DidId,
      proofTypes: Seq[ProofType],
      maybeOptions: Option[io.iohk.atala.pollux.core.model.presentation.Options],
      format: CredentialFormat,
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      invitationId <- ZIO.succeed(DidCommID())

      request <- ZIO.succeed(
        createDidCommRequestPresentation(
          proofTypes,
          invitationId,
          Some(pairwiseVerifierDID),
          None, // TODO Fix not require in
          format match {
            case CredentialFormat.JWT => maybeOptions.map(options => Seq(toJWTAttachment(options))).getOrElse(Seq.empty)
            case CredentialFormat.AnonCreds =>
              maybeOptions
                .map(options => Seq(toAnoncredAttachment(options)))
                .getOrElse(Seq.empty) // TODO ATL-5945 Create Actual Anoncred Request
          }
        )
      )
      invitation <- ZIO.succeed(
        PresentProofInvitation.makeInvitation(pairwiseVerifierDID, goalCode, goal, invitationId.value, request)
      )
      record <- ZIO.succeed(
        PresentationRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = invitationId, // Parent thread id since OOB
          connectionId = None,
          schemaId = None, // TODO REMOVE from DB
          role = PresentationRecord.Role.Verifier,
          subjectId = pairwiseVerifierDID, // Need to make optional as well
          protocolState = PresentationRecord.ProtocolState.InvitationGenerated,
          credentialFormat = format,
          invitation = Some(invitation),
          requestPresentationData = Some(request),
          proposePresentationData = None,
          presentationData = None,
          credentialsToUse = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      count <- presentationRepository
        .createPresentationRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.startRecordingTime(
        s"${record.id}_present_proof_flow_verifier_req_pending_to_sent_ms_gauge"
      )
    } yield record
  }
  override def getRequestPresentationFromInvitation(
      pairwiseProverDID: DidId,
      invitation: String
  ): ZIO[WalletAccessContext, PresentationError, RequestPresentation] = {
    for {
      invitation <- ZIO
        .fromEither(io.circe.parser.decode[Invitation](Base64Utils.decodeUrlToString(invitation)))
        .mapError(err => InvitationParsingError(err))
      _ <- presentationRepository
        .getPresentationRecordByThreadId(DidCommID(invitation.id))
        .mapError(RepositoryError.apply)
        .flatMap {
          case None    => ZIO.unit
          case Some(_) => ZIO.fail(InvitationAlreadyReceived(invitation.id))
        }
      requestPresentation <- ZIO.fromEither {
        invitation.attachments
          .flatMap(
            _.headOption.map(attachment =>
              decode[io.iohk.atala.mercury.model.JsonData](attachment.data.asJson.noSpaces)
                .flatMap { data =>
                  RequestPresentation.given_Decoder_RequestPresentation
                    .decodeJson(data.json.asJson)
                    .map(r => r.copy(to = Some(pairwiseProverDID)))
                    .leftMap(err =>
                      PresentationDecodingError(
                        new Throwable(s"RequestPresentation As Attachment decoding error: $err")
                      )
                    )
                }
                .leftMap(err =>
                  PresentationDecodingError(new Throwable(s"Invitation Attachment JsonData decoding error: $err"))
                )
            )
          )
          .getOrElse(
            Left(PresentationNotFoundError(new Throwable("Missing Invitation Attachment for RequestPresentation")))
          )
      }
    } yield requestPresentation

  }
  override def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]] = {
    for {
      records <- presentationRepository
        .getPresentationRecordsByStates(ignoreWithZeroRetries, limit, states: _*)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): IO[PresentationError, Seq[PresentationRecord]] = {
    for {
      records <- presentationRepository
        .getPresentationRecordsByStatesForAllWallets(ignoreWithZeroRetries, limit, states: _*)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def receiveRequestPresentation(
      connectionId: Option[String],
      request: RequestPresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      format <- request.attachments match {
        case Seq() => ZIO.fail(PresentationError.MissingCredential) // TODO MissingCredential is not correct here
        case Seq(head) =>
          val jsonF = PresentCredentialRequestFormat.JWT.name // stable identifier
          val anoncredF = PresentCredentialRequestFormat.Anoncred.name // stable identifier
          head.format match
            case None                    => ZIO.fail(PresentationError.MissingCredentialFormat)
            case Some(`jsonF`)           => ZIO.succeed(CredentialFormat.JWT)
            case Some(`anoncredF`)       => ZIO.succeed(CredentialFormat.AnonCreds)
            case Some(unsupportedFormat) => ZIO.fail(PresentationError.UnsupportedCredentialFormat(unsupportedFormat))
        case _ => ZIO.fail(PresentationError.UnexpectedError("Presentation with multi attachments"))
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
          subjectId = request.to.getOrElse(throw new java.lang.RuntimeException("FIXME")), // FIXME
          protocolState = PresentationRecord.ProtocolState.RequestReceived,
          credentialFormat = format,
          invitation = None,
          requestPresentationData = Some(request),
          proposePresentationData = None,
          presentationData = None,
          credentialsToUse = None,
          metaRetries = maxRetries,
          metaNextRetry = Some(Instant.now()),
          metaLastFailure = None,
        )
      )
      count <- presentationRepository
        .createPresentationRecord(record)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
    } yield record
  }

  /** All credentials MUST be of the same format */
  private def createPresentationPayloadFromCredential(
      issuedCredentials: Seq[String],
      format: CredentialFormat,
      requestPresentation: RequestPresentation,
      prover: Issuer
  ): IO[PresentationError, PresentationPayload] = {

    val verifiableCredentials: Either[
      PresentationError.PresentationDecodingError,
      Seq[JwtVerifiableCredentialPayload | AnoncredVerifiableCredentialPayload]
    ] =
      issuedCredentials.map { signedCredential =>
        format match {
          case CredentialFormat.JWT =>
            decode[io.iohk.atala.mercury.model.Base64](signedCredential)
              .flatMap(x => Right(new String(java.util.Base64.getDecoder().decode(x.base64))))
              .flatMap(x => Right(JwtVerifiableCredentialPayload(JWT(x))))
              .left
              .map(err => PresentationDecodingError(new Throwable(s"JsonData decoding error: $err")))
          case CredentialFormat.AnonCreds =>
            decode[io.iohk.atala.mercury.model.Base64](signedCredential)
              .flatMap(x => Right(new String(java.util.Base64.getDecoder().decode(x.base64))))
              .flatMap(x => Right(AnoncredVerifiableCredentialPayload(x)))
              .left
              .map(err => PresentationDecodingError(new Throwable(s"JsonData decoding error: $err")))
        }
      }.sequence

    val maybePresentationOptions
        : Either[PresentationError, Option[io.iohk.atala.pollux.core.model.presentation.Options]] =
      requestPresentation.attachments.headOption
        .map(attachment =>
          decode[io.iohk.atala.mercury.model.JsonData](attachment.data.asJson.noSpaces)
            .flatMap(data =>
              io.iohk.atala.pollux.core.model.presentation.PresentationAttachment.given_Decoder_PresentationAttachment
                .decodeJson(data.json.asJson)
                .map(_.options)
                .leftMap(err =>
                  PresentationDecodingError(new Throwable(s"PresentationAttachment decoding error: $err"))
                )
            )
            .leftMap(err => PresentationDecodingError(new Throwable(s"JsonData decoding error: $err")))
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

  def acceptRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {

    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestReceived)
      issuedValidCredentials <- credentialRepository
        .getValidIssuedCredentials(credentialsToUse.map(DidCommID(_)))
        .mapError(RepositoryError.apply)
      _ <- ZIO.cond(
        (issuedValidCredentials.map(_.subjectId).toSet.size == 1),
        (),
        PresentationError.HolderBindingError(
          s"Creating a Verifiable Presentation for credential with different subject DID is not supported, found : ${issuedValidCredentials
              .map(_.subjectId)}"
        )
      )
      signedCredentials = issuedValidCredentials.flatMap(_.issuedCredentialRaw)
      // record.credentialFormat match {
      //   case PresentationRecord.CredentialFormat.JWT => issuedRawCredentials
      //   case CredentialFormat.AnonCreds => issuedRawCredentials
      // }
      issuedCredentials <- ZIO.fromEither(
        Either.cond(
          signedCredentials.nonEmpty,
          signedCredentials,
          PresentationError.IssuedCredentialNotFoundError(
            new Throwable(s"No matching issued credentials found in prover db from the given: $credentialsToUse")
          )
        )
      )
      count <- presentationRepository
        .updatePresentationWithCredentialsToUse(recordId, Option(credentialsToUse), ProtocolState.PresentationPending)
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.startRecordingTime(
        s"${record.id}_present_proof_flow_prover_presentation_pending_to_generated_ms_gauge"
      )
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- presentationRepository
        .getPresentationRecord(recordId)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  override def acceptPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      maybeRecord <- presentationRepository
        .getPresentationRecord(recordId)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => RecordIdNotFound(recordId))
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
      record <- getRecordFromThreadId(presentation.thid)
      _ <- presentationRepository
        .updateWithPresentation(record.id, presentation, ProtocolState.PresentationReceived)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply) @@ CustomMetricsAspect.startRecordingTime(
        s"${record.id}_present_proof_flow_verifier_presentation_received_to_verification_success_or_failure_ms_gauge"
      )
      record <- presentationRepository
        .getPresentationRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  override def acceptProposePresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      maybeRecord <- presentationRepository
        .getPresentationRecord(recordId)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => RecordIdNotFound(recordId))
      request <- ZIO
        .fromOption(record.proposePresentationData)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      // TODO: Generate the JWT credential and use it to create the Presentation object
      requestPresentation = createDidCommRequestPresentationFromProposal(request)
      count <- presentationRepository
        .updateWithRequestPresentation(recordId, requestPresentation, ProtocolState.PresentationPending)
        .mapError(RepositoryError.apply)
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- presentationRepository
        .getPresentationRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  override def receiveProposePresentation(
      proposePresentation: ProposePresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      record <- getRecordFromThreadId(proposePresentation.thid)
      _ <- presentationRepository
        .updateWithProposePresentation(record.id, proposePresentation, ProtocolState.ProposalReceived)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
      record <- presentationRepository
        .getPresentationRecord(record.id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(record.id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

  private[this] def getRecordWithState(
      recordId: DidCommID,
      state: ProtocolState
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      maybeRecord <- presentationRepository
        .getPresentationRecord(recordId)
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

  def reportProcessingFailure(
      recordId: DidCommID,
      failReason: Option[String]
  ): ZIO[WalletAccessContext, PresentationError, Unit] =
    presentationRepository
      .updateAfterFail(recordId, failReason)
      .mapError(RepositoryError.apply)
      .flatMap {
        case 1 => ZIO.unit
        case n => ZIO.fail(UnexpectedError(s"Invalid number of records updated: $n"))
      }

  private[this] def getRecordFromThreadId(
      thid: Option[String]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      thidID <- ZIO
        .fromOption(thid)
        .map(DidCommID(_))
        .mapError(_ => UnexpectedError("No `thid` found in Presentation request"))
      maybeRecord <- presentationRepository
        .getPresentationRecordByThreadId(thidID)
        .mapError(RepositoryError.apply)
      record <- ZIO
        .fromOption(maybeRecord)
        .mapError(_ => ThreadIdNotFound(thidID))
    } yield record
  }

  private[this] def toJWTAttachment(options: Options): AttachmentDescriptor = {
    AttachmentDescriptor.buildJsonAttachment(
      payload = PresentationAttachment.build(Some(options)),
      format = Some(PresentCredentialRequestFormat.JWT.name)
    )
  }

  // TODO ATL-5945 Create Actual Anoncred Request
  private[this] def toAnoncredAttachment(options: Options): AttachmentDescriptor = {
    AttachmentDescriptor.buildJsonAttachment(
      payload = PresentationAttachment.build(Some(options)),
      format = Some(PresentCredentialRequestFormat.Anoncred.name)
    )
  }

  private[this] def createDidCommRequestPresentation(
      proofTypes: Seq[ProofType],
      thid: DidCommID,
      pairwiseVerifierDID: Option[DidId],
      pairwiseProverDID: Option[DidId],
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

  private[this] def createDidCommRequestPresentationFromProposal(
      proposePresentation: ProposePresentation
  ): RequestPresentation = {
    // TODO to review what is needed
    val body = RequestPresentation.Body(goal_code = proposePresentation.body.goal_code)

    RequestPresentation(
      body = body,
      attachments = proposePresentation.attachments,
      from = Some(proposePresentation.to),
      to = Some(proposePresentation.from),
      thid = proposePresentation.thid
    )
  }

  private[this] def updatePresentationRecordProtocolState(
      id: DidCommID,
      from: PresentationRecord.ProtocolState,
      to: PresentationRecord.ProtocolState
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = {
    for {
      _ <- presentationRepository
        .updatePresentationRecordProtocolState(id, from, to)
        .flatMap {
          case 1 => ZIO.succeed(())
          case n => ZIO.fail(UnexpectedException(s"Invalid row count result: $n"))
        }
        .mapError(RepositoryError.apply)
      record <- presentationRepository
        .getPresentationRecord(id)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(id))
          case Some(value) => ZIO.succeed(value)
        }
    } yield record
  }

}

object PresentationServiceImpl {
  val layer: URLayer[PresentationRepository & CredentialRepository, PresentationService] =
    ZLayer.fromFunction(PresentationServiceImpl(_, _))
}
