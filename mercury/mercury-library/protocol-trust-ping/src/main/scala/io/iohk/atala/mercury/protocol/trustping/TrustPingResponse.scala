package io.iohk.atala.mercury.protocol.trustping

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.iohk.atala.mercury.model._

final case class TrustPingResponse(
    `type`: PIURI = TrustPing.`type`,
    id: String = java.util.UUID.randomUUID().toString,
    thid: String,
    from: DidId,
    to: DidId,
) {
  assert(`type` == TrustPingResponse.`type`)

  def makeMessage: Message = Message(
    id = this.id,
    thid = Some(this.thid),
    `type` = this.`type`,
    from = Some(this.from),
    to = Seq(this.to),
  )
}

object TrustPingResponse {
  def `type`: PIURI = "https://didcomm.org/trust-ping/2.0/ping-response"

  /** Parse a generecy DIDComm Message into a TrustPingResponse */
  def fromMessage(message: Message): Either[String, TrustPingResponse] =
    for {
      piuri <-
        if (message.`type` == TrustPingResponse.`type`) Right(message.`type`)
        else Left(s"Message MUST be of the type '${TrustPingResponse.`type`}' instead of '${message.`type`}'")
      ret <- message.to match
        case Seq(onlyOneRecipient) =>
          message.from match
            case Some(from) =>
              Right(
                TrustPingResponse(
                  id = message.id,
                  thid = message.thid.getOrElse(???), // TODO
                  `type` = piuri,
                  from = from,
                  to = onlyOneRecipient,
                )
              )
            case None => Left("TrustPingResponse needs to define the recipient")
        case _ => Left("The recipient is ambiguous. Need to have only 1 recipient")
    } yield ret
}
