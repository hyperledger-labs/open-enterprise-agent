package io.iohk.atala.connect.core.repository

import io.iohk.atala.connect.core.model.ConnectionRecord
import io.iohk.atala.connect.core.model.ConnectionRecord.ProtocolState
import io.iohk.atala.connect.core.model.error.ConnectionRepositoryError.*
import io.iohk.atala.mercury.protocol.connection.ConnectionRequest
import io.iohk.atala.mercury.protocol.connection.ConnectionResponse
import io.iohk.atala.shared.models.WalletAccessContext
import io.iohk.atala.shared.models.WalletId
import zio.*

import java.time.Instant
import java.util.UUID

class ConnectionRepositoryInMemory(walletRefs: Ref[Map[WalletId, Ref[Map[UUID, ConnectionRecord]]]])
    extends ConnectionRepository {

  private def walletStoreRef: URIO[WalletAccessContext, Ref[Map[UUID, ConnectionRecord]]] =
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      refs <- walletRefs.get
      maybeWalletRef = refs.get(walletId)
      walletRef <- maybeWalletRef
        .fold {
          for {
            ref <- Ref.make(Map.empty[UUID, ConnectionRecord])
            _ <- walletRefs.set(refs.updated(walletId, ref))
          } yield ref
        }(ZIO.succeed)
    } yield walletRef

  override def updateWithConnectionResponse(
      recordId: UUID,
      response: ConnectionResponse,
      state: ProtocolState,
      maxRetries: Int,
  ): URIO[WalletAccessContext, Int] = {
    for {
      maybeRecord <- getConnectionRecord(recordId)
      count <- maybeRecord
        .map(record =>
          for {
            storeRef <- walletStoreRef
            _ <- storeRef.update(r =>
              r.updated(
                recordId,
                record.copy(
                  updatedAt = Some(Instant.now),
                  connectionResponse = Some(response),
                  protocolState = state,
                  metaRetries = maxRetries,
                  metaLastFailure = None,
                )
              )
            )
          } yield 1
        )
        .getOrElse(ZIO.succeed(1))
    } yield count
  }

  override def updateConnectionProtocolState(
      recordId: UUID,
      from: ProtocolState,
      to: ProtocolState,
      maxRetries: Int
  ): URIO[WalletAccessContext, Int] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      maybeRecord = store
        .find((uuid, record) => uuid == recordId && record.protocolState == from)
        .map(_._2)
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
  }

  override def deleteConnectionRecord(recordId: UUID): URIO[WalletAccessContext, Unit] = {
    for {
      maybeRecord <- getConnectionRecord(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      storeRef <- walletStoreRef
      _ <- storeRef.update(r => r.removed(recordId))
    } yield ()
  }

  override def updateWithConnectionRequest(
      recordId: UUID,
      request: ConnectionRequest,
      state: ProtocolState,
      maxRetries: Int,
  ): URIO[WalletAccessContext, Unit] = {
    for {
      maybeRecord <- getConnectionRecord(recordId)
      record <- ZIO.getOrFailWith(new RuntimeException(s"Record not found for Id: $recordId"))(maybeRecord).orDie
      storeRef <- walletStoreRef
      _ <- storeRef.update(r =>
        r.updated(
          recordId,
          record.copy(
            updatedAt = Some(Instant.now),
            connectionRequest = Some(request),
            protocolState = state,
            metaRetries = maxRetries,
            metaLastFailure = None,
          )
        )
      )
    } yield ()
  }

  def updateAfterFail(
      recordId: UUID,
      failReason: Option[String],
  ): URIO[WalletAccessContext, Int] = for {
    maybeRecord <- getConnectionRecord(recordId)
    count <- maybeRecord
      .map(record =>
        for {
          storeRef <- walletStoreRef
          _ <- storeRef.update(r =>
            r.updated(
              recordId,
              record.copy(
                metaRetries = record.metaRetries - 1,
                metaLastFailure = failReason,
              )
            )
          )
        } yield 1
      )
      .getOrElse(ZIO.succeed(0))
  } yield count

  def updateAfterFailForAllWallets(
      recordId: UUID,
      failReason: Option[String],
  ): Task[Int] = walletRefs.get.flatMap { wallets =>
    ZIO.foldLeft(wallets.values)(0) { (acc, walletRef) =>
      for {
        records <- walletRef.get
        count <- records.get(recordId) match {
          case Some(record) =>
            walletRef.update { r =>
              r.updated(
                recordId,
                record.copy(
                  metaRetries = record.metaRetries - 1,
                  metaLastFailure = failReason
                )
              )
            } *> ZIO.succeed(1) // Record updated, count as 1 update
          case None => ZIO.succeed(0) // No record updated
        }
      } yield acc + count
    }
  }

  override def getConnectionRecordByThreadId(thid: String): URIO[WalletAccessContext, Option[ConnectionRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values.find(_.thid.toString == thid)
  }

  override def getConnectionRecords: URIO[WalletAccessContext, Seq[ConnectionRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values.toSeq
  }

  override def getConnectionRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): URIO[WalletAccessContext, Seq[ConnectionRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values
      .filter(rec => (ignoreWithZeroRetries & rec.metaRetries > 0) & states.contains(rec.protocolState))
      .take(limit)
      .toSeq
  }

  override def getConnectionRecordsByStatesForAllWallets(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): UIO[Seq[ConnectionRecord]] = {

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

  override def createConnectionRecord(record: ConnectionRecord): URIO[WalletAccessContext, Unit] = {
    for {
      _ <- for {
        storeRef <- walletStoreRef
        store <- storeRef.get
        maybeRecord <- ZIO.succeed(store.values.find(_.thid == record.thid))
        _ <- maybeRecord match
          case Some(value) => throw RuntimeException("Unique constraint violation on 'thid'")
          case None        => ZIO.unit
      } yield ()
      storeRef <- walletStoreRef
      _ <- storeRef.update(r => r + (record.id -> record))
    } yield ()
  }

  override def getConnectionRecord(recordId: UUID): URIO[WalletAccessContext, Option[ConnectionRecord]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      record = store.get(recordId)
    } yield record
  }

}

object ConnectionRepositoryInMemory {
  val layer: ULayer[ConnectionRepository] = ZLayer.fromZIO(
    Ref
      .make(Map.empty[WalletId, Ref[Map[UUID, ConnectionRecord]]])
      .map(ConnectionRepositoryInMemory(_))
  )
}
