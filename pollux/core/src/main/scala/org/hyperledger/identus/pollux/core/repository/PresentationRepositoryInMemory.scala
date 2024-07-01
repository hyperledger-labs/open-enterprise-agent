/*
package org.hyperledger.identus.pollux.core.repository

import org.hyperledger.identus.mercury.protocol.presentproof.*
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.model.PresentationRecord.ProtocolState
import org.hyperledger.identus.shared.db.Implicits.*
import org.hyperledger.identus.shared.models.*
import zio.*
import zio.json.*
import zio.json.ast.Json

import java.time.Instant

class PresentationRepositoryInMemory(
    walletRefs: Ref[Map[WalletId, Ref[Map[DidCommID, PresentationRecord]]]],
    maxRetries: Int
) extends PresentationRepository {

  private def walletStoreRef: URIO[WalletAccessContext, Ref[Map[DidCommID, PresentationRecord]]] =
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      refs <- walletRefs.get
      maybeWalletRef = refs.get(walletId)
      walletRef <- maybeWalletRef
        .fold {
          for {
            ref <- Ref.make(Map.empty[DidCommID, PresentationRecord])
            _ <- walletRefs.set(refs.updated(walletId, ref))
          } yield ref
        }(ZIO.succeed)
    } yield walletRef

  override def createPresentationRecord(record: PresentationRecord): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        _ <- for {
          store <- storeRef.get
          maybeRecord = store.values.find(_.thid == record.thid)
        } yield ()
        _ <- storeRef.update(r => r + (record.id -> record))
      } yield 1
    result.ensureOneAffectedRowOrDie
  }

  override def findPresentationRecord(recordId: DidCommID): URIO[WalletAccessContext, Option[PresentationRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      record = store.get(recordId)
    } yield record
  }

  override def getPresentationRecords(
      ignoreWithZeroRetries: Boolean,
  ): URIO[WalletAccessContext, Seq[PresentationRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values.toSeq
  }

  override def updatePresentationRecordProtocolState(
      recordId: DidCommID,
      from: ProtocolState,
      to: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        store <- storeRef.get
        maybeRecord = store.find((id, record) => id == recordId && record.protocolState == from).map(_._2)
        count <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    protocolState = to,
                    metaRetries = maxRetries,
                    metaLastFailure = None,
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield count
    result.ensureOneAffectedRowOrDie
  }

  override def updatePresentationWithCredentialsToUse(
      recordId: DidCommID,
      credentialsToUse: Option[Seq[String]],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        maybeRecord <- findPresentationRecord(recordId)
        count <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    updatedAt = Some(Instant.now),
                    credentialsToUse = credentialsToUse.map(_.toList),
                    protocolState = protocolState,
                    metaRetries = maxRetries,
                    metaLastFailure = None,
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield count
    result.ensureOneAffectedRowOrDie
  }

  override def updateSDJWTPresentationWithCredentialsToUse(
      recordId: DidCommID,
      credentialsToUse: Option[Seq[String]],
      sdJwtClaimsToDisclose: Option[SdJwtCredentialToDisclose],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val result = {
      for {
        storeRef <- walletStoreRef
        maybeRecord <- findPresentationRecord(recordId)
        result <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    updatedAt = Some(Instant.now),
                    credentialsToUse = credentialsToUse.map(_.toList),
                    sdJwtClaimsToDisclose = sdJwtClaimsToDisclose,
                    protocolState = protocolState,
                    metaRetries = maxRetries,
                    metaLastFailure = None,
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield result
    }
    result.ensureOneAffectedRowOrDie
  }

  def updateAnoncredPresentationWithCredentialsToUse(
      recordId: DidCommID,
      anoncredCredentialsToUseJsonSchemaId: Option[String],
      anoncredCredentialsToUse: Option[AnoncredCredentialProofs],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        maybeRecord <- findPresentationRecord(recordId)
        count <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    updatedAt = Some(Instant.now),
                    anoncredCredentialsToUseJsonSchemaId = anoncredCredentialsToUseJsonSchemaId,
                    anoncredCredentialsToUse = anoncredCredentialsToUse,
                    protocolState = protocolState,
                    metaRetries = maxRetries,
                    metaLastFailure = None,
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield count
    result.ensureOneAffectedRowOrDie
  }

  override def updateWithPresentation(
      recordId: DidCommID,
      presentation: Presentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        maybeRecord <- findPresentationRecord(recordId)
        count <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    updatedAt = Some(Instant.now),
                    presentationData = Some(presentation),
                    protocolState = protocolState,
                    metaRetries = maxRetries,
                    metaLastFailure = None,
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield count
    result.ensureOneAffectedRowOrDie
  }

  override def getPresentationRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): URIO[WalletAccessContext, Seq[PresentationRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values
      .filter(rec => states.contains(rec.protocolState) & (!ignoreWithZeroRetries | rec.metaRetries > 0))
      .take(limit)
      .toSeq
  }

  override def getPresentationRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): UIO[Seq[PresentationRecord]] = {
    for {
      refs <- walletRefs.get
      stores <- ZIO.foreach(refs.values.toList)(_.get)
    } yield {
      stores
        .flatMap(_.values)
        .filter { rec =>
          (!ignoreWithZeroRetries || rec.metaRetries > 0) &&
          states.contains(rec.protocolState)
        }
        .take(limit)
        .toSeq
    }
  }

  override def findPresentationRecordByThreadId(
      thid: DidCommID,
  ): URIO[WalletAccessContext, Option[PresentationRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values.find(_.thid == thid).filter(_.metaRetries > 0)
  }

  override def updateWithRequestPresentation(
      recordId: DidCommID,
      request: RequestPresentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        maybeRecord <- findPresentationRecord(recordId)
        count <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    updatedAt = Some(Instant.now),
                    requestPresentationData = Some(request),
                    protocolState = protocolState,
                    metaRetries = maxRetries,
                    metaLastFailure = None,
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield count
    result.ensureOneAffectedRowOrDie
  }

  override def updateWithProposePresentation(
      recordId: DidCommID,
      request: ProposePresentation,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        maybeRecord <- findPresentationRecord(recordId)
        count <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    updatedAt = Some(Instant.now),
                    proposePresentationData = Some(request),
                    protocolState = protocolState,
                    metaRetries = maxRetries,
                    metaLastFailure = None,
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield count
    result.ensureOneAffectedRowOrDie
  }

  override def updateAfterFail(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): URIO[WalletAccessContext, Unit] = {
    val result =
      for {
        storeRef <- walletStoreRef
        maybeRecord <- findPresentationRecord(recordId)
        count <- maybeRecord
          .map(record =>
            for {
              _ <- storeRef.update(r =>
                r.updated(
                  recordId,
                  record.copy(
                    metaRetries = math.max(0, record.metaRetries - 1),
                    metaNextRetry =
                      if (record.metaRetries - 1 <= 0) None
                      else Some(Instant.now().plusSeconds(60)), // TODO exponention time
                    metaLastFailure = failReason
                  )
                )
              )
            } yield 1
          )
          .getOrElse(ZIO.succeed(0))
      } yield count
    result.ensureOneAffectedRowOrDie
  }
}

object PresentationRepositoryInMemory {
  val maxRetries = 5 // TODO Move to config
  val layer: ULayer[PresentationRepository] = ZLayer.fromZIO(
    Ref
      .make(Map.empty[WalletId, Ref[Map[DidCommID, PresentationRecord]]])
      .map(PresentationRepositoryInMemory(_, maxRetries))
  )
}
 */
