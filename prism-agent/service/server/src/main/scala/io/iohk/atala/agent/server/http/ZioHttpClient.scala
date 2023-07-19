package io.iohk.atala.agent.server.http

import zio._
import zio.http._
import zio.http.model.{Header => _, _}
import io.iohk.atala.mercury._

object ZioHttpClient {
  val layer = ZLayer.succeed(new ZioHttpClient())
}

class ZioHttpClient extends HttpClient {

  override def get(url: String): Task[HttpResponse] =
    zio.http.Client
      .request(url)
      .provideSomeLayer(zio.http.Client.default)
      .provideSomeLayer(zio.Scope.default)
      .flatMap { response =>
        response.headers.toSeq.map(e => e)
        response.body.asString
          .map(body =>
            HttpResponse(
              response.status.code,
              response.headers.toSeq.map(h => Header(h.key.toString, h.value.toString)),
              body
            )
          )
      }

  def postDIDComm(url: String, data: String): Task[HttpResponse] =
    zio.http.Client
      .request(
        url = url, // TODO make ERROR type
        method = Method.POST,
        headers = Headers("content-type" -> "application/didcomm-encrypted+json"),
        // headers = Headers("content-type" -> MediaTypes.contentTypeEncrypted),
        content = Body.fromChunk(Chunk.fromArray(data.getBytes)),
        // ssl = ClientSSLOptions.DefaultSSL,
      )
      .provideSomeLayer(zio.http.Client.default)
      .provideSomeLayer(zio.Scope.default)
      .flatMap { response =>
        response.headers.toSeq.map(e => e)
        response.body.asString
          .map(body =>
            HttpResponse(
              response.status.code,
              response.headers.toSeq.map(h => Header(h.key.toString, h.value.toString)),
              body
            )
          )
      }
}
