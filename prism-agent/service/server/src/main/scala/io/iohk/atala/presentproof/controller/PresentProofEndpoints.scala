package io.iohk.atala.presentproof.controller

import io.iohk.atala.api.http.EndpointOutputs.*
import io.iohk.atala.api.http.model.PaginationInput
import io.iohk.atala.api.http.{ErrorResponse, RequestContext}
import io.iohk.atala.iam.authentication.apikey.ApiKeyCredentials
import io.iohk.atala.iam.authentication.apikey.ApiKeyEndpointSecurityLogic.apiKeyHeader
import io.iohk.atala.iam.authentication.oidc.JwtCredentials
import io.iohk.atala.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import io.iohk.atala.presentproof.controller.http.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody

import java.util.UUID

object PresentProofEndpoints {

  private val paginationInput: EndpointInput[PaginationInput] = EndpointInput.derived[PaginationInput]

  val requestPresentation: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, RequestPresentationInput),
    ErrorResponse,
    PresentationStatus,
    Any
  ] =
    endpoint.post
      .tag("Present Proof")
      .name("requestPresentation")
      .summary("As a Verifier, create a new proof presentation request and send it to the Prover.")
      .description("Holder presents proof derived from the verifiable credential to verifier.")
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in("present-proof" / "presentations")
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(jsonBody[RequestPresentationInput].description("The present proof creation request."))
      .out(
        statusCode(StatusCode.Created).description(
          "The proof presentation request was created successfully and will be sent asynchronously to the Prover."
        )
      )
      .out(jsonBody[PresentationStatus])
      .errorOut(basicFailureAndNotFoundAndForbidden)

  val oobRequestPresentation: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, OOBRequestPresentation),
    ErrorResponse,
    OOBPresentation,
    Any
  ] =
    endpoint.post
      .tag("Present Proof")
      .name("oobRequestPresentation")
      .summary("As a Verifier, create a new OOB proof presentation request and send it to the Prover.")
      .description("Holder presents proof derived from the verifiable credential to verifier.")
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in("present-proof" / "presentations" / "invitation")
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(jsonBody[OOBRequestPresentation].description("The present proof creation request."))
      .out(
        statusCode(StatusCode.Created).description(
          "The proof presentation request was created successfully and will be sent asynchronously to the Prover."
        )
      )
      .out(jsonBody[OOBPresentation])
      .errorOut(basicFailureAndNotFoundAndForbidden)

  val acceptRequestPresentationInvitation: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, AcceptRequestPresentationInvitationRequest),
    ErrorResponse,
    PresentationStatus,
    Any
  ] =
    endpoint.post
      .tag("Present Proof")
      .name("acceptRequestPresentationInvitationEndpoint")
      .summary("As a Prover or Holder, accept OOB  presentation request invitation")
      .description("""Accept an new presentation request invitation received out-of-band from another peer agent.
           |The invitation must be compliant with the DIDComm Messaging v2.0 - [Out of Band Messages](https://identity.foundation/didcomm-messaging/spec/v2.0/#out-of-band-messages) specification [section 9.5.4](https://identity.foundation/didcomm-messaging/spec/v2.0/#invitation).
           |A new Presentation record with state `RequestReceived` will be created in the agent.
           |The created record will contain a newly generated pairwise Peer DID used for that presentation exchange.
           |""".stripMargin)
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in("present-proof" / "presentations" / "accept-invitation")
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(jsonBody[AcceptRequestPresentationInvitationRequest].description("The present proof creation request."))
      .out(
        statusCode(StatusCode.Created).description(
          "The Presentation record was created successfully with status RequestReceived"
        )
      )
      .out(jsonBody[PresentationStatus])
      .errorOut(basicFailureAndNotFoundAndForbidden)

  val getAllPresentations: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, PaginationInput, Option[String]),
    ErrorResponse,
    PresentationStatusPage,
    Any
  ] =
    endpoint.get
      .tag("Present Proof")
      .name("getAllPresentation")
      .summary("Gets the list of proof presentation records.")
      .description("list of presentation statuses")
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in("present-proof" / "presentations")
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(paginationInput)
      .in(query[Option[String]]("thid"))
      .out(statusCode(StatusCode.Ok).description("The list of proof presentation records."))
      .out(jsonBody[PresentationStatusPage])
      .errorOut(basicFailuresAndForbidden)

  val getPresentation
      : Endpoint[(ApiKeyCredentials, JwtCredentials), (RequestContext, UUID), ErrorResponse, PresentationStatus, Any] =
    endpoint.get
      .tag("Present Proof")
      .name("getPresentation")
      .summary(
        "Gets an existing proof presentation record by its unique identifier. " +
          "More information on the error can be found in the response body."
      )
      .description("Returns an existing presentation record by id.")
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "present-proof" / "presentations" / path[UUID]("presentationId").description(
          "The unique identifier of the presentation record."
        )
      )
      .out(statusCode(StatusCode.Ok).description("The proof presentation record."))
      .out(jsonBody[PresentationStatus])
      .errorOut(basicFailureAndNotFoundAndForbidden)

  val updatePresentation: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID, RequestPresentationAction),
    ErrorResponse,
    PresentationStatus,
    Any
  ] =
    endpoint.patch
      .tag("Present Proof")
      .name("updatePresentation")
      .summary(
        "Updates the proof presentation record matching the unique identifier, " +
          "with the specific action to perform."
      )
      .description("Accept or reject presentation of proof request.")
      .securityIn(apiKeyHeader)
      .securityIn(jwtAuthHeader)
      .in(extractFromRequest[RequestContext](RequestContext.apply))
      .in(
        "present-proof" / "presentations" / path[UUID]("presentationId").description(
          "The unique identifier of the presentation record."
        )
      )
      .in(jsonBody[RequestPresentationAction].description("The action to perform on the proof presentation record."))
      .out(statusCode(StatusCode.Ok).description("The proof presentation record was successfully updated."))
      .out(jsonBody[PresentationStatus])
      .errorOut(basicFailureAndNotFoundAndForbidden)

}
