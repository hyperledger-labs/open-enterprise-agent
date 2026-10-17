package org.hyperledger.identus.iam.entity.http

import org.hyperledger.identus.api.http.*
import org.hyperledger.identus.api.http.model.PaginationInput
import org.hyperledger.identus.api.http.EndpointOutputs.*
import org.hyperledger.identus.iam.authentication.admin.AdminApiKeyCredentials
import org.hyperledger.identus.iam.authentication.admin.AdminApiKeySecurityLogic.adminApiKeyHeader
import org.hyperledger.identus.iam.authentication.oidc.JwtCredentials
import org.hyperledger.identus.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import org.hyperledger.identus.iam.entity.http.model.*
import sttp.apispec.Tag
import sttp.model.StatusCode
import sttp.tapir.{endpoint, extractFromRequest, path, query, statusCode, stringToPath, Endpoint, EndpointInput}
import sttp.tapir.json.zio.jsonBody

import java.util.UUID

object EntityEndpoints {

  private val tagName = "Identity and Access Management"
  private val tagDescription =
    s"""
       |The __${tagName}__ endpoints allow [agent administrators](https://identus.io/documentation/learn/glossary/#administrator)
       |to manage identity and access management for the agent's tenants.
       |It provides basic built-in IAM capabilities as an alternative to more feature rich external IAM solutions.
       |
       |Entities are resources that represent individual tenants and
       |wallets act as containers for Self-Sovereign Identity (SSI) resources within the agent.
       |The administrator can grant tenant access to specific wallets by associating the wallet ID with the Entity.
       |Additionally, the administrator can create API keys for entities and provide them to the tenants out-of-band.
       |These API keys can then be used for authorization to access specific wallets.
       |
       |For more detailed information related to the agent IAM and its usage, please refer to this [documentation](https://identus.io/cloud-agent/docs/docusaurus/multitenancy/tenant-onboarding/).
       |""".stripMargin

  val tag = Tag(tagName, Some(tagDescription))

  val createEntityEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, CreateEntityRequest),
    ErrorResponse,
    EntityResponse,
    Any
  ] =
    endpoint.post
      .securityIn(adminApiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in("iam" / "entities")
      .in(
        jsonBody[CreateEntityRequest]
          .description(
            "JSON object required for the entity creation"
          )
      )
      .out(
        statusCode(StatusCode.Created)
          .description(
            "The new entity is successfully created"
          )
      )
      .out(jsonBody[EntityResponse])
      .description("Entity record")
      .errorOut(basicFailureAndNotFoundAndForbidden)
      .name("createEntity")
      .summary("Create a new entity record")
      .description(
        "Create a new entity record. The entity record is a representation of the account in the system."
      )
      .tag(tagName)

  val updateEntityNameEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, UpdateEntityNameRequest),
    ErrorResponse,
    EntityResponse,
    Any
  ] =
    endpoint.put
      .securityIn(adminApiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "iam" / "entities" / path[UUID]("id") / "name" // .description(EntityResponse.annotations.id.description)
      )
      .in(
        jsonBody[UpdateEntityNameRequest]
          .description(
            "JSON object required for the entity name update"
          )
      )
      .out(
        statusCode(StatusCode.Ok)
          .description(
            "The entity record is successfully updated"
          )
      )
      .out(jsonBody[EntityResponse])
      .description("Entity record")
      .errorOut(basicFailureAndNotFoundAndForbidden)
      .name("updateEntityName")
      .summary("Update the entity record name by `id`")
      .description(
        "Update the entity record name by `id`"
      )
      .tag(tagName)

  val updateEntityWalletIdEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, UpdateEntityWalletIdRequest),
    ErrorResponse,
    EntityResponse,
    Any
  ] =
    endpoint.put
      .securityIn(adminApiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "iam" / "entities" / path[UUID]("id") / "walletId"
      )
      .in(
        jsonBody[UpdateEntityWalletIdRequest]
          .description(
            "JSON object required for the entity walletId update"
          )
      )
      .out(
        statusCode(StatusCode.Ok)
          .description(
            "The entity record is successfully updated"
          )
      )
      .out(jsonBody[EntityResponse])
      .description("Entity record")
      .errorOut(basicFailureAndNotFoundAndForbidden)
      .name("updateEntityWalletId")
      .summary("Update the entity record `walletId` by `id`")
      .description(
        "Update the entity record `walletId` field by `id`"
      )
      .tag(tagName)

  val getEntityByIdEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID),
    ErrorResponse,
    EntityResponse,
    Any
  ] =
    endpoint.get
      .securityIn(adminApiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "iam" / "entities" / path[UUID]("id").description(
          "Identifier of the entity"
        )
      )
      .out(jsonBody[EntityResponse].description("Entity found by `id`"))
      .errorOut(basicFailureAndNotFoundAndForbidden)
      .name("getEntityById")
      .summary("Get the entity by the `id`")
      .description(
        "Get the entity by the unique identifier"
      )
      .tag(tagName)

  private val paginationInput: EndpointInput[PaginationInput] = EndpointInput.derived[PaginationInput]
  val getEntitiesEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, PaginationInput),
    ErrorResponse,
    EntityResponsePage,
    Any
  ] =
    endpoint.get
      .securityIn(adminApiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in("iam" / "entities")
      .in(paginationInput)
      .out(jsonBody[EntityResponsePage].description("Collection of Entity records"))
      .errorOut(basicFailuresAndForbidden)
      .name("getAllEntities")
      .summary("Get all entities")
      .description(
        "Get all entities paginated by `offset` and `limit` parameters."
      )
      .tag(tagName)

  val deleteEntityByIdEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID),
    ErrorResponse,
    Unit,
    Any
  ] =
    endpoint.delete
      .securityIn(adminApiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "iam" / "entities" / path[UUID]("id").description(
          "Identifier of the entity"
        )
      )
      .out(
        statusCode(StatusCode.Ok).description("Entity deleted successfully")
      )
      .errorOut(basicFailureAndNotFoundAndForbidden)
      .name("deleteEntityById")
      .summary("Delete the entity by `id`")
      .description(
        "Delete the entity by the unique identifier"
      )
      .tag(tagName)

  val addEntityApiKeyAuthenticationEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, ApiKeyAuthenticationRequest),
    ErrorResponse,
    Unit,
    Any
  ] = endpoint.post
    .securityIn(adminApiKeyHeader)
    .securityIn(jwtAuthHeader)
    .in(extractFromRequest[RequestContext](RequestContext.apply))
    .in("iam" / "apikey-authentication")
    .in(
      jsonBody[ApiKeyAuthenticationRequest]
        .description(
          "JSON object required for registering the entity and `apikey`"
        )
    )
    .out(
      statusCode(StatusCode.Created)
        .description(
          "The new `apikey` is successfully registered for the entity"
        )
    )
    .errorOut(basicFailureAndNotFoundAndForbidden)
    .name("addEntityApiKeyAuthentication")
    .summary("Register the `apikey` for the entity")
    .description(
      "Register the `apikey` for the entity."
    )
    .tag(tagName)

  val deleteEntityApiKeyAuthenticationEndpoint: Endpoint[
    (AdminApiKeyCredentials, JwtCredentials),
    (RequestContext, ApiKeyAuthenticationRequest),
    ErrorResponse,
    Unit,
    Any
  ] = endpoint.delete
    .securityIn(adminApiKeyHeader)
    .securityIn(jwtAuthHeader)
    .in(extractFromRequest[RequestContext](RequestContext.apply))
    .in("iam" / "apikey-authentication")
    .in(
      jsonBody[ApiKeyAuthenticationRequest]
        .description(
          "JSON object required for unregistering the entity and `apikey`"
        )
    )
    .out(
      statusCode(StatusCode.Ok)
        .description(
          "The `apikey` is successfully unregistered for the entity"
        )
    )
    .errorOut(basicFailureAndNotFoundAndForbidden)
    .name("deleteEntityApiKeyAuthentication")
    .summary("Unregister the `apikey` for the entity")
    .description(
      "Unregister the `apikey` for the entity."
    )
    .tag(tagName)
}
