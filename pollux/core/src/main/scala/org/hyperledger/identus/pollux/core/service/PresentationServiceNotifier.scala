package org.hyperledger.identus.pollux.core.service

import org.hyperledger.identus.event.notification.{Event, EventNotificationService}
import org.hyperledger.identus.mercury.model.DidId
import org.hyperledger.identus.mercury.protocol.presentproof.{
  Presentation,
  ProofType,
  ProposePresentation,
  RequestPresentation
}
import org.hyperledger.identus.pollux.anoncreds.AnoncredPresentation
import org.hyperledger.identus.pollux.core.model.{DidCommID, PresentationRecord}
import org.hyperledger.identus.pollux.core.model.error.PresentationError
import org.hyperledger.identus.pollux.core.model.presentation.Options
import org.hyperledger.identus.pollux.core.service.serdes.{AnoncredCredentialProofsV1, AnoncredPresentationRequestV1}
import org.hyperledger.identus.pollux.sdjwt.{HolderPrivateKey, PresentationCompact}
import org.hyperledger.identus.pollux.vc.jwt.{Issuer, PresentationPayload, W3cCredentialPayload}
import org.hyperledger.identus.shared.models.*
import zio.{IO, URLayer, ZIO, ZLayer}
import zio.json.*

import java.time.Instant
import java.util.UUID

class PresentationServiceNotifier(
    svc: PresentationService,
    eventNotificationService: EventNotificationService
) extends PresentationService {

  private val presentationUpdatedEvent = "PresentationUpdated"

  override def createJwtPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      options: Option[Options],
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(
      svc.createJwtPresentationRecord(
        pairwiseVerifierDID,
        pairwiseProverDID,
        thid,
        connectionId,
        proofTypes,
        options
      )
    )

  override def createSDJWTPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      claimsToDisclose: ast.Json.Obj,
      options: Option[org.hyperledger.identus.pollux.core.model.presentation.Options]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(
      svc.createSDJWTPresentationRecord(
        pairwiseVerifierDID,
        pairwiseProverDID,
        thid,
        connectionId,
        proofTypes,
        claimsToDisclose,
        options,
      )
    )

  def createAnoncredPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      presentationRequest: AnoncredPresentationRequestV1
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(
      svc.createAnoncredPresentationRecord(
        pairwiseVerifierDID,
        pairwiseProverDID,
        thid,
        connectionId,
        presentationRequest
      )
    )

  override def markRequestPresentationSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.markRequestPresentationSent(recordId))

  override def receiveRequestPresentation(
      connectionId: Option[String],
      request: RequestPresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.receiveRequestPresentation(connectionId, request))

  override def markRequestPresentationRejected(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.markRequestPresentationRejected(recordId))

  override def acceptRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.acceptRequestPresentation(recordId, credentialsToUse))

  override def acceptAnoncredRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: AnoncredCredentialProofsV1
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] = notifyOnSuccess(
    svc.acceptAnoncredRequestPresentation(recordId, credentialsToUse)
  )

  override def rejectRequestPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.rejectRequestPresentation(recordId))

  override def markPresentationGenerated(
      recordId: DidCommID,
      presentation: Presentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.markPresentationGenerated(recordId, presentation))

  override def markPresentationSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.markPresentationSent(recordId))

  override def receivePresentation(
      presentation: Presentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.receivePresentation(presentation))

  override def markPresentationVerified(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.markPresentationVerified(recordId))

  override def markPresentationVerificationFailed(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.markPresentationVerificationFailed(recordId))

  override def verifyAnoncredPresentation(
      presentation: Presentation,
      requestPresentation: RequestPresentation,
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(
      svc.verifyAnoncredPresentation(presentation, requestPresentation, recordId)
    )

  override def acceptPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.acceptPresentation(recordId))

  def acceptSDJWTRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String],
      claimsToDisclose: Option[ast.Json.Obj]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.acceptSDJWTRequestPresentation(recordId, credentialsToUse, claimsToDisclose))

  override def rejectPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.rejectPresentation(recordId))

  private def notifyOnSuccess[R](effect: ZIO[R, PresentationError, PresentationRecord]) =
    for {
      record <- effect
      _ <- notify(record)
    } yield record

  private def notify(record: PresentationRecord) = {
    val result = for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      producer <- eventNotificationService.producer[PresentationRecord]("Presentation")
      _ <- producer.send(Event(presentationUpdatedEvent, record, walletId))
    } yield ()
    result.catchAll(e => ZIO.logError(s"Notification service error: $e"))
  }

  override def extractIdFromCredential(credential: W3cCredentialPayload): Option[UUID] =
    svc.extractIdFromCredential(credential)

  override def getPresentationRecords(
      ignoreWithZeroRetries: Boolean
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]] =
    svc.getPresentationRecords(ignoreWithZeroRetries)

  override def createJwtPresentationPayloadFromRecord(
      record: DidCommID,
      issuer: Issuer,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, PresentationPayload] =
    svc.createJwtPresentationPayloadFromRecord(record, issuer, issuanceDate)

  override def createPresentationFromRecord(
      record: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationCompact] =
    svc.createPresentationFromRecord(record)

  override def createSDJwtPresentation(
      record: DidCommID,
      requestPresentation: RequestPresentation,
      optionalHolderPrivateKey: Option[HolderPrivateKey],
  ): ZIO[WalletAccessContext, PresentationError, Presentation] =
    svc.createSDJwtPresentation(record, requestPresentation, optionalHolderPrivateKey)

  override def createAnoncredPresentationPayloadFromRecord(
      record: DidCommID,
      anoncredCredentialProof: AnoncredCredentialProofsV1,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, AnoncredPresentation] =
    svc.createAnoncredPresentationPayloadFromRecord(record, anoncredCredentialProof, issuanceDate)

  override def createAnoncredPresentation(
      requestPresentation: RequestPresentation,
      recordId: DidCommID,
      anoncredCredentialProof: AnoncredCredentialProofsV1,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, Presentation] =
    svc.createAnoncredPresentation(requestPresentation, recordId, anoncredCredentialProof, issuanceDate)

  override def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      state: PresentationRecord.ProtocolState*
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]] =
    svc.getPresentationRecordsByStates(ignoreWithZeroRetries, limit, state*)

  override def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      state: PresentationRecord.ProtocolState*
  ): IO[PresentationError, Seq[PresentationRecord]] =
    svc.getPresentationRecordsByStatesForAllWallets(ignoreWithZeroRetries, limit, state*)

  override def findPresentationRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] =
    svc.findPresentationRecord(recordId)

  override def findPresentationRecordByThreadId(
      thid: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] =
    svc.findPresentationRecordByThreadId(thid)

  override def receiveProposePresentation(
      request: ProposePresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    svc.receiveProposePresentation((request))

  override def acceptProposePresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    svc.acceptPresentation(recordId)

  override def markProposePresentationSent(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    svc.markProposePresentationSent(recordId)

  override def markPresentationAccepted(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    svc.markPresentationAccepted(recordId)

  override def markPresentationRejected(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    svc.markPresentationRejected(recordId)

  override def reportProcessingFailure(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): IO[PresentationError, Unit] = svc.reportProcessingFailure(recordId, failReason)
}

object PresentationServiceNotifier {
  val layer: URLayer[EventNotificationService & PresentationService, PresentationService] =
    ZLayer.fromFunction(PresentationServiceNotifier(_, _))
}
