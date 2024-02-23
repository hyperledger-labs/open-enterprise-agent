package io.iohk.atala.pollux.core.service

import io.iohk.atala.mercury.model.DidId
import io.iohk.atala.mercury.protocol.presentproof.{Presentation, ProofType, ProposePresentation, RequestPresentation}
import io.iohk.atala.pollux.core.model.error.PresentationError
import io.iohk.atala.pollux.core.model.presentation.Options
import io.iohk.atala.pollux.core.model.{DidCommID, PresentationRecord}
import io.iohk.atala.pollux.vc.jwt.{Issuer, PresentationPayload, W3cCredentialPayload}
import zio.mock.{Mock, Proxy}
import zio.{IO, URLayer, ZIO, ZLayer, mock}

import java.time.Instant
import java.util.UUID
import io.iohk.atala.pollux.core.model.CredentialFormat

object MockPresentationService extends Mock[PresentationService] {

  object CreatePresentationRecord
      extends Effect[
        (DidId, DidId, DidCommID, Option[String], Seq[ProofType], Option[Options]),
        PresentationError,
        PresentationRecord
      ]
  object CreateOOBPresentationRecord
      extends Effect[
        (Option[String], Option[String], DidId, Seq[ProofType], Option[Options], CredentialFormat),
        PresentationError,
        PresentationRecord
      ]
  object AcceptRequestPresentationInvitation extends Effect[(DidId, String), PresentationError, RequestPresentation]

  object MarkRequestPresentationSent extends Effect[DidCommID, PresentationError, PresentationRecord]

  object ReceivePresentation extends Effect[Presentation, PresentationError, PresentationRecord]

  object MarkPresentationVerified extends Effect[DidCommID, PresentationError, PresentationRecord]

  object MarkPresentationAccepted extends Effect[DidCommID, PresentationError, PresentationRecord]

  object MarkPresentationRejected extends Effect[DidCommID, PresentationError, PresentationRecord]

  object MarkPresentationVerificationFailed extends Effect[DidCommID, PresentationError, PresentationRecord]

  object AcceptRequestPresentation extends Effect[(DidCommID, Seq[String]), PresentationError, PresentationRecord]

  object RejectRequestPresentation extends Effect[DidCommID, PresentationError, PresentationRecord]

  object MarkPresentationGenerated extends Effect[(DidCommID, Presentation), PresentationError, PresentationRecord]

  object MarkPresentationSent extends Effect[DidCommID, PresentationError, PresentationRecord]

  object AcceptPresentation extends Effect[DidCommID, PresentationError, PresentationRecord]

  object RejectPresentation extends Effect[DidCommID, PresentationError, PresentationRecord]

  object ReceiveRequestPresentation
      extends Effect[(Option[String], RequestPresentation), PresentationError, PresentationRecord]

  override val compose: URLayer[mock.Proxy, PresentationService] = ZLayer {
    for {
      proxy <- ZIO.service[Proxy]
    } yield new PresentationService {

      override def createPresentationRecord(
          pairwiseVerifierDID: DidId,
          pairwiseProverDID: DidId,
          thid: DidCommID,
          connectionId: Option[String],
          proofTypes: Seq[ProofType],
          options: Option[Options],
          format: CredentialFormat,
      ): IO[PresentationError, PresentationRecord] =
        proxy(
          CreatePresentationRecord,
          (pairwiseVerifierDID, pairwiseProverDID, thid, connectionId, proofTypes, options)
        )

      override def createOOBPresentationRecord(
          goalCode: Option[String],
          goal: Option[String],
          pairwiseVerifierDID: DidId,
          proofTypes: Seq[ProofType],
          maybeOptions: Option[io.iohk.atala.pollux.core.model.presentation.Options],
          format: CredentialFormat,
      ): IO[PresentationError, PresentationRecord] =
        proxy(
          CreateOOBPresentationRecord,
          (goalCode, goal, pairwiseVerifierDID, proofTypes, maybeOptions, format)
        )

      override def acceptRequestPresentation(
          recordId: DidCommID,
          credentialsToUse: Seq[String]
      ): IO[PresentationError, PresentationRecord] =
        proxy(AcceptRequestPresentation, (recordId, credentialsToUse))

      override def rejectRequestPresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(RejectRequestPresentation, recordId)

      override def receivePresentation(presentation: Presentation): IO[PresentationError, PresentationRecord] =
        proxy(ReceivePresentation, presentation)

      override def markRequestPresentationSent(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(MarkRequestPresentationSent, recordId)

      override def markPresentationSent(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(MarkPresentationSent, recordId)

      override def markPresentationGenerated(
          recordId: DidCommID,
          presentation: Presentation
      ): IO[PresentationError, PresentationRecord] =
        proxy(MarkPresentationGenerated, (recordId, presentation))

      override def markPresentationVerified(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(MarkPresentationVerified, recordId)

      override def markPresentationRejected(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(MarkPresentationRejected, recordId)

      override def markPresentationAccepted(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(MarkPresentationAccepted, recordId)

      override def markPresentationVerificationFailed(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(MarkPresentationVerificationFailed, recordId)

      override def getRequestPresentationFromInvitation(
          pairwiseProverDID: DidId,
          invitation: String
      ): IO[PresentationError, RequestPresentation] =
        proxy(AcceptRequestPresentationInvitation, (pairwiseProverDID, invitation))

      override def receiveRequestPresentation(
          connectionId: Option[String],
          request: RequestPresentation
      ): IO[PresentationError, PresentationRecord] =
        proxy(ReceiveRequestPresentation, (connectionId, request))

      override def acceptPresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(AcceptPresentation, recordId)

      override def rejectPresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord] =
        proxy(RejectPresentation, recordId)

      override def extractIdFromCredential(credential: W3cCredentialPayload): Option[UUID] = ???

      override def getPresentationRecords(
          ignoreWithZeroRetries: Boolean
      ): IO[PresentationError, Seq[PresentationRecord]] = ???

      override def createPresentationPayloadFromRecord(
          record: DidCommID,
          issuer: Issuer,
          issuanceDate: Instant
      ): IO[PresentationError, PresentationPayload] = ???

      override def getPresentationRecordsByStates(
          ignoreWithZeroRetries: Boolean,
          limit: Int,
          state: PresentationRecord.ProtocolState*
      ): IO[PresentationError, Seq[PresentationRecord]] = ???

      override def getPresentationRecordsByStatesForAllWallets(
          ignoreWithZeroRetries: Boolean,
          limit: Int,
          state: PresentationRecord.ProtocolState*
      ): IO[PresentationError, Seq[PresentationRecord]] = ???

      override def getPresentationRecord(recordId: DidCommID): IO[PresentationError, Option[PresentationRecord]] = ???

      override def getPresentationRecordByThreadId(thid: DidCommID): IO[PresentationError, Option[PresentationRecord]] =
        ???

      override def receiveProposePresentation(request: ProposePresentation): IO[PresentationError, PresentationRecord] =
        ???

      override def acceptProposePresentation(recordId: DidCommID): IO[PresentationError, PresentationRecord] = ???

      override def markRequestPresentationRejected(recordId: DidCommID): IO[PresentationError, PresentationRecord] = ???

      override def markProposePresentationSent(recordId: DidCommID): IO[PresentationError, PresentationRecord] = ???

      override def reportProcessingFailure(
          recordId: DidCommID,
          failReason: Option[String]
      ): IO[PresentationError, Unit] = ???
    }
  }

}
