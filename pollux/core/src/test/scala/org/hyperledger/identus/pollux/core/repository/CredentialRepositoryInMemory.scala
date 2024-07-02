package org.hyperledger.identus.pollux.core.repository

import org.hyperledger.identus.mercury.protocol.issuecredential.{IssueCredential, RequestCredential}
import org.hyperledger.identus.pollux.anoncreds.AnoncredCredentialRequestMetadata
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.model.IssueCredentialRecord.ProtocolState
import org.hyperledger.identus.shared.models.*
import zio.*

import java.time.Instant

class CredentialRepositoryInMemory(
    walletRefs: Ref[Map[WalletId, Ref[Map[DidCommID, IssueCredentialRecord]]]],
    maxRetries: Int
) extends CredentialRepository {

  private def walletStoreRef: URIO[WalletAccessContext, Ref[Map[DidCommID, IssueCredentialRecord]]] =
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      refs <- walletRefs.get
      maybeWalletRef = refs.get(walletId)
      walletRef <- maybeWalletRef
        .fold {
          for {
            ref <- Ref.make(Map.empty[DidCommID, IssueCredentialRecord])
            _ <- walletRefs.set(refs.updated(walletId, ref))
          } yield ref
        }(ZIO.succeed)
    } yield walletRef

  override def create(record: IssueCredentialRecord): URIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      _ <- for {
        store <- storeRef.get
        maybeRecord = store.values.find(_.thid == record.thid)
        _ <- maybeRecord match
          case None        => ZIO.unit
          case Some(value) => ZIO.die(RuntimeException("Unique Constraint Violation on 'thid'"))
      } yield ()
      _ <- storeRef.update(r => r + (record.id -> record))
    } yield ()
  }

  override def getById(
      recordId: DidCommID
  ): URIO[WalletAccessContext, IssueCredentialRecord] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      maybeRecord = store.get(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
    } yield record
  }

  override def findById(
      recordId: DidCommID
  ): URIO[WalletAccessContext, Option[IssueCredentialRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      record = store.get(recordId)
    } yield record
  }

  override def findAll(
      ignoreWithZeroRetries: Boolean,
      offset: Option[Int],
      limit: Option[Int]
  ): URIO[WalletAccessContext, (Seq[IssueCredentialRecord], RuntimeFlags)] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      records = if (ignoreWithZeroRetries) store.values.filter(_.metaRetries > 0) else store.values
      paginated = records.toSeq.drop(offset.getOrElse(0)).take(limit.getOrElse(Int.MaxValue))
    } yield paginated -> store.values.size
  }

  override def updateProtocolState(
      recordId: DidCommID,
      from: ProtocolState,
      to: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    for {
      record <- getById(recordId)
      storeRef <- walletStoreRef
      _ <-
        if (record.protocolState != from) ZIO.die(RuntimeException(s"Invalid protocol state: $from"))
        else
          storeRef.update(r =>
            r.updated(
              recordId,
              record.copy(
                protocolState = to,
                metaRetries = maxRetries,
                metaLastFailure = None,
              )
            )
          )
    } yield ()
  }

  override def updateWithIssuedRawCredential(
      recordId: DidCommID,
      issue: IssueCredential,
      issuedRawCredential: String,
      schemaUri: Option[String],
      credentialDefinitionUri: Option[String],
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      maybeRecord <- findById(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      _ <- storeRef.update(r =>
        r.updated(
          recordId,
          record.copy(
            updatedAt = Some(Instant.now),
            schemaUri = schemaUri,
            credentialDefinitionUri = credentialDefinitionUri,
            issueCredentialData = Some(issue),
            issuedCredentialRaw = Some(issuedRawCredential),
            protocolState = protocolState,
            metaRetries = maxRetries,
            metaLastFailure = None,
          )
        )
      )
    } yield ()
  }

  override def findValidIssuedCredentials(
      recordId: Seq[DidCommID]
  ): URIO[WalletAccessContext, Seq[ValidIssuedCredentialRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values
      .filter(rec => recordId.contains(rec.id) && rec.issuedCredentialRaw.isDefined)
      .map(rec =>
        ValidIssuedCredentialRecord(rec.id, rec.issuedCredentialRaw, rec.credentialFormat, rec.subjectId, None)
      )
      .toSeq
  }

  override def findValidAnonCredsIssuedCredentials(
      recordId: Seq[DidCommID]
  ): URIO[WalletAccessContext, Seq[ValidFullIssuedCredentialRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values
      .filter(rec =>
        recordId.contains(
          rec.id
        ) && rec.issueCredentialData.isDefined
          && rec.schemaUri.isDefined
          && rec.credentialDefinitionUri.isDefined
          && rec.credentialFormat == CredentialFormat.AnonCreds
      )
      .map(rec =>
        ValidFullIssuedCredentialRecord(
          rec.id,
          rec.issueCredentialData,
          rec.credentialFormat,
          rec.schemaUri,
          rec.credentialDefinitionUri,
          rec.subjectId,
          rec.keyId,
        )
      )
      .toSeq
  }

  override def deleteById(recordId: DidCommID): URIO[WalletAccessContext, Unit] = {
    for {
      maybeRecord <- findById(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      storeRef <- walletStoreRef
      _ <- storeRef.update(r => r.removed(recordId))
    } yield ()
  }

  override def updateWithIssueCredential(
      recordId: DidCommID,
      issue: IssueCredential,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      maybeRecord <- findById(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      _ <- storeRef.update(r =>
        r.updated(
          recordId,
          record.copy(
            updatedAt = Some(Instant.now),
            issueCredentialData = Some(issue),
            protocolState = protocolState,
            metaRetries = maxRetries,
            metaLastFailure = None,
          )
        )
      )
    } yield ()
  }

  override def findByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): URIO[WalletAccessContext, Seq[IssueCredentialRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      records = if (ignoreWithZeroRetries) store.values.filter(_.metaRetries > 0) else store.values
    } yield records
      .filter(rec => states.contains(rec.protocolState))
      .take(limit)
      .toSeq
  }

  override def findByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ProtocolState*
  ): UIO[Seq[IssueCredentialRecord]] = {
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

  override def findByThreadId(
      thid: DidCommID,
      ignoreWithZeroRetries: Boolean,
  ): URIO[WalletAccessContext, Option[IssueCredentialRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      records = if (ignoreWithZeroRetries) store.values.filter(_.metaRetries > 0) else store.values
    } yield records.find(_.thid == thid)
  }

  override def updateWithSubjectId(
      recordId: DidCommID,
      subjectId: String,
      keyId: Option[KeyId] = None,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      maybeRecord <- findById(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      _ <- storeRef.update(r =>
        r.updated(
          recordId,
          record.copy(
            updatedAt = Some(Instant.now),
            protocolState = protocolState,
            subjectId = Some(subjectId),
            metaRetries = maxRetries,
            metaLastFailure = None,
          )
        )
      )
    } yield ()
  }

  override def updateWithJWTRequestCredential(
      recordId: DidCommID,
      request: RequestCredential,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      maybeRecord <- findById(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      _ <- storeRef.update(r =>
        r.updated(
          recordId,
          record.copy(
            updatedAt = Some(Instant.now),
            requestCredentialData = Some(request),
            protocolState = protocolState,
            metaRetries = maxRetries,
            metaLastFailure = None,
          )
        )
      )
    } yield ()
  }

  override def updateWithAnonCredsRequestCredential(
      recordId: DidCommID,
      request: RequestCredential,
      metadata: AnoncredCredentialRequestMetadata,
      protocolState: ProtocolState
  ): URIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      maybeRecord <- findById(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      _ <- storeRef.update(r =>
        r.updated(
          recordId,
          record.copy(
            updatedAt = Some(Instant.now),
            requestCredentialData = Some(request),
            anonCredsRequestMetadata = Some(metadata),
            protocolState = protocolState,
            metaRetries = maxRetries,
            metaLastFailure = None,
          )
        )
      )
    } yield ()
  }

  override def updateAfterFail(
      recordId: DidCommID,
      failReason: Option[Failure]
  ): URIO[WalletAccessContext, Unit] = for {
    storeRef <- walletStoreRef
    maybeRecord <- findById(recordId)
    record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
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
  } yield ()

}

object CredentialRepositoryInMemory {
  val maxRetries = 5 // TODO Move to config
  val layer: ULayer[CredentialRepository] = ZLayer.fromZIO(
    Ref
      .make(Map.empty[WalletId, Ref[Map[DidCommID, IssueCredentialRecord]]])
      .map(CredentialRepositoryInMemory(_, maxRetries))
  )
}
