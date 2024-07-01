package org.hyperledger.identus.pollux.core.service

import org.hyperledger.identus.mercury.model.*
import org.hyperledger.identus.mercury.protocol.presentproof.*
import org.hyperledger.identus.pollux.anoncreds.AnoncredPresentation
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.model.error.PresentationError
import org.hyperledger.identus.pollux.core.model.presentation.*
import org.hyperledger.identus.pollux.core.service.serdes.{AnoncredCredentialProofsV1, AnoncredPresentationRequestV1}
import org.hyperledger.identus.pollux.sdjwt.{HolderPrivateKey, PresentationCompact}
import org.hyperledger.identus.pollux.vc.jwt.*
import org.hyperledger.identus.shared.models.*
import zio.*
import zio.json.ast

import java.time.Instant
import java.util.UUID
import java.util as ju

trait PresentationService {
  def extractIdFromCredential(credential: W3cCredentialPayload): Option[UUID]

  def createJwtPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      options: Option[org.hyperledger.identus.pollux.core.model.presentation.Options],
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def createSDJWTPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      proofTypes: Seq[ProofType],
      claimsToDisclose: ast.Json.Obj,
      options: Option[org.hyperledger.identus.pollux.core.model.presentation.Options],
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def createAnoncredPresentationRecord(
      pairwiseVerifierDID: DidId,
      pairwiseProverDID: DidId,
      thid: DidCommID,
      connectionId: Option[String],
      presentationRequest: AnoncredPresentationRequestV1
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def getPresentationRecords(
      ignoreWithZeroRetries: Boolean
  ): ZIO[WalletAccessContext, PresentationError, Seq[PresentationRecord]]

  def createJwtPresentationPayloadFromRecord(
      record: DidCommID,
      issuer: Issuer,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, PresentationPayload]

  def createPresentationFromRecord(
      record: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationCompact]

  def createSDJwtPresentation(
      recordId: DidCommID,
      requestPresentation: RequestPresentation,
      optionalHolderPrivateKey: Option[HolderPrivateKey],
  ): ZIO[WalletAccessContext, PresentationError, Presentation]

  def createAnoncredPresentationPayloadFromRecord(
      record: DidCommID,
      anoncredCredentialProof: AnoncredCredentialProofsV1,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, AnoncredPresentation]

  def createAnoncredPresentation(
      requestPresentation: RequestPresentation,
      recordId: DidCommID,
      anoncredCredentialProof: AnoncredCredentialProofsV1,
      issuanceDate: Instant
  ): ZIO[WalletAccessContext, PresentationError, Presentation]

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

  def findPresentationRecord(
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, Option[PresentationRecord]]

  def findPresentationRecordByThreadId(
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

  def acceptSDJWTRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: Seq[String],
      claimsToDisclose: Option[ast.Json.Obj]
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def acceptAnoncredRequestPresentation(
      recordId: DidCommID,
      credentialsToUse: AnoncredCredentialProofsV1
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

  def verifyAnoncredPresentation(
      presentation: Presentation,
      requestPresentation: RequestPresentation,
      recordId: DidCommID
  ): ZIO[WalletAccessContext, PresentationError, PresentationRecord]

  def reportProcessingFailure(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): IO[PresentationError, Unit]

}
