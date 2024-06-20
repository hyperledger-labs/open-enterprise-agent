package org.hyperledger.identus.agent.server.jobs

import org.hyperledger.identus.mercury.HttpResponse
import org.hyperledger.identus.shared.models._

sealed trait BackgroundJobError(
    override val statusCode: org.hyperledger.identus.shared.models.StatusCode,
    override val userFacingMessage: String
) extends Failure {
  override val namespace: String = "BackgroundJobError"
}

object BackgroundJobError {
  final case class InvalidState(cause: String)
      extends BackgroundJobError(
        statusCode = StatusCode.FixmeStatusCode,
        userFacingMessage = s"Invalid State: cause='$cause'"
      )

  case object NotImplemented
      extends BackgroundJobError(
        statusCode = StatusCode.FixmeStatusCode,
        userFacingMessage = s"NotImplemented"
      )
  final case class ErrorResponseReceivedFromPeerAgent(response: HttpResponse)
      extends BackgroundJobError(
        statusCode = StatusCode.FixmeStatusCode,
        userFacingMessage = s"DIDComm sending error: [${response.status}] - ${response.bodyAsString}"
      ) {
    override def toString: String = s"DIDComm sending error: [${response.status}] - ${response.bodyAsString}"
  }
}
