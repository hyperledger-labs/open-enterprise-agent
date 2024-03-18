package io.iohk.atala.api.util

import io.iohk.atala.agent.server.AgentHttpServer
import io.iohk.atala.agent.server.http.DocModels
import io.iohk.atala.castor.controller.{DIDController, DIDRegistrarController}
import io.iohk.atala.connect.controller.ConnectionController
import io.iohk.atala.event.controller.EventController
import io.iohk.atala.iam.authentication.DefaultAuthenticator
import io.iohk.atala.iam.entity.http.controller.EntityController
import io.iohk.atala.iam.oidc.controller.CredentialIssuerController
import io.iohk.atala.iam.wallet.http.controller.WalletManagementController
import io.iohk.atala.issue.controller.IssueController
import io.iohk.atala.pollux.credentialdefinition.controller.CredentialDefinitionController
import io.iohk.atala.pollux.credentialschema.controller.{CredentialSchemaController, VerificationPolicyController}
import io.iohk.atala.presentproof.controller.PresentProofController
import io.iohk.atala.system.controller.SystemController
import org.scalatestplus.mockito.MockitoSugar.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.util.Using

object Tapir2StaticOAS extends ZIOAppDefault {

  @main override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val effect = for {
      args <- getArgs
      _ <- ZIO.when(args.length != 2)(ZIO.fail("Usage: Tapir2StaticOAS <output file> <server url>"))
      allEndpoints <- AgentHttpServer.agentRESTServiceEndpoints
    } yield {
      import sttp.apispec.openapi.circe.yaml.*
      val model = DocModels.customiseDocsModel(OpenAPIDocsInterpreter().toOpenAPI(allEndpoints.map(_.endpoint), "", ""))
      val yaml = model.info(model.info.copy(version = args(1))).toYaml
      val path = Path.of(args.head)
      Using(Files.newBufferedWriter(path, StandardCharsets.UTF_8)) { writer => writer.write(yaml) }
    }
    effect.provideSomeLayer(
      ZLayer.succeed(mock[ConnectionController]) ++
        ZLayer.succeed(mock[CredentialDefinitionController]) ++
        ZLayer.succeed(mock[CredentialSchemaController]) ++
        ZLayer.succeed(mock[VerificationPolicyController]) ++
        ZLayer.succeed(mock[DIDRegistrarController]) ++
        ZLayer.succeed(mock[PresentProofController]) ++
        ZLayer.succeed(mock[IssueController]) ++
        ZLayer.succeed(mock[DIDController]) ++
        ZLayer.succeed(mock[SystemController]) ++
        ZLayer.succeed(mock[EntityController]) ++
        ZLayer.succeed(mock[WalletManagementController]) ++
        ZLayer.succeed(mock[DefaultAuthenticator]) ++
        ZLayer.succeed(mock[EventController]) ++
        ZLayer.succeed(mock[CredentialIssuerController])
    )
  }

}
