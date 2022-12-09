package io.iohk.atala.mercury.protocol.connection

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.iohk.atala.mercury.model.{DidId, Message, PIURI}
import io.circe.syntax.*

object ConnectionResponse {
  def `type`: PIURI = "https://atalaprism.io/mercury/connections/1.0/response"

  final case class Body(
      goal_code: Option[String] = None,
      goal: Option[String] = None,
      accept: Option[Seq[String]] = None
  )

  object Body {
    given Encoder[Body] = deriveEncoder[Body]
    given Decoder[Body] = deriveDecoder[Body]
  }

  def makeResponseFromRequest(msg: Message): ConnectionResponse = {
    val cr: ConnectionRequest = ConnectionRequest.readFromMessage(msg)

    ConnectionResponse(
      body = ConnectionResponse.Body(
        goal_code = cr.body.goal_code,
        goal = cr.body.goal,
        accept = cr.body.accept,
      ),
      thid = msg.thid.orElse(Some(cr.id)),
      from = {
        assert(msg.to.length == 1, "The recipient is ambiguous. Need to have only 1 recipient") // TODO return error
        msg.to.head
      },
      to = msg.from.get, // TODO get
    )
  }

  def readFromMessage(message: Message): ConnectionResponse = {
    val body = message.body.asJson.as[ConnectionResponse.Body].toOption.get // TODO get
    ConnectionResponse(
      id = message.id,
      `type` = message.piuri,
      body = body,
      thid = message.thid,
      from = message.from.get, // TODO get
      to = {
        assert(message.to.length == 1, "The recipient is ambiguous. Need to have only 1 recipient") // TODO return error
        message.to.head
      },
    )
  }

  given Encoder[ConnectionResponse] = deriveEncoder[ConnectionResponse]

  given Decoder[ConnectionResponse] = deriveDecoder[ConnectionResponse]
}

final case class ConnectionResponse(
    `type`: PIURI = ConnectionResponse.`type`,
    id: String = java.util.UUID.randomUUID().toString,
    from: DidId,
    to: DidId,
    thid: Option[String],
    body: ConnectionResponse.Body,
) {
  assert(`type` == "https://atalaprism.io/mercury/connections/1.0/response")

  def makeMessage: Message = Message(
    id = this.id,
    piuri = this.`type`,
    from = Some(this.from),
    to = Seq(this.to),
    thid = this.thid,
    body = this.body.asJson.asObject.get,
  )
}
