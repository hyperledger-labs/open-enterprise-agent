package org.hyperledger.identus.pollux.core.repository

import org.hyperledger.identus.mercury.protocol.presentproof.*
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.model.PresentationRecord.ProtocolState
import org.hyperledger.identus.shared.models.*
import zio.*

trait PresentationRepository {
  def createPresentationRecord(record: PresentationRecord): URIO[WalletAccessContext, Unit]

  def getPresentationRecords(ignoreWithZeroRetries: Boolean): URIO[WalletAccessContext, Seq[PresentationRecord]]

  def findPresentationRecord(recordId: DidCommID): URIO[WalletAccessContext, Option[PresentationRecord]]

  def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[PresentationRecord]]

  def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: PresentationRecord.ProtocolState*
  ): UIO[Seq[PresentationRecord]]

  def findPresentationRecordByThreadId(thid: DidCommID): URIO[WalletAccessContext, Option[PresentationRecord]]

  def updatePresentationRecordProtocolState(
      recordId: DidCommID,
      from: PresentationRecord.ProtocolState,
      to: PresentationRecord.ProtocolState
  ): URIO[WalletAccessContext, Unit]

  def updateWithRequestPresentation(
      recordId: DidCommID,
      request: RequestPresentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit]

  def updateWithProposePresentation(
      recordId: DidCommID,
      request: ProposePresentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit]

  def updateWithPresentation(
      recordId: DidCommID,
      presentation: Presentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit]

  def updatePresentationWithCredentialsToUse(
      recordId: DidCommID,
      credentialsToUse: Option[Seq[String]],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit]

  def updateSDJWTPresentationWithCredentialsToUse(
      recordId: DidCommID,
      credentialsToUse: Option[Seq[String]],
      sdJwtClaimsToDisclose: Option[SdJwtCredentialToDisclose],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit]

  def updateAnoncredPresentationWithCredentialsToUse(
      recordId: DidCommID,
      anoncredCredentialsToUseJsonSchemaId: Option[String],
      anoncredCredentialsToUse: Option[AnoncredCredentialProofs],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit]

  def updateAfterFail(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): UIO[Unit]
}
