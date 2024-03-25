package io.iohk.atala.agent.server.http

import io.iohk.atala.castor.controller.{DIDEndpoints, DIDRegistrarEndpoints}
import io.iohk.atala.connect.controller.ConnectionEndpoints
import io.iohk.atala.iam.wallet.http.WalletManagementEndpoints
import io.iohk.atala.pollux.credentialdefinition.CredentialDefinitionRegistryEndpoints
import io.iohk.atala.pollux.credentialschema.{SchemaRegistryEndpoints, VerificationPolicyEndpoints}
import io.iohk.atala.system.controller.SystemEndpoints
import sttp.apispec.openapi.*
import sttp.apispec.{SecurityScheme, Tag}
import sttp.model.headers.AuthenticationScheme

import scala.collection.immutable.ListMap
import io.iohk.atala.issue.controller.IssueEndpoints

object DocModels {

  private val apiKeySecuritySchema = SecurityScheme(
    `type` = "apiKey",
    description = Some("API Key Authentication. The header `apikey` must be set with the API key."),
    name = Some("apikey"),
    in = Some("header"),
    scheme = None,
    bearerFormat = None,
    flows = None,
    openIdConnectUrl = None
  )

  private val adminApiKeySecuritySchema = SecurityScheme(
    `type` = "apiKey",
    description =
      Some("Admin API Key Authentication. The header `x-admin-api-key` must be set with the Admin API key."),
    name = Some("x-admin-api-key"),
    in = Some("header"),
    scheme = None,
    bearerFormat = None,
    flows = None,
    openIdConnectUrl = None
  )

  private val jwtSecurityScheme = SecurityScheme(
    `type` = "http",
    description =
      Some("JWT Authentication. The header `Authorization` must be set with the JWT token using `Bearer` scheme"),
    name = Some("Authorization"),
    in = Some("header"),
    scheme = Some(AuthenticationScheme.Bearer.name),
    bearerFormat = None,
    flows = None,
    openIdConnectUrl = None
  )

  val customiseDocsModel: OpenAPI => OpenAPI = { oapi =>
    oapi
      .openapi("3.0.3")
      .info(
        Info(
          title = "Open Enterprise Agent API Reference",
          version = "1.0", // Will be replaced dynamically by 'Tapir2StaticOAS'
          summary = None,
          description = Some("""
              |The Open Enterprise Agent API facilitates the integration and management of self-sovereign identity capabilities within applications.
              |It supports DID (Decentralized Identifiers) management, verifiable credential exchange, and secure messaging based on DIDComm standards.
              |The API is designed to be interoperable with various blockchain and DLT (Distributed Ledger Technology) platforms, ensuring wide compatibility and flexibility.
              |Key features include connection management, credential issuance and verification, and secure, privacy-preserving communication between entities.
              |Additional information and the full list of capabilities can be found in the [Open Enterprise Agent documentation](https://docs.atalaprism.io/docs/category/prism-cloud-agent)
              |""".stripMargin),
          termsOfService = None,
          contact = None,
          license = Some(
            License(
              name = "Apache 2.0",
              url = Some("https://www.apache.org/licenses/LICENSE-2.0"),
              extensions = ListMap.empty
            )
          ),
          extensions = ListMap.empty
        )
      )
      .servers(
        List(
          Server(url = "http://localhost:8085", description = Some("Local Prism Agent")),
          Server(url = "http://localhost/prism-agent", description = Some("Local Prism Agent with APISIX proxy")),
          Server(
            url = "https://k8s-dev.atalaprism.io/prism-agent",
            description = Some("Prism Agent on the Staging Environment")
          ),
        )
      )
      .components(
        oapi.components
          .getOrElse(sttp.apispec.openapi.Components.Empty)
          .copy(securitySchemes =
            ListMap(
              "apiKeyAuth" -> Right(apiKeySecuritySchema),
              "adminApiKeyAuth" -> Right(adminApiKeySecuritySchema),
              "jwtAuth" -> Right(jwtSecurityScheme)
            )
          )
      )
      .addSecurity(
        ListMap(
          "apiKeyAuth" -> Vector.empty[String],
          "adminApiKeyAuth" -> Vector.empty[String],
          "jwtAuth" -> Vector.empty[String]
        )
      )
      .tags(
        List(
          ConnectionEndpoints.tag,
          IssueEndpoints.tag,
          VerificationPolicyEndpoints.tag,
          SchemaRegistryEndpoints.tag,
          CredentialDefinitionRegistryEndpoints.tag,
          DIDEndpoints.tag,
          DIDRegistrarEndpoints.tag,
          WalletManagementEndpoints.tag,
          SystemEndpoints.tag
        )
      )

  }

}
