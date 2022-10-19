package io.iohk.atala.agent.keymanagement.service

import io.iohk.atala.agent.keymanagement.crypto.KeyGeneratorWrapper
import io.iohk.atala.agent.keymanagement.model.{CommitmentPurpose, DIDPublicKeyTemplate, ECKeyPair, ManagedDIDTemplate}
import io.iohk.atala.agent.keymanagement.model.ECCoordinates.*
import io.iohk.atala.agent.keymanagement.model.error.CreateManagedDIDError
import io.iohk.atala.agent.keymanagement.service.ManagedDIDService.{CreateDIDSecret, KeyManagementConfig}
import io.iohk.atala.agent.keymanagement.storage.{DIDSecretStorage, InMemoryDIDSecretStorage}
import io.iohk.atala.castor.core.model.did.{
  DID,
  DIDDocument,
  DIDStorage,
  EllipticCurve,
  LongFormPrismDIDV1,
  PrismDID,
  PublicKey,
  PublicKeyJwk,
  PublishedDIDOperation
}
import io.iohk.atala.prism.crypto.Sha256
import io.iohk.atala.prism.crypto.util.Random
import io.iohk.atala.shared.models.Base64UrlStrings.*
import io.iohk.atala.shared.models.HexStrings.*
import zio.*

/** A wrapper around Castor's DIDService providing key-management capability. Analogous to the secretAPI in
  * indy-wallet-sdk.
  */
final class ManagedDIDService private[keymanagement] (secretStorage: DIDSecretStorage, config: KeyManagementConfig) {

  def createAndStoreDID(didTemplate: ManagedDIDTemplate): IO[CreateManagedDIDError, PrismDID] = {
    for {
      _ <- Console.printLine("creating and storing managed DID").ignore
      generated <- generateCreateOperation(didTemplate)
      (createOperation, secret) = generated
      longFormDID = LongFormPrismDIDV1.fromCreateOperation(createOperation)
      did = longFormDID.toCanonical
      _ <- secretStorage
        .upsertDIDCommitmentRevealValue(did, CommitmentPurpose.Update, secret.updateCommitmentRevealValue)
        .mapError(CreateManagedDIDError.SecretStorageError.apply)
      _ <- secretStorage
        .upsertDIDCommitmentRevealValue(did, CommitmentPurpose.Recovery, secret.recoveryCommitmentRevealValue)
        .mapError(CreateManagedDIDError.SecretStorageError.apply)
      _ <- ZIO
        .foreachDiscard(secret.keyPairs) { case (keyId, keyPair) => secretStorage.upsertKey(did, keyId, keyPair) }
        .mapError(CreateManagedDIDError.SecretStorageError.apply)
      _ <- ZIO.die(RuntimeException("DIE!!!"))
    } yield longFormDID
  }

  private def generateCreateOperation(
      didTemplate: ManagedDIDTemplate
  ): IO[CreateManagedDIDError, (PublishedDIDOperation.Create, CreateDIDSecret)] = {
    for {
      keys <- ZIO
        .foreach(didTemplate.publicKeys.sortBy(_.id))(generateKeyPairAndPublicKey)
        .mapError(CreateManagedDIDError.KeyGenerationError.apply)
      updateCommitmentRevealValue = Random.INSTANCE.bytesOfLength(config.updateCommitmentRevealByte)
      recoveryCommitmentRevealValue = Random.INSTANCE.bytesOfLength(config.recoveryCommitmentRevealByte)
      operation = PublishedDIDOperation.Create(
        updateCommitment = HexString.fromByteArray(Sha256.compute(updateCommitmentRevealValue).getValue),
        recoveryCommitment = HexString.fromByteArray(Sha256.compute(recoveryCommitmentRevealValue).getValue),
        storage = DIDStorage.Cardano(didTemplate.storage),
        document = DIDDocument(
          publicKeys = keys.map(_._2),
          services = didTemplate.services
        )
      )
      secret = CreateDIDSecret(
        updateCommitmentRevealValue = HexString.fromByteArray(updateCommitmentRevealValue),
        recoveryCommitmentRevealValue = HexString.fromByteArray(recoveryCommitmentRevealValue),
        keyPairs = keys.map { case (keyPair, template) => template.id -> keyPair }.toMap
      )
    } yield operation -> secret
  }

  private def generateKeyPairAndPublicKey(template: DIDPublicKeyTemplate): Task[(ECKeyPair, PublicKey)] = {
    val curve = EllipticCurve.SECP256K1
    for {
      keyPair <- KeyGeneratorWrapper.generateECKeyPair(curve)
      publicKey = PublicKey.JsonWebKey2020(
        id = template.id,
        purposes = Seq(template.purpose),
        publicKeyJwk = PublicKeyJwk.ECPublicKeyData(
          crv = curve,
          x = Base64UrlString.fromByteArray(keyPair.publicKey.p.x.toPaddedByteArray(curve)),
          y = Base64UrlString.fromByteArray(keyPair.publicKey.p.y.toPaddedByteArray(curve))
        )
      )
    } yield (keyPair, publicKey)
  }

}

object ManagedDIDService {

  private final case class CreateDIDSecret(
      updateCommitmentRevealValue: HexString,
      recoveryCommitmentRevealValue: HexString,
      keyPairs: Map[String, ECKeyPair]
  )

  final case class KeyManagementConfig(updateCommitmentRevealByte: Int, recoveryCommitmentRevealByte: Int)

  object KeyManagementConfig {
    val default: KeyManagementConfig = KeyManagementConfig(
      updateCommitmentRevealByte = 32,
      recoveryCommitmentRevealByte = 32
    )
  }

  def inMemoryStorage(config: KeyManagementConfig): ULayer[ManagedDIDService] =
    InMemoryDIDSecretStorage.layer >>> ZLayer.fromFunction(ManagedDIDService(_, config))
}
