package io.iohk.atala.pollux.core.service

import io.iohk.atala.event.notification.{Event, EventNotificationService}
import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.presentproof.{Presentation, ProofType, ProposePresentation, RequestPresentation}
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.pollux.core.model.presentation.Options
import io.iohk.atala.pollux.core.model.{DidCommID, PresentationRecord}
import io.iohk.atala.pollux.vc.jwt.{Issuer, PresentationPayload, W3cCredentialPayload}
import io.iohk.atala.shared.models.WalletAccessContext
import zio.{URLayer, ZIO, ZLayer, IO}

import java.time.Instant
import java.util.UUID
import io.iohk.atala.pollux.core.model.CredentialFormat

class PresentationServiceNotifier(
    svc: PresentationService,
    eventNotificationService: EventNotificationService
) extends PresentationService {

  private val presentationUpdatedEvent = "PresentationUpdated"

  override def createPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      options: Option[Options],
      format: CredentialFormat,
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(
      svc.createPresentationRecord(
        pairwiseVerifierDID,
        pairwiseProverDID,
        thid,
        connectionId,
        proofTypes,
        options,
        format: CredentialFormat
      )
    )
  override def createOOBPresentationRecord(
      goalCode: Option[String],
      goal: Option[String],
      pairwiseVerifierDID: DidId,
      proofTypes: Seq[ProofType],
      maybeOptions: Option[io.iohk.atala.pollux.core.model.presentation.Options],
      format: CredentialFormat,
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(
      svc.createOOBPresentationRecord(goalCode, goal, pairwiseVerifierDID, proofTypes, maybeOptions, format)
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

  override def acceptPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.acceptPresentation(recordId))

  override def rejectPresentation(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord] =
    notifyOnSuccess(svc.rejectPresentation(recordId))

  private[this] def notifyOnSuccess[R](effect: ZIO[R, PresentationError, PresentationRecord]) =
    for {
      record <- effect
      _ <- notify(record)
    } yield record

  private[this] def notify(record: PresentationRecord) = {
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

  override def createPresentationPayloadFromRecord(
      record: DidCommID,
      issuer: Issuer,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, PresentationPayload] =
    svc.createPresentationPayloadFromRecord(record, issuer, issuanceDate)

  override def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      state: PresentationRecord.ProtocolState*
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]] =
    svc.getPresentationRecordsByStates(ignoreWithZeroRetries, limit, state: _*)

  override def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      state: PresentationRecord.ProtocolState*
  ): IO[PresentationError, Seq[PresentationRecord]] =
    svc.getPresentationRecordsByStatesForAllWallets(ignoreWithZeroRetries, limit, state: _*)

  override def getPresentationRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] =
    svc.getPresentationRecord(recordId)

  override def getPresentationRecordByThreadId(
      thid: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]] =
    svc.getPresentationRecordByThreadId(thid)

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
      failReason: Option[_root_.java.lang.String]
  ): ZIO[WalletAccessContext, PresentationError, Unit] = svc.reportProcessingFailure(recordId, failReason)

  override def getRequestPresentationFromInvitation(
      pairwiseProverDID: DidId,
      invitation: _root_.java.lang.String
  ): ZIO[WalletAccessContext, PresentationError, RequestPresentation] =
    svc.getRequestPresentationFromInvitation(pairwiseProverDID, invitation)
}

object PresentationServiceNotifier {
  val layer: URLayer[EventNotificationService & PresentationService, PresentationService] =
    ZLayer.fromFunction(PresentationServiceNotifier(_, _))
}
