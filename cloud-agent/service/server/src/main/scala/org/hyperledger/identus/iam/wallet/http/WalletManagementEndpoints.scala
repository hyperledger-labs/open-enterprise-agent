package org.hyperledger.identus.iam.wallet.http

import org.hyperledger.identus.api.http.{EndpointOutputs, ErrorResponse, RequestContext}
import org.hyperledger.identus.api.http.model.PaginationInput
import org.hyperledger.identus.iam.authentication.admin.AdminApiKeyCredentials
import org.hyperledger.identus.iam.authentication.admin.AdminApiKeySecurityLogic.adminApiKeyHeader
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyCredentials
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyEndpointSecurityLogic.apiKeyHeader
import org.hyperledger.identus.iam.authentication.oidc.JwtCredentials
import org.hyperledger.identus.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import org.hyperledger.identus.iam.wallet.http.model.{
  CreateWalletRequest,
  CreateWalletUmaPermissionRequest,
  WalletDetail,
  WalletDetailPage
}
import sttp.apispec.Tag
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody

import java.util.UUID

object WalletManagementEndpoints {

  private val tagName = "Wallet Management"
  private val tagDescription =
    s"""
       |The __${tagName}__ endpoints enable both users and administrators to manage [wallets](https://identus.io/documentation/learn/advanced-explainers/cloud-agent/multi-tenancy/#wallet).
       |
       |In a multitenant agent, a wallet is a container for various resources (e.g. Connections, DIDs) and it isolates the access based on the authorization settings.
       |[Administrators](https://identus.io/documentation/learn/glossary/#administrator) can utilize the endpoints to manage and onboard [tenants](https://identus.io/documentation/learn/glossary/#tenant).
       |See [this example](https://identus.io/cloud-agent/docs/docusaurus/multitenancy/tenant-onboarding/-ext-iam) for instructions on how to utilize the endpoints as an administrator.
       |Tenants can also manage and onboard their own wallets using these endpoints depending on the configuration.
       |See [this document](https://identus.io/cloud-agent/docs/docusaurus/multitenancy/tenant-onboarding/-self-service) for a detailed example of self-service tenant onboarding.
       |
       |Wallet permissions are controlled by [UMA](https://identus.io/documentation/learn/glossary/#uma) configuration which the agent
       |exposes endpoints to easily configure wallet access using `uma-permissions` resource.
       |The permissions can also be configured out-of-band directly on the external IAM provider that supports the UMA standard.
       |""".stripMargin

  val tag = Tag(tagName, Some(tagDescription))

  private val baseEndpoint = endpoint
    .tag(tagName)
    .securityIn(adminApiKeyHeader)
    .securityIn(apiKeyHeader)
    .securityIn(jwtAuthHeader)
    .in("wallets")
    .in(extractFromRequest[RequestContext](RequestContext.apply))

  private val paginationInput: EndpointInput[PaginationInput] = EndpointInput.derived[PaginationInput]

  val listWallet: Endpoint[
    (AdminApiKeyCredentials, ApiKeyCredentials, JwtCredentials),
    (RequestContext, PaginationInput),
    ErrorResponse,
    WalletDetailPage,
    Any
  ] =
    baseEndpoint.get
      .in(paginationInput)
      .errorOut(EndpointOutputs.basicFailuresAndForbidden)
      .out(statusCode(StatusCode.Ok).description("Successfully list all permitted wallets"))
      .out(jsonBody[WalletDetailPage])
      .summary("List all permitted wallets")
      .description(
        "List all permitted wallets. If the role is admin, returns all wallets. If the role is tenant, returns only permitted wallets."
      )

  val getWallet: Endpoint[
    (AdminApiKeyCredentials, ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID),
    ErrorResponse,
    WalletDetail,
    Any
  ] =
    baseEndpoint.get
      .in(path[UUID]("walletId"))
      .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
      .out(statusCode(StatusCode.Ok).description("Successfully get the wallet"))
      .out(jsonBody[WalletDetail])
      .summary("Get the wallet by ID")
      .description("Get the wallet by ID. If the role is tenant, only searches among permitted wallets.")

  val createWallet: Endpoint[
    (AdminApiKeyCredentials, ApiKeyCredentials, JwtCredentials),
    (RequestContext, CreateWalletRequest),
    ErrorResponse,
    WalletDetail,
    Any
  ] = baseEndpoint.post
    .in(jsonBody[CreateWalletRequest])
    .out(
      statusCode(StatusCode.Created).description("Successfully create a new wallet")
    )
    .out(jsonBody[WalletDetail])
    .errorOut(EndpointOutputs.basicFailuresAndForbidden)
    .name("createWallet")
    .summary("Create a new wallet")
    .description(
      """Create a new wallet with the option to provide the seed.
        |The seed will be used for all PRISM DID keypair derivation within the wallet.
        |
        |If the role is admin, a wallet can be created at any time.
        |If the role is tenant, a wallet can only be created if there is no existing wallet permission for that tenant.
        |The permission for the tenant will be automatically granted after the wallet is created with tenant role.
        """.stripMargin
    )

  val createWalletUmaPermission: Endpoint[
    (AdminApiKeyCredentials, ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, CreateWalletUmaPermissionRequest),
    ErrorResponse,
    Unit,
    Any
  ] =
    baseEndpoint.post
      .in(path[UUID]("walletId") / "uma-permissions")
      .in(jsonBody[CreateWalletUmaPermissionRequest])
      .out(
        statusCode(StatusCode.Ok)
          .description("UMA resource permission is created on an authorization server")
      )
      .errorOut(EndpointOutputs.basicFailuresAndForbidden)
      .name("createWalletUmaPermission")
      .summary("Create a UMA resource permission on an authorization server for the wallet.")
      .description(
        """Create a UMA resource permission on an authorization server for the wallet.
          |This grants the wallet permission to the specified `subject`, where the `subject` denotes the identity of the tenant on an authorization server.
          """.stripMargin
      )

  val deleteWalletUmaPermission: Endpoint[
    (AdminApiKeyCredentials, ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, UUID),
    ErrorResponse,
    Unit,
    Any
  ] =
    baseEndpoint.delete
      .in(path[UUID]("walletId") / "uma-permissions")
      .in(query[UUID]("subject"))
      .out(
        statusCode(StatusCode.Ok)
          .description("UMA resource permission is removed from an authorization server.")
      )
      .errorOut(EndpointOutputs.basicFailuresAndForbidden)
      .name("deleteWalletUmaPermission")
      .summary("Delete a UMA resource permission on an authorization server for the wallet.")
      .description(
        """Remove a UMA resource permission on an authorization server for the wallet.
          |This removes the wallet permission for the specified `subject`, where the `subject` denotes the identity of the tenant on an authorization server.
          """.stripMargin
      )

}
