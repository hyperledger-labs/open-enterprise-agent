package io.iohk.atala.pollux.credentialschema

import io.iohk.atala.api.http.*
import io.iohk.atala.api.http.EndpointOutputs.*
import io.iohk.atala.api.http.codec.OrderCodec.*
import io.iohk.atala.api.http.model.{Order, PaginationInput}
import io.iohk.atala.iam.authentication.apikey.ApiKeyCredentials
import io.iohk.atala.iam.authentication.apikey.ApiKeyEndpointSecurityLogic.apiKeyHeader
import io.iohk.atala.iam.authentication.oidc.JwtCredentials
import io.iohk.atala.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import io.iohk.atala.pollux.credentialschema.http.{
  CredentialSchemaInput,
  CredentialSchemaResponse,
  CredentialSchemaResponsePage,
  FilterInput
}
import sttp.apispec.{ExternalDocumentation, Tag}
import sttp.model.StatusCode
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.{
  Endpoint,
  EndpointInput,
  PublicEndpoint,
  endpoint,
  extractFromRequest,
  path,
  query,
  statusCode,
  stringToPath
}

import java.util.UUID

object SchemaRegistryEndpoints {

  private val tagName = "Schema Registry"
  private val tagDescription =
    s"""
      |The __${tagName}__ is a REST API that allows to publish and lookup credential schemas in [W3C](https://w3c.github.io/vc-json-schema/) and [AnonCreds](https://hyperledger.github.io/anoncreds-spec/#term:schema) formats.
      |
      |The Credential Schema is a JSON document that describes the structure of the credential and consists of the following parts: metadata, schema and signature.
      |The metadata contains the following fields:
        |* `id` - locally unique identifier of the schema
        |* `version` - version of the schema
        |* `author` - the DID of the issuer of the schema
        |* `guid` - globally unique identifier of the schema (generated by the Schema Registry based on `author`, `id` and `version`)
        |* `name` - name of the schema
        |* `tags` - list of tags that describe the schema
        |* `createdAt` - timestamp of the schema creation
        |* `description` - description of the schema
      |
      |The schema contains the JSON Schema that describes the structure of the credential in the `schema` field
      |The signature contains the signature of the schema by the issuer in the `proof` field. The signature is generated by the issuer's DID key using Ed25519Signature2020 method.
      |
      |The __Credential Schema__ object is immutable, so update operation creates a new version of the schema.
      |The __Credential Schema__ is referenced via `schemaId` field in the issuance and verification flows.
      |
      |Endpoints are secured by __apiKeyAuth__ or __jwtAuth__ authentication.
      |""".stripMargin

  private val tagExternalDocumentation = ExternalDocumentation(
    url = "https://docs.atalaprism.io/tutorials/schemas/credential-schema",
    description = Some("Credential Schema documentation")
  )

  val tag = Tag(name = tagName, description = Option(tagDescription), externalDocs = Option(tagExternalDocumentation))

  val createSchemaEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, CredentialSchemaInput),
    ErrorResponse,
    CredentialSchemaResponse,
    Any
  ] =
    endpoint.post
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in("schema-registry" / "schemas")
      .in(
        jsonBody[CredentialSchemaInput]
          .description(
            "JSON object required for the credential schema creation"
          )
      )
      .out(
        statusCode(StatusCode.Created)
          .description(
            "The new credential schema record is successfully created"
          )
      )
      .out(jsonBody[CredentialSchemaResponse])
      .description("Credential schema record")
      .errorOut(basicFailureAndNotFoundAndForbidden)
      .name("createSchema")
      .summary("Publish new schema to the schema registry")
      .description(
        "Create the new credential schema record with metadata and internal JSON Schema on behalf of Cloud Agent. " +
          "The credential schema will be signed by the keys of Cloud Agent and issued by the DID that corresponds to it."
      )
      .tag(tagName)

  val updateSchemaEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String, UUID, CredentialSchemaInput),
    ErrorResponse,
    CredentialSchemaResponse,
    Any
  ] =
    endpoint.put
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "schema-registry" /
          path[String]("author").description(CredentialSchemaResponse.annotations.author.description) /
          path[UUID]("id").description(CredentialSchemaResponse.annotations.id.description)
      )
      .in(
        jsonBody[CredentialSchemaInput]
          .description(
            "JSON object required for the credential schema update"
          )
      )
      .out(
        statusCode(StatusCode.Ok)
          .description(
            "The credential schema record is successfully updated"
          )
      )
      .out(jsonBody[CredentialSchemaResponse])
      .description("Credential schema record")
      .errorOut(basicFailureAndNotFoundAndForbidden)
      .name("updateSchema")
      .summary("Publish the new version of the credential schema to the schema registry")
      .description(
        "Publish the new version of the credential schema record with metadata and internal JSON Schema on behalf of Cloud Agent. " +
          "The credential schema will be signed by the keys of Cloud Agent and issued by the DID that corresponds to it."
      )
      .tag(tagName)

  val getSchemaByIdEndpoint: PublicEndpoint[
    (RequestContext, UUID),
    ErrorResponse,
    CredentialSchemaResponse,
    Any
  ] =
    endpoint.get
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "schema-registry" / "schemas" / path[UUID]("guid").description(
          "Globally unique identifier of the credential schema record"
        )
      )
      .out(jsonBody[CredentialSchemaResponse].description("CredentialSchema found by `guid`"))
      .errorOut(basicFailuresAndNotFound)
      .name("getSchemaById")
      .summary("Fetch the schema from the registry by `guid`")
      .description(
        "Fetch the credential schema by the unique identifier"
      )
      .tag(tagName)

  private val credentialSchemaFilterInput: EndpointInput[FilterInput] = EndpointInput.derived[FilterInput]
  private val paginationInput: EndpointInput[PaginationInput] = EndpointInput.derived[PaginationInput]
  val lookupSchemasByQueryEndpoint: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (
        RequestContext,
        FilterInput,
        PaginationInput,
        Option[Order]
    ),
    ErrorResponse,
    CredentialSchemaResponsePage,
    Any
  ] =
    endpoint.get
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in("schema-registry" / "schemas".description("Lookup schemas by query"))
      .in(credentialSchemaFilterInput)
      .in(paginationInput)
      .in(query[Option[Order]]("order"))
      .out(jsonBody[CredentialSchemaResponsePage].description("Collection of CredentialSchema records."))
      .errorOut(basicFailuresAndForbidden)
      .name("lookupSchemasByQuery")
      .summary("Lookup schemas by indexed fields")
      .description(
        "Lookup schemas by `author`, `name`, `tags` parameters and control the pagination by `offset` and `limit` parameters "
      )
      .tag(tagName)
}
