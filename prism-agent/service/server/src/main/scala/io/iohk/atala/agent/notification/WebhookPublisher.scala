package io.iohk.atala.agent.notification
import io.iohk.atala.agent.notification.JsonEventEncoders.*
import io.iohk.atala.agent.notification.WebhookPublisherError.{InvalidWebhookURL, UnexpectedError}
import io.iohk.atala.agent.server.config.AppConfig
import io.iohk.atala.agent.walletapi.model.ManagedDIDDetail
import io.iohk.atala.connect.core.model.ConnectionRecord
import io.iohk.atala.event.notification.{Event, EventConsumer, EventNotificationService}
import io.iohk.atala.pollux.core.model.{IssueCredentialRecord, PresentationRecord}
import zio.*
import zio.http.*
import zio.http.model.*
import zio.json.*

import java.net.URL

class WebhookPublisher(appConfig: AppConfig, notificationService: EventNotificationService) {

  private val config = appConfig.agent.webhookPublisher
  private val baseHeaders =
    config.apiKey.map(key => Headers.bearerAuthorizationHeader(key)).getOrElse(Headers.empty) ++
      Headers.contentType(HeaderValues.applicationJson)

  private val parallelism = config.parallelism match {
    case Some(p) if p < 1  => 1
    case Some(p) if p > 10 => 10
    case Some(p)           => p
    case None              => 1
  }

  val run: ZIO[Client, WebhookPublisherError, Unit] = config.url match {
    case Some(url) =>
      for {
        url <- ZIO.attempt(URL(url)).mapError(th => InvalidWebhookURL(s"$url [${th.getMessage}]"))
        connectConsumer <- notificationService
          .consumer[ConnectionRecord]("Connect")
          .mapError(e => UnexpectedError(e.toString))
        issueConsumer <- notificationService
          .consumer[IssueCredentialRecord]("Issue")
          .mapError(e => UnexpectedError(e.toString))
        presentationConsumer <- notificationService
          .consumer[PresentationRecord]("Presentation")
          .mapError(e => UnexpectedError(e.toString))
        didStateConsumer <- notificationService
          .consumer[ManagedDIDDetail]("DIDDetail")
          .mapError(e => UnexpectedError(e.toString))
        _ <- pollAndNotify(connectConsumer, url).forever.debug.forkDaemon
        _ <- pollAndNotify(issueConsumer, url).forever.debug.forkDaemon
        _ <- pollAndNotify(presentationConsumer, url).forever.debug.forkDaemon
        _ <- pollAndNotify(didStateConsumer, url).forever.debug.forkDaemon
      } yield ()
    case None => ZIO.unit
  }

  private[this] def pollAndNotify[A](consumer: EventConsumer[A], url: URL)(implicit encoder: JsonEncoder[A]) = {
    for {
      _ <- ZIO.log(s"Polling $parallelism event(s)")
      events <- consumer.poll(parallelism).mapError(e => UnexpectedError(e.toString))
      _ <- ZIO.log(s"Got ${events.size} event(s)")
      _ <- ZIO.foreachPar(events)(e =>
        notifyWebhook(e, url)
          .retry(Schedule.spaced(5.second) && Schedule.recurs(2))
          .catchAll(e => ZIO.logError(s"Webhook permanently failing, with last error being: ${e.msg}"))
      )
    } yield ()
  }

  private[this] def notifyWebhook[A](event: Event[A], url: URL)(implicit
      encoder: JsonEncoder[A]
  ): ZIO[Client, UnexpectedError, Unit] = {
    for {
      _ <- ZIO.logDebug(s"Sending event: $event to HTTP webhook URL: $url.")
      response <- Client
        .request(
          url = url.toString,
          method = Method.POST,
          headers = baseHeaders,
          content = Body.fromString(event.toJson)
        )
        .timeoutFail(new RuntimeException("Client request timed out"))(5.seconds)
        .mapError(t => UnexpectedError(s"Webhook request error: $t"))
      resp <- response match
        case Response(status, _, _, _, _) if status.isSuccess =>
          ZIO.unit
        case Response(status, _, _, _, maybeHttpError) =>
          ZIO.fail(
            UnexpectedError(
              s"Unsuccessful webhook response: [status: $status] [error: ${maybeHttpError.getOrElse("none")}]"
            )
          )
    } yield resp
  }
}

object WebhookPublisher {
  val layer: URLayer[AppConfig & EventNotificationService, WebhookPublisher] =
    ZLayer.fromFunction(WebhookPublisher(_, _))
}
