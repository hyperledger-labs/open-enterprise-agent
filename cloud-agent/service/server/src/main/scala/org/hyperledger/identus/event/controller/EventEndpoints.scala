package org.hyperledger.identus.event.controller

import org.hyperledger.identus.api.http.{EndpointOutputs, ErrorResponse, RequestContext}
import org.hyperledger.identus.api.http.EndpointOutputs.FailureVariant
import org.hyperledger.identus.event.controller.http.{
  CreateWebhookNotification,
  WebhookNotification,
  WebhookNotificationPage
}
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyCredentials
import org.hyperledger.identus.iam.authentication.apikey.ApiKeyEndpointSecurityLogic.apiKeyHeader
import org.hyperledger.identus.iam.authentication.oidc.JwtCredentials
import org.hyperledger.identus.iam.authentication.oidc.JwtSecurityLogic.jwtAuthHeader
import sttp.apispec.Tag
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.zio.jsonBody

import java.util.UUID

object EventEndpoints {

  private val tagName = "Events"
  private val tagDescription =
    s"""The __${tagName}__ endpoints enable users to manage event-related resources, such as webhook notifications.
       |These notifications are specifically designed to inform about events occurring within the wallet, including but not limited to:
       |
       |- DID publication notifications
       |- DIDComm connection notifications
       |- Issuance protocol notifications
       |- Presentation protocol notifications
       |
       |For more detailed information regarding event notifications, please refer to the [documentation](https://identus.io/cloud-agent/docs/docusaurus/webhooks/webhook/).
       |""".stripMargin

  val tag = Tag(tagName, Some(tagDescription))

  private val baseEndpoint = endpoint
    .tag(tagName)
    .in("events")
    .securityIn(apiKeyHeader)
    .securityIn(jwtAuthHeader)
    .in(extractFromRequest[RequestContext](RequestContext.apply))

  val createWebhookNotification: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, CreateWebhookNotification),
    ErrorResponse,
    WebhookNotification,
    Any
  ] = baseEndpoint.post
    .in("webhooks")
    .in(jsonBody[CreateWebhookNotification])
    .errorOut(EndpointOutputs.basicFailuresAndForbiddenWith(FailureVariant.conflict))
    .out(statusCode(StatusCode.Ok).description("Webhook notification has been created successfully"))
    .out(jsonBody[WebhookNotification])
    .summary("Create a wallet webhook notification")
    .description(
      """Create a new wallet webhook notification and subscribe to events.
        |A dispatched webhook request may contain static custom headers for authentication or custom metadata.
      """.stripMargin
    )

  val listWebhookNotification: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    RequestContext,
    ErrorResponse,
    WebhookNotificationPage,
    Any
  ] = baseEndpoint.get
    .in("webhooks")
    .errorOut(EndpointOutputs.basicFailuresAndForbidden)
    .out(statusCode(StatusCode.Ok).description("Wallet webhook notifications listed successfully"))
    .out(jsonBody[WebhookNotificationPage])
    .summary("List wallet webhook notifications")
    .description(
      """List all registered webhook notifications.
        |Each webhook notification contains a unique identifier, the URL to which the events are sent,
        |and the custom headers to be included in the dispatched webhook request.
      """.stripMargin
    )

  val deleteWebhookNotification: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, UUID),
    ErrorResponse,
    Unit,
    Any
  ] = baseEndpoint.delete
    .in("webhooks" / path[UUID]("id").description("ID of the webhook notification to delete."))
    .errorOut(EndpointOutputs.basicFailureAndNotFoundAndForbidden)
    .out(statusCode(StatusCode.Ok).description("Webhook notification has been deleted."))
    .summary("Delete the wallet webhook notification by `id`")

}
