package io.iohk.atala.agent.walletapi.storage

import zio.*
import zio.test.*
import zio.test.Assertion.*
import io.iohk.atala.agent.walletapi.crypto.KeyGeneratorWrapper
import io.iohk.atala.agent.walletapi.model.DIDPublicKeyTemplate
import io.iohk.atala.agent.walletapi.model.DIDUpdateLineage
import io.iohk.atala.agent.walletapi.model.ECCoordinates.ECCoordinate
import io.iohk.atala.agent.walletapi.model.ECKeyPair
import io.iohk.atala.agent.walletapi.model.ECPoint
import io.iohk.atala.agent.walletapi.model.ECPrivateKey
import io.iohk.atala.agent.walletapi.model.ECPublicKey
import io.iohk.atala.agent.walletapi.model.ManagedDIDState
import io.iohk.atala.agent.walletapi.model.ManagedDIDTemplate
import io.iohk.atala.agent.walletapi.sql.JdbcDIDNonSecretStorage
import io.iohk.atala.agent.walletapi.sql.JdbcDIDSecretStorage
import io.iohk.atala.agent.walletapi.util.OperationFactory
import io.iohk.atala.castor.core.model.did.EllipticCurve
import io.iohk.atala.castor.core.model.did.PrismDID
import io.iohk.atala.castor.core.model.did.PrismDIDOperation
import io.iohk.atala.castor.core.model.did.ScheduledDIDOperationStatus
import io.iohk.atala.castor.core.model.did.VerificationRelationship
import io.iohk.atala.test.container.{DBTestUtils, PostgresTestContainerSupport}

import java.time.Instant
import scala.collection.immutable.ArraySeq

object JdbcDIDSecretStorageSpec extends ZIOSpecDefault, PostgresTestContainerSupport {

  private val didExample = PrismDID.buildLongFormFromOperation(PrismDIDOperation.Create(Nil, Nil, Nil))

  private def updateLineage(
      operationId: Array[Byte] = Array.fill(32)(0),
      operationHash: Array[Byte] = Array.fill(32)(0),
      status: ScheduledDIDOperationStatus = ScheduledDIDOperationStatus.Confirmed
  ) = DIDUpdateLineage(
    operationId = ArraySeq.from(operationId),
    operationHash = ArraySeq.from(operationHash),
    previousOperationHash = ArraySeq.fill(32)(0),
    status = status,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH
  )

  private def generateKeyPair() = KeyGeneratorWrapper.generateECKeyPair(EllipticCurve.SECP256K1)

  private def generateCreateOperation(keyIds: Seq[String]) =
    OperationFactory.makeCreateOperation("master0", EllipticCurve.SECP256K1, generateKeyPair)(
      ManagedDIDTemplate(
        publicKeys = keyIds.map(DIDPublicKeyTemplate(_, VerificationRelationship.Authentication)),
        services = Nil
      )
    )

  private def initializeDIDStateAndKeys(keyIds: Seq[String] = Nil) = {
    for {
      nonSecretStorage <- ZIO.service[DIDNonSecretStorage]
      secretStorage <- ZIO.service[DIDSecretStorage]
      generated <- generateCreateOperation(keyIds)
      (createOperation, secrets) = generated
      did = createOperation.did
      keyPairs = secrets.keyPairs.toSeq
      _ <- nonSecretStorage.setManagedDIDState(did, ManagedDIDState.Created(createOperation))
      _ <- ZIO.foreach(keyPairs) { case (keyId, keyPair) =>
        secretStorage.insertKey(did, keyId, keyPair, createOperation.toAtalaOperationHash)
      }
    } yield (did, keyPairs)
  }

  override def spec = {
    val testSuite =
      suite("JdbcDIDSecretStorageSpec")(
        listKeySpec,
        getKeySpec,
        // upsertKeySpec,
        // removeKeySpec
      ) @@ TestAspect.sequential @@ TestAspect.before(DBTestUtils.runMigrationAgentDB) @@ TestAspect.timed @@ TestAspect
        .tag("dev")

    testSuite.provideSomeLayer(
      pgContainerLayer >+> transactorLayer >+> (JdbcDIDSecretStorage.layer ++ JdbcDIDNonSecretStorage.layer)
    )
  }

  private val listKeySpec = suite("listKeys")(
    test("initialize with empty list") {
      for {
        storage <- ZIO.service[DIDSecretStorage]
        keys <- storage.listKeys(didExample)
      } yield assert(keys)(isEmpty)
    },
    test("list all existing keys") {
      val operationHash = Array.fill[Byte](32)(0)
      for {
        storage <- ZIO.service[DIDSecretStorage]
        keyPairs <- ZIO.foreach(Seq("key-1", "key-2", "key-3"))(keyId => generateKeyPair().map(keyId -> _))
        _ <- ZIO.foreachDiscard(keyPairs) { case (keyId, keyPair) =>
          storage.insertKey(didExample, keyId, keyPair, operationHash)
        }
        readKeyPairs <- storage.listKeys(didExample)
      } yield assert(readKeyPairs)(hasSameElements(keyPairs))
    }
  )

  private val getKeySpec = suite("getKey")(
    test("return None if key doesn't exist") {
      for {
        storage <- ZIO.service[DIDSecretStorage]
        readKeyPair <- storage.getKey(didExample, "key-1")
      } yield assert(readKeyPair)(isNone)
    },
    test("return None if key exists but is not part of CreateOperation or confirmed UpdateOperation") {
      val operationHash = Array.fill[Byte](32)(0)
      for {
        storage <- ZIO.service[DIDSecretStorage]
        keyPair <- generateKeyPair()
        _ <- storage.insertKey(didExample, "key-1", keyPair, operationHash)
        readKeyPair <- storage.getKey(didExample, "key-1")
      } yield assert(readKeyPair)(isNone)
    },
    test("return key if exists and is part of CreateOperation") {
      for {
        storage <- ZIO.service[DIDSecretStorage]
        generated <- initializeDIDStateAndKeys(Seq("key-1"))
        (did, keyPairs) = generated
        readKeyPair <- storage.getKey(did, "key-1")
      } yield assert(readKeyPair)(isSome(equalTo(keyPairs.head._2)))
    },
    test("return key if exists and is part of confirmed UpdateOperation") {
      val operationHash = Array.fill[Byte](32)(42)
      for {
        storage <- ZIO.service[DIDSecretStorage]
        nonSecretStorage <- ZIO.service[DIDNonSecretStorage]
        generated <- initializeDIDStateAndKeys()
        (did, _) = generated
        _ <- nonSecretStorage.insertDIDUpdateLineage(
          did,
          updateLineage(operationHash = operationHash, status = ScheduledDIDOperationStatus.Confirmed)
        )
        keyPair <- generateKeyPair()
        _ <- storage.insertKey(did, "key-1", keyPair, operationHash)
        readKeyPair <- storage.getKey(did, "key-1")
      } yield assert(readKeyPair)(isSome(equalTo(keyPair)))
    },
    test("return None if key exists and is part of unconfirmed UpdateOperation") {
      val inputs = Seq(
        ("key-1", Array.fill[Byte](32)(1), ScheduledDIDOperationStatus.Pending),
        ("key-2", Array.fill[Byte](32)(2), ScheduledDIDOperationStatus.AwaitingConfirmation),
        ("key-3", Array.fill[Byte](32)(3), ScheduledDIDOperationStatus.Rejected),
      )
      for {
        storage <- ZIO.service[DIDSecretStorage]
        nonSecretStorage <- ZIO.service[DIDNonSecretStorage]
        generated <- initializeDIDStateAndKeys()
        (did, _) = generated
        keyIds <- ZIO.foreach(inputs) { case (keyId, hash, status) =>
          for {
            _ <- nonSecretStorage.insertDIDUpdateLineage(
              did,
              updateLineage(
                operationId = hash,
                operationHash = hash,
                status = status
              )
            )
            keyPair <- generateKeyPair()
            _ <- storage.insertKey(did, keyId, keyPair, hash)
          } yield keyId
        }
        readKeyPairs <- ZIO.foreach(keyIds)(keyId => storage.getKey(did, keyId))
      } yield assert(readKeyPairs)(forall(isNone))
    }
  )

  // private val upsertKeySpec = suite("upsertKey")(
  //   test("replace value for existing key") {
  //     val keyPair1 = generateKeyPair(publicKey = (1, 1))
  //     val keyPair2 = generateKeyPair(publicKey = (2, 2))
  //     for {
  //       storage <- ZIO.service[DIDSecretStorage]
  //       _ <- storage.insertKey(didExample, "key-1", keyPair1, Array.empty)
  //       _ <- storage.insertKey(didExample, "key-1", keyPair2, Array.empty)
  //       key <- storage.getKey(didExample, "key-1")
  //     } yield assert(key)(isSome(equalTo(keyPair2)))
  //   }
  // )

  // private val removeKeySpec = suite("removeKey")(
  //   test("remove existing key and return removed value") {
  //     val keyPair = generateKeyPair(publicKey = (1, 1))
  //     for {
  //       storage <- ZIO.service[DIDSecretStorage]
  //       _ <- storage.insertKey(didExample, "key-1", keyPair, Array.empty)
  //       _ <- storage.removeKey(didExample, "key-1")
  //       keys <- storage.listKeys(didExample)
  //     } yield assert(keys)(isEmpty)
  //   },
  //   test("remove non-existing key and return None for the removed value") {
  //     val keyPair = generateKeyPair(publicKey = (1, 1))
  //     for {
  //       storage <- ZIO.service[DIDSecretStorage]
  //       _ <- storage.insertKey(didExample, "key-1", keyPair, Array.empty)
  //       _ <- storage.removeKey(didExample, "key-2")
  //       keys <- storage.listKeys(didExample)
  //     } yield assert(keys)(hasSize(equalTo(1)))
  //   },
  //   test("remove some of existing keys and keep other keys") {
  //     val keyPairs = Map(
  //       "key-1" -> generateKeyPair(publicKey = (1, 1)),
  //       "key-2" -> generateKeyPair(publicKey = (2, 2)),
  //       "key-3" -> generateKeyPair(publicKey = (3, 3))
  //     )
  //     for {
  //       storage <- ZIO.service[DIDSecretStorage]
  //       _ <- ZIO.foreachDiscard(keyPairs) { case (keyId, keyPair) =>
  //         storage.insertKey(didExample, keyId, keyPair, Array.empty)
  //       }
  //       _ <- storage.removeKey(didExample, "key-1")
  //       keys <- storage.listKeys(didExample)
  //     } yield assert(keys.keys)(hasSameElements(Seq("key-2", "key-3")))
  //   }
  // )

}
