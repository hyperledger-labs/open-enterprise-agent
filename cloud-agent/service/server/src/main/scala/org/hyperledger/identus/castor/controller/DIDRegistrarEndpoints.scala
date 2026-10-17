package org.hyperledger.identus.castor.controller

import org.hyperledger.identus.api.http.{EndpointOutputs, ErrorResponse, RequestContext}
import org.hyperledger.identus.api.http.model.PaginationInput
import org.hyperledger.identus.api.http.EndpointOutputs.FailureVariant
import org.hyperledger.identus.castor.controller.http.{
  CreateManagedDIDResponse,
  CreateManagedDidRequest,
  DIDInput,
  DIDOperationResponse,
  ManagedDID,
  ManagedDIDPage,
  UpdateManagedDIDRequest
}
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyCredentials
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyEndpointSecurityLogic.apiKeyHeader
import org.hyperledger.identus.iam.authentication.oidc.JwtCredentials
import org.hyperledger.identus.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import sttp.apispec.Tag
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody

object DIDRegistrarEndpoints {

  private val tagName = "DID Registrar"
  private val tagDescription =
    s"""
       |The __${tagName}__ endpoints facilitate the management of [PRISM DIDs](https://github.com/input-output-hk/prism-did-method-spec) hosted in the cloud agent.
       |
       |Implementation of [DID management](https://identus.io/documentation/develop/cloud-agent/did-management/) in the cloud agent.
       |The agent securely manages and stores DIDs along with their keys in its secret storage.
       |These endpoints allow users to create, read, update, deactivate, and publish without direct exposure to the key material.
       |These DIDs can be utilized for various operations during issuance and verification processes.
       |
       |More examples and tutorials can be found in the [Identus documentation](https://identus.io/documentation/develop/).
       |""".stripMargin

  val tag = Tag(tagName, Some(tagDescription))

  private val baseEndpoint = endpoint
    .tag(tagName)
    .in("did-registrar" / "dids")
    .in(extractFromRequest[RequestContext](RequestContext.apply))
    .securityIn(apiKeyHeader)
    .securityIn(jwtAuthHeader)

  private val paginationInput: EndpointInput[PaginationInput] = EndpointInput.derived[PaginationInput]

  val listManagedDid: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, PaginationInput),
    ErrorResponse,
    ManagedDIDPage,
    Any
  ] = baseEndpoint.get
    .in(paginationInput)
    .errorOut(EndpointOutputs.basicFailuresAndForbidden)
    .out(statusCode(StatusCode.Ok).description("List the agent managed DIDs in the wallet"))
    .out(jsonBody[ManagedDIDPage])
    .summary("List all DIDs stored in the agent's wallet")
    .description(
      """List all DIDs stored in the agent's wallet.
        |Returns paginated items ordered by created timestamp.""".stripMargin
    )

  val createManagedDid: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, CreateManagedDidRequest),
    ErrorResponse,
    CreateManagedDIDResponse,
    Any
  ] = baseEndpoint.post
    .in(jsonBody[CreateManagedDidRequest])
    .errorOut(EndpointOutputs.basicFailuresAndForbiddenWith(FailureVariant.notFound))
    .out(statusCode(StatusCode.Created).description("Created an unpublished PRISM DID"))
    .out(jsonBody[CreateManagedDIDResponse])
    .summary("Create an unpublished PRISM DID and store it in the agent's wallet")
    .description(
      """Create an unpublished PRISM DID and store it in the agent's wallet.
        |The public/private keys of the DID will be derived according to the `didDocumentTemplate` and managed by the agent.
        |The DID can later be published to the VDR using the `publications` endpoint.
        |After the DID is created, it has the `CREATED` status.""".stripMargin
    )

  val getManagedDid: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String),
    ErrorResponse,
    ManagedDID,
    Any
  ] = baseEndpoint.get
    .in(DIDInput.didRefPathSegment)
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .out(statusCode(StatusCode.Ok).description("Get a DID in the agent's wallet"))
    .out(jsonBody[ManagedDID])
    .summary("Get a specific DID stored in the agent's wallet")
    .description("Get a specific DID stored in the agent's wallet")

  val publishManagedDid: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String),
    ErrorResponse,
    DIDOperationResponse,
    Any
  ] = baseEndpoint.post
    .in(DIDInput.didRefPathSegment / "publications")
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .out(statusCode(StatusCode.Accepted).description("Publishing DID to the VDR initiated"))
    .out(jsonBody[DIDOperationResponse])
    .summary("Publish the DID stored in the agent's wallet to the VDR")
    .description(
      """Initiate the publication of the DID stored in the agent's wallet to the VDR.
        |The publishing process is asynchronous.
        |Attempting to publish the same DID while the previous publication is ongoing will not initiate another publication.
        |After the submission of the DID publication, its status is changed to `PUBLICATION_PENDING`.
        |Upon confirmation after a predefined number of blocks, the status is changed to `PUBLISHED`.
        |In case of a failed DID publication, the status is reverted to `CREATED`.
        |""".stripMargin
    )

  val updateManagedDid: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String, UpdateManagedDIDRequest),
    ErrorResponse,
    DIDOperationResponse,
    Any
  ] = baseEndpoint.post
    .in(DIDInput.didRefPathSegment / "updates")
    .in(jsonBody[UpdateManagedDIDRequest])
    .errorOut(EndpointOutputs.basicFailuresAndForbiddenWith(FailureVariant.notFound, FailureVariant.conflict))
    .out(statusCode(StatusCode.Accepted).description("DID update operation accepted"))
    .out(jsonBody[DIDOperationResponse])
    .summary("Update DID in the agent's wallet and post update operation to the VDR")
    .description(
      """Update DID in the agent's wallet and post the update operation to the VDR.
        |Only the DID with status `PUBLISHED` can be updated.
        |This endpoint updates the DID document from the last confirmed operation.
        |The update operation is an asynchronous operation and the agent will reject
        |a new update request if the previous operation is not yet confirmed.""".stripMargin
    )

  val deactivateManagedDid: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, String),
    ErrorResponse,
    DIDOperationResponse,
    Any
  ] = baseEndpoint.post
    .in(DIDInput.didRefPathSegment / "deactivations")
    .errorOut(EndpointOutputs.basicFailuresAndForbiddenWith(FailureVariant.notFound))
    .out(statusCode(StatusCode.Accepted).description("DID deactivation operation accepted"))
    .out(jsonBody[DIDOperationResponse])
    .summary("Deactivate DID in the agent's wallet and post deactivate operation to the VDR")
    .description(
      """Deactivate DID in the agent's wallet and post deactivate operation to the VDR.
        |Only the DID with status `PUBLISHED` can be deactivated.
        |The deactivation operation is an asynchronous operation and the agent will reject
        |a new deactivate request if the previous operation is not yet confirmed.""".stripMargin
    )

}
