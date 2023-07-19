package io.iohk.atala.pollux.core.service

import io.iohk.atala.mercury.model.*
import io.iohk.atala.mercury.protocol.presentproof.*
import io.iohk.atala.pollux.core.model.*
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.pollux.core.model.presentation.*
import io.iohk.atala.pollux.vc.jwt.*
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
      options: Option[io.iohk.atala.pollux.core.model.presentation.Options]
  ): IO[PresentationError, PresentationRecord]

  def getPresentationRecords(): IO[PresentationError, Seq[PresentationRecord]]

  def createPresentationPayloadFromRecord(
      record: DidCommID,
      issuer: Issuer,
      issuanceDate: Instant
  ): IO[PresentationError, PresentationPayload]

  def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      state: PresentationRecord.ProtocolState*
  ): IO[PresentationError, Seq[PresentationRecord]]

  def getPresentationRecord(recordId: DidCommID): IO[PresentationError, Option[PresentationRecord]]

  def getPresentationRecordByThreadId(thid: DidCommID): IO[PresentationError, Option[PresentationRecord]]

  def receiveRequestPresentation(
      connectionId: Option[String],
      request: RequestPresentation
  ): IO[PresentationError, PresentationRecord]

  def acceptRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String]
  ): IO[PresentationError, PresentationRecord]

  def rejectRequestPresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def receiveProposePresentation(request: ProposePresentation): IO[PresentationError, PresentationRecord]

  def acceptProposePresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def receivePresentation(presentation: Presentation): IO[PresentationError, PresentationRecord]

  def acceptPresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def rejectPresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markRequestPresentationSent(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markRequestPresentationRejected(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markProposePresentationSent(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markPresentationSent(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markPresentationGenerated(
      recordId: DidCommID,
      presentation: Presentation
  ): IO[PresentationError, PresentationRecord]

  def markPresentationVerified(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markPresentationRejected(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markPresentationAccepted(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def markPresentationVerificationFailed(recordId: DidCommID): IO[PresentationError, PresentationRecord]

  def reportProcessingFailure(recordId: DidCommID, failReason: Option[String]): IO[PresentationError, Unit]

}
