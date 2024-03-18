package io.iohk.atala.agent.server

import io.iohk.atala.agent.notification.WebhookPublisher
import io.iohk.atala.agent.server.config.AppConfig
import io.iohk.atala.agent.server.http.{ZHttp4sBlazeServer, ZHttpEndpoints}
import io.iohk.atala.agent.server.jobs.{
  ConnectBackgroundJobs,
  DIDStateSyncBackgroundJobs,
  IssueBackgroundJobs,
  PresentBackgroundJobs
}
import io.iohk.atala.agent.walletapi.model.{Entity, Wallet, WalletSeed}
import io.iohk.atala.agent.walletapi.service.{EntityService, ManagedDIDService, WalletManagementService}
import io.iohk.atala.agent.walletapi.storage.DIDNonSecretStorage
import io.iohk.atala.castor.controller.{DIDRegistrarServerEndpoints, DIDServerEndpoints}
import io.iohk.atala.castor.core.service.DIDService
import io.iohk.atala.connect.controller.ConnectionServerEndpoints
import io.iohk.atala.connect.core.service.ConnectionService
import io.iohk.atala.event.controller.EventServerEndpoints
import io.iohk.atala.event.notification.EventNotificationConfig
import io.iohk.atala.iam.authentication.apikey.ApiKeyAuthenticator
import io.iohk.atala.iam.entity.http.EntityServerEndpoints
import io.iohk.atala.iam.oidc.CredentialIssuerServerEndpoints
import io.iohk.atala.iam.wallet.http.WalletManagementServerEndpoints
import io.iohk.atala.issue.controller.IssueServerEndpoints
import io.iohk.atala.mercury.{DidOps, HttpClient}
import io.iohk.atala.pollux.core.service.{CredentialService, PresentationService}
import io.iohk.atala.pollux.credentialdefinition.CredentialDefinitionRegistryServerEndpoints
import io.iohk.atala.pollux.credentialschema.{SchemaRegistryServerEndpoints, VerificationPolicyServerEndpoints}
import io.iohk.atala.pollux.vc.jwt.DidResolver as JwtDidResolver
import io.iohk.atala.presentproof.controller.PresentProofServerEndpoints
import io.iohk.atala.resolvers.DIDResolver
import io.iohk.atala.shared.models.WalletAdministrationContext
import io.iohk.atala.shared.models.{HexString, WalletAccessContext, WalletId}
import io.iohk.atala.shared.utils.DurationOps.toMetricsSeconds
import io.iohk.atala.system.controller.SystemServerEndpoints
import zio.*
import zio.metrics.*

object PrismAgentApp {

  def run = for {
    _ <- AgentInitialization.run
    _ <- issueCredentialDidCommExchangesJob.debug.fork
    _ <- presentProofExchangeJob.debug.fork
    _ <- connectDidCommExchangesJob.debug.fork
    _ <- syncDIDPublicationStateFromDltJob.fork
    _ <- AgentHttpServer.run.fork
    fiber <- DidCommHttpServer.run.fork
    _ <- WebhookPublisher.layer.build.map(_.get[WebhookPublisher]).flatMap(_.run.debug.fork)
    _ <- fiber.join *> ZIO.log(s"Server End")
    _ <- ZIO.never
  } yield ()

  private val issueCredentialDidCommExchangesJob: RIO[
    AppConfig & DidOps & DIDResolver & JwtDidResolver & HttpClient & CredentialService & DIDNonSecretStorage &
      DIDService & ManagedDIDService & PresentationService & WalletManagementService,
    Unit
  ] =
    for {
      config <- ZIO.service[AppConfig]
      _ <- (IssueBackgroundJobs.issueCredentialDidCommExchanges @@ Metric
        .gauge("issuance_flow_did_com_exchange_job_ms_gauge")
        .trackDurationWith(_.toMetricsSeconds))
        .repeat(Schedule.spaced(config.pollux.issueBgJobRecurrenceDelay))
        .unit
    } yield ()

  private val presentProofExchangeJob: RIO[
    AppConfig & DidOps & DIDResolver & JwtDidResolver & HttpClient & PresentationService & CredentialService &
      DIDNonSecretStorage & DIDService & ManagedDIDService & WalletManagementService,
    Unit
  ] =
    for {
      config <- ZIO.service[AppConfig]
      _ <- (PresentBackgroundJobs.presentProofExchanges @@ Metric
        .gauge("present_proof_flow_did_com_exchange_job_ms_gauge")
        .trackDurationWith(_.toMetricsSeconds))
        .repeat(Schedule.spaced(config.pollux.presentationBgJobRecurrenceDelay))
        .unit
    } yield ()

  private val connectDidCommExchangesJob: RIO[
    AppConfig & DidOps & DIDResolver & HttpClient & ConnectionService & ManagedDIDService & DIDNonSecretStorage &
      WalletManagementService,
    Unit
  ] =
    for {
      config <- ZIO.service[AppConfig]
      _ <- (ConnectBackgroundJobs.didCommExchanges @@ Metric
        .gauge("connection_flow_did_com_exchange_job_ms_gauge")
        .trackDurationWith(_.toMetricsSeconds))
        .repeat(Schedule.spaced(config.connect.connectBgJobRecurrenceDelay))
        .unit
    } yield ()

  private val syncDIDPublicationStateFromDltJob: URIO[ManagedDIDService & WalletManagementService, Unit] =
    ZIO
      .serviceWithZIO[WalletManagementService](_.listWallets().map(_._1))
      .flatMap { wallets =>
        ZIO.foreach(wallets) { wallet =>
          DIDStateSyncBackgroundJobs.syncDIDPublicationStateFromDlt
            .provideSomeLayer(ZLayer.succeed(WalletAccessContext(wallet.id)))
        }
      }
      .catchAll(e => ZIO.logError(s"error while syncing DID publication state: $e"))
      .repeat(Schedule.spaced(10.seconds))
      .unit
      .provideSomeLayer(ZLayer.succeed(WalletAdministrationContext.Admin()))

}

object AgentHttpServer {
  val agentRESTServiceEndpoints = for {
    allCredentialDefinitionRegistryEndpoints <- CredentialDefinitionRegistryServerEndpoints.all
    allSchemaRegistryEndpoints <- SchemaRegistryServerEndpoints.all
    allVerificationPolicyEndpoints <- VerificationPolicyServerEndpoints.all
    allConnectionEndpoints <- ConnectionServerEndpoints.all
    allIssueEndpoints <- IssueServerEndpoints.all
    allDIDEndpoints <- DIDServerEndpoints.all
    allDIDRegistrarEndpoints <- DIDRegistrarServerEndpoints.all
    allPresentProofEndpoints <- PresentProofServerEndpoints.all
    allSystemEndpoints <- SystemServerEndpoints.all
    allEntityEndpoints <- EntityServerEndpoints.all
    allWalletManagementEndpoints <- WalletManagementServerEndpoints.all
    allEventEndpoints <- EventServerEndpoints.all
    allOIDCEndpoints <- CredentialIssuerServerEndpoints.all
  } yield allCredentialDefinitionRegistryEndpoints ++
    allSchemaRegistryEndpoints ++
    allVerificationPolicyEndpoints ++
    allConnectionEndpoints ++
    allDIDEndpoints ++
    allDIDRegistrarEndpoints ++
    allIssueEndpoints ++
    allPresentProofEndpoints ++
    allSystemEndpoints ++
    allEntityEndpoints ++
    allWalletManagementEndpoints ++
    allEventEndpoints ++
    allOIDCEndpoints
  def run =
    for {
      allEndpoints <- agentRESTServiceEndpoints
      allEndpointsWithDocumentation = ZHttpEndpoints.withDocumentations[Task](allEndpoints)
      server <- ZHttp4sBlazeServer.make
      appConfig <- ZIO.service[AppConfig]
      _ <- server.start(allEndpointsWithDocumentation, port = appConfig.agent.httpEndpoint.http.port).debug
    } yield ()
}

object AgentInitialization {

  private val defaultWalletId = WalletId.default
  private val defaultWallet = Wallet("default", defaultWalletId)
  private val defaultEntity = Entity.Default

  def run: RIO[AppConfig & WalletManagementService & EntityService & ApiKeyAuthenticator, Unit] =
    for {
      _ <- validateAppConfig
      _ <- initializeDefaultWallet
        .provideSomeLayer(ZLayer.succeed(WalletAdministrationContext.Admin()))
    } yield ()

  private val validateAppConfig =
    ZIO.serviceWithZIO[AppConfig](conf =>
      ZIO
        .fromEither(conf.validate)
        .mapError(msg => RuntimeException(s"Application configuration is invalid. $msg"))
    )

  private val initializeDefaultWallet =
    for {
      _ <- ZIO.logInfo("Initializing default wallet.")
      config <- ZIO.serviceWith[AppConfig](_.agent.defaultWallet)
      walletService <- ZIO.service[WalletManagementService]
      isDefaultWalletEnabled = config.enabled
      isDefaultWalletExist <- walletService
        .getWallet(defaultWalletId)
        .map(_.isDefined)
        .mapError(_.toThrowable)
      _ <- ZIO.logInfo(s"Default wallet not enabled.").when(!isDefaultWalletEnabled)
      _ <- ZIO.logInfo(s"Default wallet already exist.").when(isDefaultWalletExist)
      _ <- createDefaultWallet.when(isDefaultWalletEnabled && !isDefaultWalletExist)
    } yield ()

  private val createDefaultWallet =
    for {
      walletService <- ZIO.service[WalletManagementService]
      entityService <- ZIO.service[EntityService]
      apiKeyAuth <- ZIO.service[ApiKeyAuthenticator]
      config <- ZIO.serviceWith[AppConfig](_.agent.defaultWallet)
      seed <- config.seed.fold(ZIO.none) { seedHex =>
        ZIO
          .fromTry(HexString.fromString(seedHex))
          .map(bytes => WalletSeed.fromByteArray(bytes.toByteArray).left.map(Exception(_)))
          .absolve
          .asSome
      }
      _ <- ZIO.logInfo(s"Default wallet seed is not provided. New seed will be generated.").when(seed.isEmpty)
      _ <- walletService
        .createWallet(defaultWallet, seed)
        .mapError(_.toThrowable)
      _ <- entityService.create(defaultEntity).mapError(e => Exception(e.message))
      _ <- apiKeyAuth.add(defaultEntity.id, config.authApiKey).mapError(e => Exception(e.message))
      _ <- config.webhookUrl.fold(ZIO.unit) { url =>
        val customHeaders = config.webhookApiKey.fold(Map.empty)(apiKey => Map("Authorization" -> s"Bearer $apiKey"))
        walletService
          .createWalletNotification(EventNotificationConfig(defaultWalletId, url, customHeaders))
          .mapError(_.toThrowable)
          .provide(ZLayer.succeed(WalletAccessContext(defaultWalletId)))
      }
    } yield ()

}
