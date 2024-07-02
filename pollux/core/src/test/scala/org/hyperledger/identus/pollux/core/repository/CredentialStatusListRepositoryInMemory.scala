package org.hyperledger.identus.pollux.core.repository

import org.hyperledger.identus.castor.core.model.did.{CanonicalPrismDID, PrismDID}
import org.hyperledger.identus.pollux.core.model.*
import org.hyperledger.identus.pollux.vc.jwt.{revocation, Issuer, StatusPurpose}
import org.hyperledger.identus.pollux.vc.jwt.revocation.{BitString, VCStatusList2021}
import org.hyperledger.identus.pollux.vc.jwt.revocation.BitStringError.{
  DecodingError,
  EncodingError,
  IndexOutOfBounds,
  InvalidSize
}
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletId}
import zio.*

import java.time.Instant
import java.util.UUID

class CredentialStatusListRepositoryInMemory(
    walletToStatusListRefs: Ref[Map[WalletId, Ref[Map[UUID, CredentialStatusList]]]],
    statusListToCredInStatusListRefs: Ref[Map[UUID, Ref[Map[UUID, CredentialInStatusList]]]]
) extends CredentialStatusListRepository {

  private def walletToStatusListStorageRefs: URIO[WalletAccessContext, Ref[Map[UUID, CredentialStatusList]]] =
    for {
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      refs <- walletToStatusListRefs.get
      maybeWalletRef = refs.get(walletId)
      walletRef <- maybeWalletRef
        .fold {
          for {
            ref <- Ref.make(Map.empty[UUID, CredentialStatusList])
            _ <- walletToStatusListRefs.set(refs.updated(walletId, ref))
          } yield ref
        }(ZIO.succeed)
    } yield walletRef

  private def allStatusListsStorageRefs: UIO[Ref[Map[UUID, CredentialStatusList]]] =
    for {
      refs <- walletToStatusListRefs.get
      allRefs = refs.values.toList
      allRefsMap <- ZIO
        .collectAll(allRefs.map(_.get))
        .map(_.foldLeft(Map.empty[UUID, CredentialStatusList]) { (acc, value) =>
          acc ++ value
        })
      ref <- Ref.make(allRefsMap)
    } yield ref

  private def statusListToCredInStatusListStorageRefs(
      statusListId: UUID
  ): UIO[Ref[Map[UUID, CredentialInStatusList]]] =
    for {
      refs <- statusListToCredInStatusListRefs.get
      maybeStatusListIdRef = refs.get(statusListId)
      statusListIdRef <- maybeStatusListIdRef.fold {
        for {
          ref <- Ref.make(Map.empty[UUID, CredentialInStatusList])
          _ <- statusListToCredInStatusListRefs.set(refs.updated(statusListId, ref))
        } yield ref
      }(ZIO.succeed)
    } yield statusListIdRef

  def findById(id: UUID): UIO[Option[CredentialStatusList]] = for {
    refs <- walletToStatusListRefs.get
    stores <- ZIO.foreach(refs.values.toList)(_.get)
    found = stores.flatMap(_.values).find(_.id == id)
  } yield found

  override def existsForIssueCredentialRecordId(id: DidCommID): UIO[Boolean] = for {
    refs <- statusListToCredInStatusListRefs.get
    stores <- ZIO.foreach(refs.values)(_.get)
    exists = stores.flatMap(_.values).exists(_.issueCredentialRecordId == id)
  } yield exists

  def getLatestOfTheWallet: URIO[WalletAccessContext, Option[CredentialStatusList]] = for {
    storageRef <- walletToStatusListStorageRefs
    storage <- storageRef.get
    latest = storage.toSeq
      .sortBy(_._2.createdAt) { (x, y) => if x.isAfter(y) then -1 else 1 /* DESC */ }
      .headOption
      .map(_._2)
  } yield latest

  def createNewForTheWallet(
      jwtIssuer: Issuer,
      statusListRegistryUrl: String
  ): URIO[WalletAccessContext, CredentialStatusList] = {

    val id = UUID.randomUUID()
    val issued = Instant.now()
    val issuerDid = jwtIssuer.did.value
    val canonical = PrismDID.fromString(issuerDid).fold(e => throw RuntimeException(e), _.asCanonical)

    val embeddedProofCredential = for {
      bitString <- BitString.getInstance().mapError {
        case InvalidSize(message)      => new Throwable(message)
        case EncodingError(message)    => new Throwable(message)
        case DecodingError(message)    => new Throwable(message)
        case IndexOutOfBounds(message) => new Throwable(message)
      }
      emptyJwtCredential <- VCStatusList2021
        .build(
          vcId = s"$statusListRegistryUrl/credential-status/$id",
          revocationData = bitString,
          jwtIssuer = jwtIssuer
        )
        .mapError(x => new Throwable(x.msg))

      credentialWithEmbeddedProof <- emptyJwtCredential.toJsonWithEmbeddedProof
    } yield credentialWithEmbeddedProof.spaces2

    for {
      credential <- embeddedProofCredential.orDie
      storageRef <- walletToStatusListStorageRefs
      walletId <- ZIO.serviceWith[WalletAccessContext](_.walletId)
      newCredentialStatusList = CredentialStatusList(
        id = id,
        walletId = walletId,
        issuer = canonical,
        issued = issued,
        purpose = StatusPurpose.Revocation,
        statusListCredential = credential,
        size = BitString.MIN_SL2021_SIZE,
        lastUsedIndex = 0,
        createdAt = Instant.now(),
        updatedAt = None
      )
      _ <- storageRef.update(r => r + (newCredentialStatusList.id -> newCredentialStatusList))
    } yield newCredentialStatusList

  }

  def allocateSpaceForCredential(
      issueCredentialRecordId: DidCommID,
      credentialStatusListId: UUID,
      statusListIndex: Int
  ): URIO[WalletAccessContext, Unit] = {
    val newCredentialInStatusList = CredentialInStatusList(
      id = UUID.randomUUID(),
      issueCredentialRecordId = issueCredentialRecordId,
      credentialStatusListId = credentialStatusListId,
      statusListIndex = statusListIndex,
      isCanceled = false,
      isProcessed = false,
      createdAt = Instant.now(),
      updatedAt = None
    )

    for {
      credentialInStatusListStorageRef <- statusListToCredInStatusListStorageRefs(credentialStatusListId)
      _ <- credentialInStatusListStorageRef.update(r => r + (newCredentialInStatusList.id -> newCredentialInStatusList))
      walletToStatusListStorageRef <- walletToStatusListStorageRefs
      _ <- walletToStatusListStorageRef.update(r => {
        val value = r.get(credentialStatusListId)
        value.fold(r) { v =>
          val updated = v.copy(lastUsedIndex = statusListIndex, updatedAt = Some(Instant.now))
          r.updated(credentialStatusListId, updated)
        }
      })
    } yield ()

  }

  def revokeByIssueCredentialRecordId(
      issueCredentialRecordId: DidCommID
  ): URIO[WalletAccessContext, Unit] = {
    for {
      statusListsRefs <- walletToStatusListStorageRefs
      statusLists <- statusListsRefs.get
      credInStatusListsRefs <- ZIO
        .collectAll(statusLists.keys.map(k => statusListToCredInStatusListStorageRefs(k)))
        .map(_.toVector)
      _ = credInStatusListsRefs.foreach(ref =>
        ref.update { credInStatusListsMap =>
          val maybeFound = credInStatusListsMap.find(_._2.issueCredentialRecordId == issueCredentialRecordId)
          maybeFound.fold(credInStatusListsMap) { case (id, value) =>
            if (!value.isCanceled) {
              credInStatusListsMap.updated(id, value.copy(isCanceled = true, updatedAt = Some(Instant.now())))
              credInStatusListsMap
            } else credInStatusListsMap
          }

        }
      )
    } yield ()
  }

  def getCredentialStatusListsWithCreds: UIO[List[CredentialStatusListWithCreds]] = {
    for {
      statusListsRefs <- allStatusListsStorageRefs
      statusLists <- statusListsRefs.get
      statusListWithCredEffects = statusLists.map { (id, statusList) =>
        val credsinStatusListEffect = statusListToCredInStatusListStorageRefs(id).flatMap(_.get.map(_.values.toList))
        credsinStatusListEffect.map { credsInStatusList =>
          CredentialStatusListWithCreds(
            id = id,
            walletId = statusList.walletId,
            issuer = statusList.issuer,
            issued = statusList.issued,
            purpose = statusList.purpose,
            statusListCredential = statusList.statusListCredential,
            size = statusList.size,
            lastUsedIndex = statusList.lastUsedIndex,
            credentials = credsInStatusList.map { cred =>
              CredInStatusList(
                id = cred.id,
                issueCredentialRecordId = cred.issueCredentialRecordId,
                statusListIndex = cred.statusListIndex,
                isCanceled = cred.isCanceled,
                isProcessed = cred.isProcessed,
              )
            }
          )
        }

      }.toList
      res <- ZIO.collectAll(statusListWithCredEffects)
    } yield res
  }

  def updateStatusListCredential(
      credentialStatusListId: UUID,
      statusListCredential: String
  ): URIO[WalletAccessContext, Unit] = {
    for {
      statusListsRefs <- walletToStatusListStorageRefs
      _ <- statusListsRefs.update { statusLists =>
        statusLists.updatedWith(credentialStatusListId) { maybeCredentialStatusList =>
          maybeCredentialStatusList.map { credentialStatusList =>
            credentialStatusList.copy(statusListCredential = statusListCredential, updatedAt = Some(Instant.now()))
          }
        }
      }
    } yield ()

  }

  def markAsProcessedMany(
      credsInStatusListIds: Seq[UUID]
  ): URIO[WalletAccessContext, Unit] = for {
    statusListsRefs <- walletToStatusListStorageRefs
    statusLists <- statusListsRefs.get
    credInStatusListsRefs <- ZIO
      .collectAll(statusLists.keys.map(k => statusListToCredInStatusListStorageRefs(k)))
      .map(_.toVector)
    _ = credInStatusListsRefs.foreach(ref =>
      ref.update { credInStatusListsMap =>
        credInStatusListsMap.transform { (id, credInStatusList) =>
          if (credsInStatusListIds.contains(id))
            credInStatusList.copy(isProcessed = true, updatedAt = Some(Instant.now()))
          else credInStatusList
        }
      }
    )
  } yield ()

}

object CredentialStatusListRepositoryInMemory {
  val layer: ULayer[CredentialStatusListRepositoryInMemory] = ZLayer.fromZIO(
    for {
      walletToStatusList <- Ref
        .make(Map.empty[WalletId, Ref[Map[UUID, CredentialStatusList]]])
      statusListIdToCredInStatusList <- Ref.make(Map.empty[UUID, Ref[Map[UUID, CredentialInStatusList]]])
    } yield CredentialStatusListRepositoryInMemory(walletToStatusList, statusListIdToCredInStatusList)
  )
}

private case class CredentialInStatusList(
    id: UUID,
    issueCredentialRecordId: DidCommID,
    credentialStatusListId: UUID,
    statusListIndex: Int,
    isCanceled: Boolean,
    isProcessed: Boolean,
    createdAt: Instant = Instant.now(),
    updatedAt: Option[Instant] = None
)
