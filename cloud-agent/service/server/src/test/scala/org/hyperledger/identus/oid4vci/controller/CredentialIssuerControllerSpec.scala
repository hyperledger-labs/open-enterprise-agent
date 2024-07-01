package org.hyperledger.identus.oid4vci.controller

import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.util.transactor.Transactor
import org.hyperledger.identus.agent.server.config.AppConfig
import org.hyperledger.identus.agent.server.AppModule.apolloLayer
import org.hyperledger.identus.agent.walletapi.model.{BaseEntity, DIDPublicKeyTemplate, Entity, ManagedDIDTemplate}
import org.hyperledger.identus.agent.walletapi.service.{
  ManagedDIDService,
  ManagedDIDServiceImpl,
  WalletManagementService,
  WalletManagementServiceImpl
}
import org.hyperledger.identus.agent.walletapi.sql.{
  JdbcDIDNonSecretStorage,
  JdbcDIDSecretStorage,
  JdbcWalletNonSecretStorage,
  JdbcWalletSecretStorage
}
import org.hyperledger.identus.agent.walletapi.storage.{DIDSecretStorage, WalletSecretStorage}
import org.hyperledger.identus.api.http.RequestContext
import org.hyperledger.identus.castor.controller.{DIDRegistrarController, DIDRegistrarControllerImpl}
import org.hyperledger.identus.castor.core.model.did.{
  DIDData,
  DIDMetadata,
  EllipticCurve,
  PrismDID,
  ScheduleDIDOperationOutcome,
  ScheduledDIDOperationDetail,
  ScheduledDIDOperationStatus,
  SignedPrismDIDOperation,
  VerificationRelationship
}
import org.hyperledger.identus.castor.core.model.error.{DIDOperationError, DIDResolutionError}
import org.hyperledger.identus.castor.core.service.DIDService
import org.hyperledger.identus.castor.core.util.DIDOperationValidator
import org.hyperledger.identus.iam.authentication.oidc.{
  KeycloakAuthenticator,
  KeycloakAuthenticatorImpl,
  KeycloakClient,
  KeycloakClientImpl,
  KeycloakConfig,
  KeycloakEntity
}
import org.hyperledger.identus.iam.authentication.oidc.KeycloakAuthenticatorSpec.{
  grantClientRole,
  initializeClient,
  keycloakAdminClientLayer,
  keycloakConfigLayer,
  keycloakContainerLayer
}
import org.hyperledger.identus.iam.authorization.core.PermissionManagement
import org.hyperledger.identus.iam.authorization.keycloak.admin.{
  KeycloakAdmin,
  KeycloakConfigUtils,
  KeycloakPermissionManagementService
}
import org.hyperledger.identus.iam.authorization.DefaultPermissionManagementService
import org.hyperledger.identus.iam.wallet.http.controller.{WalletManagementController, WalletManagementControllerImpl}
import org.hyperledger.identus.iam.wallet.http.model.CreateWalletRequest
import org.hyperledger.identus.oid4vci.domain.Openid4VCIProofJwtOps
import org.hyperledger.identus.pollux.core.service.CredentialServiceSpecHelper
import org.hyperledger.identus.shared.db.ContextAwareTask
import org.hyperledger.identus.shared.models.{WalletAccessContext, WalletAdministrationContext, WalletId}
import org.hyperledger.identus.sharedtest.containers.{
  KeycloakAdminClient,
  KeycloakContainerCustom,
  KeycloakTestContainerSupport,
  PostgresTestContainerSupport
}
import org.hyperledger.identus.test.container.DBTestUtils
import org.keycloak.authorization.client.AuthzClient
import zio.{mock, *}
import zio.http.Client
import zio.mock.*
import zio.mock.{Mock, MockSpecDefault}
import zio.test.{assertTrue, TestAspect, ZIOSpecDefault}
import zio.test.TestAspect.sequential

import scala.collection.immutable.ArraySeq

object CredentialIssuerControllerSpec
    extends MockSpecDefault
    with CredentialServiceSpecHelper
    with Openid4VCIProofJwtOps
    with KeycloakConfigUtils
    with KeycloakTestContainerSupport
    with PostgresTestContainerSupport {

  private object PSE extends PermissionManagement.Service[Entity] {

    override def grantWalletToUser(
        walletId: WalletId,
        entity: Entity
    ): ZIO[WalletAdministrationContext, PermissionManagement.Error, Unit] = ???

    override def revokeWalletFromUser(
        walletId: WalletId,
        entity: Entity
    ): ZIO[WalletAdministrationContext, PermissionManagement.Error, Unit] = ???

    override def listWalletPermissions(
        entity: Entity
    ): ZIO[WalletAdministrationContext, PermissionManagement.Error, Seq[WalletId]] = ???
  }

  private trait TestDIDService extends DIDService {
    def getPublishedOperations: UIO[Seq[SignedPrismDIDOperation]]

    def setOperationStatus(status: ScheduledDIDOperationStatus): UIO[Unit]

    def setResolutionResult(result: Option[(DIDMetadata, DIDData)]): UIO[Unit]
  }

  private def testDIDServiceLayer = ZLayer.fromZIO {
    for {
      operationStore <- Ref.make(Seq.empty[SignedPrismDIDOperation])
      statusStore <- Ref.make[ScheduledDIDOperationStatus](ScheduledDIDOperationStatus.Pending)
      resolutionStore <- Ref.make[Option[(DIDMetadata, DIDData)]](None)
    } yield new TestDIDService {
      override def scheduleOperation(
          signOperation: SignedPrismDIDOperation
      ): IO[DIDOperationError, ScheduleDIDOperationOutcome] = {
        operationStore
          .update(_.appended(signOperation))
          .as(ScheduleDIDOperationOutcome(signOperation.operation.did, signOperation.operation, ArraySeq.empty))
      }

      override def resolveDID(did: PrismDID): IO[DIDResolutionError, Option[(DIDMetadata, DIDData)]] =
        resolutionStore.get

      override def getScheduledDIDOperationDetail(
          operationId: Array[Byte]
      ): IO[DIDOperationError, Option[ScheduledDIDOperationDetail]] =
        statusStore.get.map(ScheduledDIDOperationDetail).asSome

      override def getPublishedOperations: UIO[Seq[SignedPrismDIDOperation]] = operationStore.get

      override def setOperationStatus(status: ScheduledDIDOperationStatus): UIO[Unit] = statusStore.set(status)

      override def setResolutionResult(result: Option[(DIDMetadata, DIDData)]): UIO[Unit] = resolutionStore.set(result)
    }
  }

  private val keycloakInfrastructureLayers = ZLayer.make[KeycloakConfig & KeycloakAdminClient](
    keycloakContainerLayer,
    keycloakAdminClientLayer,
    keycloakConfigLayer(true)
  )

  private val applicationLayers =
    ZLayer.make[
      WalletManagementController & KeycloakClient & AuthzClient & DIDRegistrarController & KeycloakAuthenticator &
        KeycloakConfig & KeycloakAdminClient & PostgreSQLContainer
    ](
      WalletManagementServiceImpl.layer,
      WalletManagementControllerImpl.layer,
      KeycloakPermissionManagementService.layer,
      KeycloakAuthenticatorImpl.layer,
      DefaultPermissionManagementService.layer,
      DIDRegistrarControllerImpl.layer,
      ZLayer.succeed(PSE),
      JdbcWalletSecretStorage.layer,
      JdbcWalletNonSecretStorage.layer,
      systemTransactorLayer,
      contextAwareTransactorLayer,
      KeycloakClientImpl.authzClientLayer,
      KeycloakClientImpl.layer,
      Client.default,
      ManagedDIDServiceImpl.layer,
      DIDOperationValidator.layer(),
      JdbcDIDSecretStorage.layer,
      JdbcDIDNonSecretStorage.layer,
      testDIDServiceLayer,
      apolloLayer,
      pgContainerLayer,
      keycloakInfrastructureLayers
    )

  val didTemplate = ManagedDIDTemplate(
    publicKeys = Seq(DIDPublicKeyTemplate("key-0", VerificationRelationship.AssertionMethod, EllipticCurve.ED25519)),
    services = Nil,
    contexts = Nil
  )

  override def spec = (suite("CredentialIssuerController")(
    // authorizationCodeFlowSpec1a,
    autorizationCodeFlowSpec1b
    // preAutorizedCodeFlowSpec,
  ) @@ bootstrapKeycloakRealmAspect @@ TestAspect.beforeAll(DBTestUtils.runMigrationAgentDB))
    .provideLayerShared(applicationLayers)

  val authorizationCodeFlowSpec1a = suite("Authorization Code Flow 1a")(
    test(
      "1a: The Wallet-initiated flow begins as the End-User requests a Credential via the Wallet from the Credential Issuer."
    ) {
      assertTrue(true)
    },
    test("2: The Wallet uses the Credential Issuer's URL to fetch the Credential Issuer metadata") {
      assertTrue(true)
    },
    test("3: The Wallet sends an Authorization Request to the Authorization Endpoint") {
      assertTrue(true)
    },
    test(
      "4: The Authorization Endpoint returns the Authorization Response with the Authorization Code upon successfully processing the Authorization Request"
    ) {
      assertTrue(true)
    },
    test("5: The Wallet sends a Token Request to the Token Endpoint with the Authorization Code") {
      assertTrue(true)
    },
    test(
      "6: The Wallet sends a Credential Request to the Credential Issuer's Credential Endpoint with the Access Token"
    ) {
      assertTrue(true)
    },
  ) @@ sequential

  val rc = RequestContext(null)

  val autorizationCodeFlowSpec1b = suite("Authorization Code Flow 1b")(
    test("Bootstrap the context") {
      for {
        client <- ZIO.service[KeycloakClient]
        issuer <- createUser("issuer", "1234")
        _ <- createClientRole("tenant")
        _ <- createClientRole("admin")
        _ <- grantClientRole("issuer", "tenant")
        token <- client.getAccessToken("issuer", "1234").map(_.access_token)
        issuerEntity <- ZIO.serviceWith[KeycloakAuthenticator](_.authenticate(token)).flatten

        wmc <- ZIO.service[WalletManagementController]
        wallet <- wmc
          .createWallet(CreateWalletRequest(seed = None, name = "issuerWallet", id = None), issuerEntity)(rc)
          .provide(ZLayer.succeed(WalletAdministrationContext.Admin()))

//        mds <- ZIO.service[ManagedDIDService]
//        issuerDid <- mds
//          .createAndStoreDID(didTemplate)
//          .provide(ZLayer.succeed(WalletAccessContext(walletId = WalletId.fromUUID(wallet.id))))
//          .map(_.asCanonical)
//        _ <- ZIO.consoleWith(_.printLine(s"Issuer DID: ${issuerDid.toString}"))
      } yield assertTrue(true)
    },
    test("Issuer creates DID, VC schema and configures OIDC issuer endpoint") {
      assertTrue(true)
    },
    test("1: The Issuer-initiated flow begins as the Credential Issuer generates a Credential Offer") {
      assertTrue(true)
    },
    test("2: The Wallet uses the Credential Issuer's URL to fetch the Credential Issuer metadata") {
      assertTrue(true)
    },
    test("3: The Wallet sends an Authorization Request to the Authorization Endpoint") {
      assertTrue(true)
    },
    test(
      "4: The Authorization Endpoint returns the Authorization Response with the Authorization Code upon successfully processing the Authorization Request"
    ) {
      assertTrue(true)
    },
    test("5: The Wallet sends a Token Request to the Token Endpoint with the Authorization Code") {
      assertTrue(true)
    },
    test(
      "6: The Wallet sends a Credential Request to the Credential Issuer's Credential Endpoint with the Access Token"
    ) {
      assertTrue(true)
    },
  ) @@ sequential

//  val preAutorizedCodeFlowSpec = suite("Pre-Authorized Code Flow")(
//    test(
//      "1: The Credential Issuer successfully obtains consent and End-User data required for the issuance of a requested Credential from the End-User using an Issuer-specific business process"
//    ) {
//      assertTrue(true)
//    },
//    test(
//      "2: Credential Issuer generates a Credential Offer for certain Credential(s) and communicates it to the Wallet"
//    ) {
//      assertTrue(true)
//    },
//    test("3: The Wallet uses the Credential Issuer's URL to fetch its metadata") {
//      assertTrue(true)
//    },
//    test(
//      "4: The Wallet sends the Pre-Authorized Code obtained in Step (2) in the Token Request to the Token Endpoint"
//    ) {
//      assertTrue(true)
//    },
//    test("5: The Wallet sends a Token Request to the Token Endpoint with the Authorization Code") {
//      assertTrue(true)
//    },
//    test(
//      "6: The Wallet sends a Credential Request to the Credential Issuer's Credential Endpoint with the Access Token"
//    ) {
//      assertTrue(true)
//    },
//  ) @@ sequential
}
