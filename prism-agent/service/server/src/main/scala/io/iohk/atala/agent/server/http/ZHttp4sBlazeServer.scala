package io.iohk.atala.agent.server.http

import io.iohk.atala.api.http.ErrorResponse
import io.iohk.atala.shared.models.{ContextRef, WalletAccessContext, WalletId}
import io.iohk.atala.system.controller.SystemEndpoints
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.*
import sttp.tapir.model.ServerRequest
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.server.interceptor.RequestInterceptor.RequestResultEffectTransform
import sttp.tapir.server.interceptor.{RequestInterceptor, RequestResult}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.ztapir.ZServerEndpoint
import zio.*
import zio.interop.catz.*

import java.util.UUID

class ZHttp4sBlazeServer(micrometerRegistry: PrometheusMeterRegistry) {

  private val tapirPrometheusMetricsZIO: Task[PrometheusMetrics[Task]] = ZIO.attempt {
    PrometheusMetrics.default[Task](registry = micrometerRegistry.getPrometheusRegistry)
  }

  private val walletIdInterceptor = RequestInterceptor.transformResultEffect(new RequestResultEffectTransform[Task] {
    override def apply[B](request: ServerRequest, result: Task[RequestResult[B]]): Task[RequestResult[B]] = {
      ContextRef.walletAccessContext.update { cr =>
        val uuid = UUID.fromString(request.queryParameters.get("walletId").get)
        cr.copy(Some(WalletAccessContext(WalletId.fromUUID(uuid))))
      } *> result
    }
  })

  private val serverOptionsZIO: ZIO[PrometheusMetrics[Task], Throwable, Http4sServerOptions[Task]] = for {
    srv <- ZIO.service[PrometheusMetrics[Task]]
    options <- ZIO.attempt {
      Http4sServerOptions
        .customiseInterceptors[Task]
        .defaultHandlers(ErrorResponse.failureResponseHandler)
        .metricsInterceptor(
          srv.metricsInterceptor(
            ignoreEndpoints = Seq(SystemEndpoints.metrics)
          )
        )
        .prependInterceptor(walletIdInterceptor)
        .options
    }
  } yield options

  def start(
      endpoints: List[ZServerEndpoint[Any, Any]],
      port: Int
  ): Task[ExitCode] = {

    val serve = for {
      metrics <- tapirPrometheusMetricsZIO
      options <- serverOptionsZIO.provide(ZLayer.succeed(metrics))
      serve <- ZIO.attempt {
        val http4sEndpoints: HttpRoutes[Task] =
          ZHttp4sServerInterpreter(options)
            .from(endpoints)
            .toRoutes

        ZIO.executor.flatMap(executor =>
          BlazeServerBuilder[Task]
            .withExecutionContext(executor.asExecutionContext)
            .bindHttp(port, "0.0.0.0")
            .withHttpApp(Router("/" -> http4sEndpoints).orNotFound)
            .serve
            .compile
            .drain
        )
      }
    } yield serve

    serve.flatten.exitCode
  }
}

object ZHttp4sBlazeServer {
  def make: URIO[PrometheusMeterRegistry, ZHttp4sBlazeServer] = {
    for {
      micrometerRegistry <- ZIO.service[PrometheusMeterRegistry]
      zHttp4sBlazeServer = ZHttp4sBlazeServer(micrometerRegistry)
    } yield zHttp4sBlazeServer
  }
}
