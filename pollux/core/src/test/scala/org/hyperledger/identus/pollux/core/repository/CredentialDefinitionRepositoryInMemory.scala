package org.hyperledger.identus.pollux.core.repository

import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.core.model.schema.CredentialDefinition
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletId}
import zio.*

import java.util.UUID

class CredentialDefinitionRepositoryInMemory(
    walletRefs: Ref[Map[WalletId, Ref[Map[UUID, CredentialDefinition]]]]
) extends CredentialDefinitionRepository {

  private def walletStoreRef: URIO[WalletAccessContext, Ref[Map[UUID, CredentialDefinition]]] =
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      refs <- walletRefs.get
      maybeWalletRef = refs.get(walletId)
      walletRef <- maybeWalletRef
        .fold {
          for {
            ref <- Ref.make(Map.empty[UUID, CredentialDefinition])
            _ <- walletRefs.set(refs.updated(walletId, ref))
          } yield ref
        }(ZIO.succeed)
    } yield walletRef

  override def create(record: CredentialDefinition): URIO[WalletAccessContext, CredentialDefinition] = {
    for {
      storeRef <- walletStoreRef
      _ <- for {
        store <- storeRef.get
        maybeRecord = store.values
          .find(_.id == record.guid)
          .foreach(_ => throw RuntimeException("Unique Constraint Violation on 'id'"))
      } yield ()
      _ <- storeRef.update(r => r + (record.guid -> record))
    } yield record
  }

  override def findByGuid(guid: UUID): UIO[Option[CredentialDefinition]] = {
    for {
      storeRefs <- walletRefs.get
      storeRefOption <- ZIO.filter(storeRefs.values)(storeRef => storeRef.get.map(_.contains(guid))).map(_.headOption)
      record <- storeRefOption match {
        case Some(storeRef) => storeRef.get.map(_.get(guid))
        case None           => ZIO.none
      }
    } yield record
  }

  override def update(cs: CredentialDefinition): URIO[WalletAccessContext, CredentialDefinition] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      existing = store.getOrElse(cs.id, throw new IllegalStateException("Entity doesn't exists"))
      _ <- {
        val updatedStore = store.updated(cs.id, cs)
        storeRef.set(updatedStore)
      }
    } yield existing
  }

  override def getAllVersions(id: UUID, author: String): URIO[WalletAccessContext, Seq[String]] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
    } yield store.values
      .filter(credDef => credDef.id == id && credDef.author == author)
      .map(_.version)
      .toSeq
  }

  override def delete(guid: UUID): URIO[WalletAccessContext, CredentialDefinition] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      record = store.getOrElse(guid, throw new IllegalStateException("Entity doesn't exists"))
      _ <- storeRef.update(r => r - record.id)
    } yield record
  }

  override def deleteAll(): URIO[WalletAccessContext, Unit] = {
    for {
      storeRef <- walletStoreRef
      store <- storeRef.get
      _ <- storeRef.update(Map.empty)
    } yield ZIO.unit
  }

  override def search(
      query: Repository.SearchQuery[CredentialDefinition.Filter]
  ): URIO[WalletAccessContext, Repository.SearchResult[CredentialDefinition]] = {
    walletStoreRef.flatMap { storeRef =>
      storeRef.get.map { store =>
        val filtered = store.values.filter { credDef =>
          query.filter.author.forall(_ == credDef.author) &&
          query.filter.name.forall(_ == credDef.name) &&
          query.filter.version.forall(_ == credDef.version) &&
          query.filter.tag.forall(tag => credDef.tag == tag)
        }
        val paginated = filtered.slice(query.skip, query.skip + query.limit)
        Repository.SearchResult(paginated.toSeq, paginated.size, filtered.size)
      }
    }
  }
}

object CredentialDefinitionRepositoryInMemory {
  val layer: ULayer[CredentialDefinitionRepository] = ZLayer.fromZIO(
    Ref
      .make(Map.empty[WalletId, Ref[Map[UUID, CredentialDefinition]]])
      .map(CredentialDefinitionRepositoryInMemory(_))
  )
}
