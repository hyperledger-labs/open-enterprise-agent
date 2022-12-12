package io.iohk.atala.mercury.protocol.connection
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.iohk.atala.mercury.model.{AttachmentDescriptor, DidId, Message, PIURI}
import io.circe.syntax.*
import io.iohk.atala.mercury.protocol.connection.ConnectionRequest.Body

object ConnectionRequest {
  def `type`: PIURI = "https://atalaprism.io/mercury/connections/1.0/request"

  case class Body(
      goal_code: Option[String] = None,
      goal: Option[String] = None,
      accept: Option[Seq[String]] = None
  )

  object Body {
    given Encoder[Body] = deriveEncoder[Body]

    given Decoder[Body] = deriveDecoder[Body]
  }

  given Encoder[ConnectionRequest] = deriveEncoder[ConnectionRequest]

  given Decoder[ConnectionRequest] = deriveDecoder[ConnectionRequest]

  def readFromMessage(message: Message): ConnectionRequest = {
    val body = message.body.asJson.as[ConnectionRequest.Body].toOption.get // TODO get
    ConnectionRequest(
      id = message.id,
      `type` = message.piuri,
      body = body,
      thid = message.thid,
      from = message.from.get, // TODO get
      to = {
        assert(
          message.to.length == 1,
          "The recipient is ambiguous. Need to have only 1 recipient"
        ) // TODO return error return error
        message.to.head
      },
    )
  }

}

final case class ConnectionRequest(
    `type`: PIURI = ConnectionRequest.`type`,
    id: String = java.util.UUID.randomUUID().toString,
    from: DidId,
    to: DidId,
    thid: Option[String],
    body: Body,
) {
  assert(`type` == "https://atalaprism.io/mercury/connections/1.0/request")

  def makeMessage: Message = Message(
    id = this.id,
    `type` = this.`type`,
    from = Some(this.from),
    to = Seq(this.to),
    thid = this.thid,
    body = this.body.asJson.asObject.get,
  )
}
