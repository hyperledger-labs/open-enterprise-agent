package io.iohk.atala.pollux.core.service

import io.iohk.atala.mercury.model.*
import io.iohk.atala.mercury.protocol.presentproof.*
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.pollux.core.model.presentation.*
import io.iohk.atala.pollux.vc.jwt.*
import io.iohk.atala.shared.models.WalletAccessContext
import zio.*

import java.time.Instant
import java.util as ju
import java.util.UUID

trait PresentationService {

  def extractIdFromCredential(credential: W3cCredentialPayload): Option[UUID]

  def createPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      options: Option[io.iohk.atala.pollux.core.model.presentation.Options],
      format: CredentialFormat,
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def createOOBPresentationRecord(
      goalCode: Option[String],
      goal: Option[String],
      pairwiseVerifierDID: DidId,
      proofTypes: Seq[ProofType],
      maybeOptions: Option[io.iohk.atala.pollux.core.model.presentation.Options],
      format: CredentialFormat,
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def getRequestPresentationFromInvitation(
      pairwiseProverDID: DidId,
      invitation: String
  ): ZIO[WalletAccessContext, PresentationError, RequestPresentation]

  def getPresentationRecords(
      ignoreWithZeroRetries: Boolean
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]]

  def createPresentationPayloadFromRecord(
      record: DidCommID,
      issuer: Issuer,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, PresentationPayload]

  def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      state: PresentationRecord.ProtocolState*
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]]

  def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      state: PresentationRecord.ProtocolState*
  ): IO[PresentationError, Seq[PresentationRecord]]

  def getPresentationRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]]

  def getPresentationRecordByThreadId(
      thid: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]]

  def receiveRequestPresentation(
      connectionId: Option[String],
      request: RequestPresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def acceptRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def rejectRequestPresentation(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def receiveProposePresentation(
      request: ProposePresentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def acceptProposePresentation(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def receivePresentation(presentation: Presentation): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def acceptPresentation(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def rejectPresentation(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markRequestPresentationSent(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markRequestPresentationRejected(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markProposePresentationSent(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markPresentationSent(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markPresentationGenerated(
      recordId: DidCommID,
      presentation: Presentation
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markPresentationVerified(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markPresentationRejected(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markPresentationAccepted(recordId: DidCommID): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def markPresentationVerificationFailed(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def reportProcessingFailure(
      recordId: DidCommID,
      failReason: Option[String]
  ): ZIO[WalletAccessContext, PresentationError, Unit]

}
