package io.iohk.atala.pollux.core.service

import com.google.protobuf.ByteString
import io.circe.Json
import io.circe.syntax.*
import io.iohk.atala.iris.proto.dlt.IrisOperation
import io.iohk.atala.iris.proto.service.IrisOperationId
import io.iohk.atala.iris.proto.service.IrisServiceGrpc.IrisServiceStub
import io.iohk.atala.iris.proto.vc_operations.IssueCredentialsBatch
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.issuecredential.Attribute
import io.iohk.atala.mercury.protocol.issuecredential.CredentialPreview
import io.iohk.atala.mercury.protocol.issuecredential.IssueCredential
import io.iohk.atala.mercury.protocol.issuecredential.OfferCredential
import io.iohk.atala.mercury.protocol.issuecredential.RequestCredential
import io.iohk.atala.pollux.core.model._
import io.iohk.atala.pollux.core.model.error.CredentialServiceError
import io.iohk.atala.pollux.core.model.error.CredentialServiceError._
import io.iohk.atala.pollux.core.repository.CredentialRepository
import io.iohk.atala.pollux.vc.jwt.Issuer
import io.iohk.atala.pollux.vc.jwt.JwtCredentialPayload
import io.iohk.atala.pollux.vc.jwt.W3CCredential
import io.iohk.atala.pollux.vc.jwt.W3cCredentialPayload
import io.iohk.atala.pollux.vc.jwt.W3cPresentationPayload
import io.iohk.atala.prism.crypto.MerkleInclusionProof
import io.iohk.atala.prism.crypto.MerkleTreeKt
import io.iohk.atala.prism.crypto.Sha256
import io.iohk.atala.resolvers.DidValidator
import zio.*

import java.rmi.UnexpectedException
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.time.Instant
import java.util.UUID
import io.iohk.atala.castor.core.model.did.CanonicalPrismDID
import io.iohk.atala.mercury.model.AttachmentDescriptor
import io.iohk.atala.pollux.core.model._
import io.iohk.atala.pollux.core.model.presentation.PresentationAttachment
import io.iohk.atala.pollux.core.model.presentation.Options
import io.iohk.atala.pollux.core.model.presentation.PresentationDefinition
import io.iohk.atala.pollux.core.model.presentation.ClaimFormat
import io.iohk.atala.pollux.core.model.presentation.Ldp
import io.iohk.atala.pollux.vc.jwt.{PresentationPayload, JWT}
import io.iohk.atala.mercury.model.JsonData
import io.iohk.atala.castor.core.model.did.PrismDID

object CredentialServiceImpl {
  val layer: URLayer[IrisServiceStub & CredentialRepository[Task], CredentialService] =
    ZLayer.fromFunction(CredentialServiceImpl(_, _))
}

private class CredentialServiceImpl(
    irisClient: IrisServiceStub,
    credentialRepository: CredentialRepository[Task],
    maxRetries: Int = 5 // TODO move to config
) extends CredentialService {

  import IssueCredentialRecord._

  override def extractIdFromCredential(credential: W3cCredentialPayload): Option[DidCommID] =
    credential.maybeId.map(_.split("/").last).map(DidCommID(_))

  override def getIssueCredentialRecords(): IO[CredentialServiceError, Seq[IssueCredentialRecord]] = {
    for {
      records <- credentialRepository
        .getIssueCredentialRecords()
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def getIssueCredentialRecord(
      recordId: DidCommID
  ): IO[CredentialServiceError, Option[IssueCredentialRecord]] = {
    for {
      record <- credentialRepository
        .getIssueCredentialRecord(recordId)
        .mapError(RepositoryError.apply)
    } yield record
  }

  override def createIssueCredentialRecord(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      thid: DidCommID,
      schemaId: Option[String],
      claims: Map[String, String],
      validityPeriod: Option[Double],
      automaticIssuance: Option[Boolean],
      awaitConfirmation: Option[Boolean],
      issuingDID: Option[CanonicalPrismDID]
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      offer <- ZIO.succeed(
        createDidCommOfferCredential(
          pairwiseIssuerDID = pairwiseIssuerDID,
          pairwiseHolderDID = pairwiseHolderDID,
          claims = claims,
          thid = thid,
          UUID.randomUUID().toString(),
          "domain"
        )
      )
      record <- ZIO.succeed(
        IssueCredentialRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = thid,
          schemaId = schemaId,
          role = IssueCredentialRecord.Role.Issuer,
          subjectId = None,
          validityPeriod = validityPeriod,
          automaticIssuance = automaticIssuance,
          awaitConfirmation = awaitConfirmation,
          protocolState = IssueCredentialRecord.ProtocolState.OfferPending,
          publicationState = None,
          offerCredentialData = Some(offer),
          requestCredentialData = None,
          issueCredentialData = None,
          issuedCredentialRaw = None,
          issuingDID = issuingDID,
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

  override def getIssueCredentialRecordsByStates(
      states: IssueCredentialRecord.ProtocolState*
  ): IO[CredentialServiceError, Seq[IssueCredentialRecord]] = {
    for {
      records <- credentialRepository
        .getIssueCredentialRecordsByStates(states: _*)
        .mapError(RepositoryError.apply)
    } yield records
  }

  override def receiveCredentialOffer(
      offer: OfferCredential
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      // TODO: align with the standard (ATL-3507)
      offerAttachment <- offer.attachments.headOption
        .map(_.data.asJson)
        .fold(ZIO.fail(CredentialServiceError.UnexpectedError("An attachment is expected in CredentialOffer"))) {
          json =>
            ZIO
              .fromTry(json.hcursor.downField("json").as[CredentialOfferAttachment].toTry)
              .mapError(e =>
                CredentialServiceError.UnexpectedError(s"Unexpected CredentialOffer attachment format: ${e.toString()}")
              )
        }
      record <- ZIO.succeed(
        IssueCredentialRecord(
          id = DidCommID(),
          createdAt = Instant.now,
          updatedAt = None,
          thid = DidCommID(offer.thid.getOrElse(offer.id)),
          schemaId = None,
          role = Role.Holder,
          subjectId = None,
          validityPeriod = None,
          automaticIssuance = None,
          awaitConfirmation = None,
          protocolState = IssueCredentialRecord.ProtocolState.OfferReceived,
          publicationState = None,
          offerCredentialData = Some(offer),
          requestCredentialData = None,
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

  override def acceptCredentialOffer(
      recordId: DidCommID,
      subjectId: String
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      prismDID <- ZIO
        .fromEither(PrismDID.fromString(subjectId))
        .mapError(_ => CredentialServiceError.UnsupportedDidFormat(subjectId))
      record <- getRecordWithState(recordId, ProtocolState.OfferReceived)
      count <- credentialRepository
        .updateWithSubjectId(recordId, subjectId, ProtocolState.RequestPending)
        .mapError(RepositoryError.apply)
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

  override def createPresentationPayload(
      recordId: DidCommID,
      subject: Issuer
  ): IO[CredentialServiceError, PresentationPayload] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestPending)
      offer <- ZIO
        .fromOption(record.offerCredentialData)
        .mapError(_ => UnexpectedError(s"Offer data not found in record: $recordId"))
      attachmentDescriptor <- ZIO
        .fromOption(offer.attachments.headOption)
        .mapError(_ => UnexpectedError(s"Attachments not found in record: $recordId"))
      json <- attachmentDescriptor.data match
        case JsonData(json) => ZIO.succeed(json.asJson)
        case _              => ZIO.fail(UnexpectedError("Attachment doesn't contain JsonData"))
      options <- ZIO
        .fromEither(json.as[PresentationAttachment].map(_.options))
        .mapError(df => UnexpectedError(df.getMessage))
    } yield {
      W3cPresentationPayload(
        `@context` = Vector("https://www.w3.org/2018/presentations/v1"),
        maybeId = None,
        `type` = Vector("VerifiablePresentation"),
        verifiableCredential = IndexedSeq.empty,
        holder = subject.did.value,
        verifier = IndexedSeq.empty ++ options.map(_.domain),
        maybeIssuanceDate = None,
        maybeExpirationDate = None
      ).toJwtPresentationPayload.copy(maybeNonce = options.map(_.challenge))
    }
  }

  override def generateCredentialRequest(
      recordId: DidCommID,
      signedPresentation: JWT
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestPending)
      offer <- ZIO
        .fromOption(record.offerCredentialData)
        .mapError(_ => InvalidFlowStateError(s"No offer found for this record: $recordId"))
      request = createDidCommRequestCredential(offer, signedPresentation)
      count <- credentialRepository
        .updateWithRequestCredential(recordId, request, ProtocolState.RequestGenerated)
        .mapError(RepositoryError.apply)
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

  override def receiveCredentialRequest(
      request: RequestCredential
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordFromThreadIdWithState(
        request.thid.map(DidCommID(_)),
        ProtocolState.OfferPending,
        ProtocolState.OfferSent
      )
      _ <- credentialRepository
        .updateWithRequestCredential(record.id, request, ProtocolState.RequestReceived)
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

  override def acceptCredentialRequest(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.RequestReceived)
      request <- ZIO
        .fromOption(record.requestCredentialData)
        .mapError(_ => InvalidFlowStateError(s"No request found for this record: $recordId"))
      issue = createDidCommIssueCredential(request)
      count <- credentialRepository
        .updateWithIssueCredential(recordId, issue, ProtocolState.CredentialPending)
        .mapError(RepositoryError.apply)
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
      issue: IssueCredential
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    val rawIssuedCredential = issue.attachments.map(_.data.asJson.noSpaces).headOption.getOrElse("???") // TODO
    for {
      record <- getRecordFromThreadIdWithState(
        issue.thid.map(DidCommID(_)),
        ProtocolState.RequestPending,
        ProtocolState.RequestSent
      )
      _ <- credentialRepository
        .updateWithIssuedRawCredential(record.id, issue, rawIssuedCredential, ProtocolState.CredentialReceived)
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

  override def markOfferSent(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordProtocolState(
      recordId,
      IssueCredentialRecord.ProtocolState.OfferPending,
      IssueCredentialRecord.ProtocolState.OfferSent
    )

  override def markRequestSent(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordProtocolState(
      recordId,
      IssueCredentialRecord.ProtocolState.RequestGenerated,
      IssueCredentialRecord.ProtocolState.RequestSent
    )

  override def markCredentialGenerated(
      recordId: DidCommID,
      issueCredential: IssueCredential
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- getRecordWithState(recordId, ProtocolState.CredentialPending)
      count <- credentialRepository
        .updateWithIssueCredential(
          recordId,
          issueCredential,
          IssueCredentialRecord.ProtocolState.CredentialGenerated
        )
        .mapError(RepositoryError.apply)
      _ <- count match
        case 1 => ZIO.succeed(())
        case n => ZIO.fail(RecordIdNotFound(recordId))
      record <- credentialRepository
        .getIssueCredentialRecord(recordId)
        .mapError(RepositoryError.apply)
        .flatMap {
          case None        => ZIO.fail(RecordIdNotFound(recordId))
          case Some(value) => ZIO.succeed(value)
        }

    } yield record
  }

  override def markCredentialSent(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordProtocolState(
      recordId,
      IssueCredentialRecord.ProtocolState.CredentialGenerated,
      IssueCredentialRecord.ProtocolState.CredentialSent
    )

  override def markCredentialPublicationPending(
      recordId: DidCommID
  ): IO[CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordPublicationState(
      recordId,
      None,
      Some(IssueCredentialRecord.PublicationState.PublicationPending)
    )

  override def markCredentialPublicationQueued(
      recordId: DidCommID
  ): IO[CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordPublicationState(
      recordId,
      Some(IssueCredentialRecord.PublicationState.PublicationPending),
      Some(IssueCredentialRecord.PublicationState.PublicationQueued)
    )

  override def markCredentialPublished(recordId: DidCommID): IO[CredentialServiceError, IssueCredentialRecord] =
    updateCredentialRecordPublicationState(
      recordId,
      Some(IssueCredentialRecord.PublicationState.PublicationQueued),
      Some(IssueCredentialRecord.PublicationState.Published)
    )

  private[this] def getRecordWithState(
      recordId: DidCommID,
      state: ProtocolState
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
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
      states: ProtocolState*
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      thid <- ZIO
        .fromOption(thid)
        .mapError(_ => UnexpectedError("No `thid` found in credential request"))
      maybeRecord <- credentialRepository
        .getIssueCredentialRecordByThreadId(thid)
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

  private[this] def createDidCommOfferCredential(
      pairwiseIssuerDID: DidId,
      pairwiseHolderDID: DidId,
      claims: Map[String, String],
      thid: DidCommID,
      challenge: String,
      domain: String
  ): OfferCredential = {
    val attributes = claims.map { case (k, v) => Attribute(k, v) }
    val credentialPreview = CredentialPreview(attributes = attributes.toSeq)
    val body = OfferCredential.Body(goal_code = Some("Offer Credential"), credential_preview = credentialPreview)

    OfferCredential(
      body = body,
      // TODO: align with the standard (ATL-3507)
      attachments = Seq(
        AttachmentDescriptor.buildJsonAttachment(
          payload = PresentationAttachment(
            Some(Options(challenge, domain)),
            PresentationDefinition(format = Some(ClaimFormat(ldp = Some(Ldp(Seq("EcdsaSecp256k1Signature2019"))))))
          )
        )
      ),
      from = pairwiseIssuerDID,
      to = pairwiseHolderDID,
      thid = Some(thid.toString())
    )
  }

  private[this] def createDidCommRequestCredential(
      offer: OfferCredential,
      signedPresentation: JWT
  ): RequestCredential = {
    RequestCredential(
      body = RequestCredential.Body(
        goal_code = offer.body.goal_code,
        comment = offer.body.comment,
        formats = offer.body.formats
      ),
      attachments = Seq(
        AttachmentDescriptor
          .buildBase64Attachment(
            payload = signedPresentation.value.getBytes(),
            mediaType = Some("prism/jwt")
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
        formats = request.body.formats
      ),
      attachments = Nil,
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
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
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

  /** this is an auxiliary function.
    *
    * @note
    *   Between updating and getting the CredentialRecord back the CredentialRecord can be updated by other operations
    *   in the middle.
    *
    * TODO: this should be improved to behave exactly like atomic operation.
    */
  private[this] def updateCredentialRecordPublicationState(
      id: DidCommID,
      from: Option[IssueCredentialRecord.PublicationState],
      to: Option[IssueCredentialRecord.PublicationState]
  ): IO[CredentialServiceError, IssueCredentialRecord] = {
    for {
      record <- credentialRepository
        .updateCredentialRecordPublicationState(id, from, to)
        .mapError(RepositoryError.apply)
        .flatMap {
          case 0 =>
            credentialRepository
              .getIssueCredentialRecord(id)
              .mapError(RepositoryError.apply)
              .flatMap {
                case None => ZIO.fail(RecordIdNotFound(id))
                case Some(record) if record.publicationState == to => // Not update by is alredy on the desirable state
                  ZIO.succeed(record)
                case Some(record) =>
                  ZIO.fail(
                    OperationNotExecuted(
                      id,
                      s"CredentialRecord was not updated because have the PublicationState ${record.publicationState}"
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
                  {
                    if (record.publicationState == to) (ZIO.unit)
                    else
                      ZIO.logError(
                        s"The CredentialRecord ($id) is expected to be on the PublicationState '$to' after updating it"
                      ) // The expectation is for the record to still be on the state we (just) updated to
                  } *> ZIO.succeed(record)
              }
          case n => ZIO.fail(UnexpectedError(s"Invalid row count result: $n"))
        }
    } yield record
  }

  private def sendCredential(
      jwtCredential: JwtCredentialPayload,
      holderDid: DID,
      inclusionProof: MerkleInclusionProof
  ): Nothing = ???

  override def createCredentialPayloadFromRecord(
      record: IssueCredentialRecord,
      issuer: Issuer,
      issuanceDate: Instant
  ): IO[CreateCredentialPayloadFromRecordError, W3cCredentialPayload] = {

    val claims = for {
      offerCredentialData <- record.offerCredentialData
      preview = offerCredentialData.body.credential_preview
      claims = preview.attributes.map(attr => attr.name -> attr.value).toMap
    } yield claims

    val credential = for {
      claims <- ZIO.fromEither(
        Either.cond(
          claims.isDefined,
          claims.get,
          CredentialServiceError.CreateCredentialPayloadFromRecordError(
            new Throwable("Could not extract claims from \"requestCredential\" Didcome message")
          )
        )
      )
      // TODO: get schema when schema registry is available if schema ID is provided
      credential = W3cCredentialPayload(
        `@context` = Set(
          "https://www.w3.org/2018/credentials/v1"
        ), // TODO: his information should come from Schema registry by record.schemaId
        maybeId = None,
        `type` =
          Set("VerifiableCredential"), // TODO: This information should come from Schema registry by record.schemaId
        issuer = issuer.did,
        issuanceDate = issuanceDate,
        maybeExpirationDate = record.validityPeriod.map(sec => issuanceDate.plusSeconds(sec.toLong)),
        maybeCredentialSchema = None,
        // TODO: Get subject DID from record.request.attachments after verifying the signature/challenge/domain
        credentialSubject = claims.updated("id", record.subjectId.getOrElse("did:prism:12345")).asJson,
        maybeCredentialStatus = None,
        maybeRefreshService = None,
        maybeEvidence = None,
        maybeTermsOfUse = None
      )
    } yield credential

    credential

  }

  def publishCredentialBatch(
      credentials: Seq[W3cCredentialPayload],
      issuer: Issuer
  ): IO[CredentialServiceError, PublishedBatchData] = {
    import collection.JavaConverters.*

    val hashes = credentials
      .map { c =>
        val encoded = W3CCredential.toEncodedJwt(c, issuer)
        Sha256.compute(encoded.value.getBytes)
      }
      .toBuffer
      .asJava

    val merkelRootAndProofs = MerkleTreeKt.generateProofs(hashes)
    val root = merkelRootAndProofs.component1()
    val proofs = merkelRootAndProofs.component2().asScala.toSeq

    val irisOperation = IrisOperation(
      IrisOperation.Operation.IssueCredentialsBatch(
        IssueCredentialsBatch(
          issuerDid = issuer.did.value,
          merkleRoot = ByteString.copyFrom(root.getHash.component1)
        )
      )
    )

    val credentialsAndProofs = credentials.zip(proofs)

    val result = ZIO
      .fromFuture(_ => irisClient.scheduleOperation(irisOperation))
      .mapBoth(
        IrisError(_),
        irisOpeRes =>
          PublishedBatchData(
            operationId = IrisOperationId(irisOpeRes.operationId),
            credentialsAnsProofs = credentialsAndProofs
          )
      )

    result
  }

  override def markCredentialRecordsAsPublishQueued(
      credentialsAndProofs: Seq[(W3cCredentialPayload, MerkleInclusionProof)]
  ): IO[CredentialServiceError, Int] = {

    /*
     * Since id of the credential is optional according to W3 spec,
     * it is of a type Option in W3cCredentialPayload since it is a generic W3 credential payload
     * but for our use-case, credentials must have an id, so if for some reason at least one
     * credential does not have an id, we return an error
     *
     */
    val maybeUndefinedId = credentialsAndProofs.find(x => extractIdFromCredential(x._1).isEmpty)

    if (maybeUndefinedId.isDefined) then ZIO.fail(CredentialIdNotDefined(maybeUndefinedId.get._1))
    else
      val idStateAndProof = credentialsAndProofs.map { credentialAndProof =>
        (
          extractIdFromCredential(credentialAndProof._1).get, // won't fail because of checks above
          IssueCredentialRecord.PublicationState.PublicationQueued,
          credentialAndProof._2
        )
      }

      credentialRepository
        .updateCredentialRecordStateAndProofByCredentialIdBulk(idStateAndProof)
        .mapError(RepositoryError(_))

  }

}
