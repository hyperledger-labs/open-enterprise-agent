package org.hyperledger.identus.shared.models

import zio.{URIO, ZIO}

trait Failure {
  val namespace: String
  val statusCode: StatusCode
  val userFacingMessage: String

  def toUnmanagedFailureException = UnmanagedFailureException(this)
}

object Failure {
  extension [R, E](effect: ZIO[R, Failure, E]) {
    def orDieAsUnmanagedFailure: URIO[R, E] = {
      effect.orDieWith(f => UnmanagedFailureException(f))
    }
  }
}

case class UnmanagedFailureException(val failure: Failure) extends Throwable {
  override def getMessage: String = failure.toString
}

sealed class StatusCode(val code: Int)

object StatusCode {
  val BadRequest: StatusCode = StatusCode(400)
  val NotFound: StatusCode = StatusCode(404)
  val UnprocessableContent: StatusCode = StatusCode(422)

  val InternalServerError: StatusCode = StatusCode(500)
  val BadGateway: StatusCode = StatusCode(502)

  val FixmeStatusCode: StatusCode = StatusCode(500) // FIXME TODO
}
