package io.iohk.atala.connect.core.repository

import io.iohk.atala.connect.core.model.ConnectionRecord
import io.iohk.atala.connect.core.model.ConnectionRecord.ProtocolState
import io.iohk.atala.connect.core.model.error.ConnectionRepositoryError.*
import io.iohk.atala.mercury.protocol.connection.{ConnectionRequest, ConnectionResponse}
import zio.*

import java.time.Instant
import java.util.UUID

class ConnectionRepositoryInMemory(storeRef: Ref[Map[UUID, ConnectionRecord]]) extends ConnectionRepository[Task] {

  override def updateWithConnectionResponse(
      recordId: UUID,
      response: ConnectionResponse,
      state: ProtocolState,
      maxRetries: Int,
  ): Task[Int] = {
    for {
      maybeRecord <- getConnectionRecord(recordId)
      count <- maybeRecord
        .map(record =>
          for {
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
  ): Task[Int] = {
    for {
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

  override def deleteConnectionRecord(recordId: UUID): Task[Int] = {
    for {
      maybeRecord <- getConnectionRecord(recordId)
      count <- maybeRecord
        .map(record =>
          for {
            _ <- storeRef.update(r => r.removed(recordId))
          } yield 1
        )
        .getOrElse(ZIO.succeed(0))
    } yield count
  }

  override def updateWithConnectionRequest(
      recordId: UUID,
      request: ConnectionRequest,
      state: ProtocolState,
      maxRetries: Int,
  ): Task[Int] = {
    for {
      maybeRecord <- getConnectionRecord(recordId)
      count <- maybeRecord
        .map(record =>
          for {
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
          } yield 1
        )
        .getOrElse(ZIO.succeed(0))
    } yield count
  }

  def updateAfterFail(
      recordId: UUID,
      failReason: Option[String],
  ): Task[Int] = for {
    maybeRecord <- getConnectionRecord(recordId)
    count <- maybeRecord
      .map(record =>
        for {
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

  override def getConnectionRecordByThreadId(thid: String): Task[Option[ConnectionRecord]] = {
    for {
      store <- storeRef.get
    } yield store.values.find(_.thid.toString == thid)
  }

  override def getConnectionRecords: Task[Seq[ConnectionRecord]] = {
    for {
      store <- storeRef.get
    } yield store.values.toSeq
  }

  override def getConnectionRecordsByStates(
      ignoreWithZeroRetries: Boolean,
      limit: Int,
      states: ConnectionRecord.ProtocolState*
  ): Task[Seq[ConnectionRecord]] = {
    for {
      store <- storeRef.get
    } yield store.values
      .filter(rec => (ignoreWithZeroRetries & rec.metaRetries > 0) & states.contains(rec.protocolState))
      .take(limit)
      .toSeq
  }

  override def createConnectionRecord(record: ConnectionRecord): Task[Int] = {
    for {
      _ <- for {
        store <- storeRef.get
        maybeRecord <- ZIO.succeed(store.values.find(_.thid == record.thid))
        _ <- maybeRecord match
          case None        => ZIO.unit
          case Some(value) => ZIO.fail(UniqueConstraintViolation("Unique Constraint Violation on 'thid'"))
      } yield ()
      _ <- storeRef.update(r => r + (record.id -> record))
    } yield 1
  }

  override def getConnectionRecord(recordId: UUID): Task[Option[ConnectionRecord]] = {
    for {
      store <- storeRef.get
      record = store.get(recordId)
    } yield record
  }

}

object ConnectionRepositoryInMemory {
  val layer: ULayer[ConnectionRepository[Task]] = ZLayer.fromZIO(
    Ref
      .make(Map.empty[UUID, ConnectionRecord])
      .map(ConnectionRepositoryInMemory(_))
  )
}
