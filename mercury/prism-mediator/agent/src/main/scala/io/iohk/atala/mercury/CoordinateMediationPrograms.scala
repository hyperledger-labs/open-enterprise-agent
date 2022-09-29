package io.iohk.atala.mercury

import zio._
import zhttp.service.Client
import zhttp.http._
import io.circe.Json._
import io.circe.syntax._
import io.circe.parser._
import io.circe.JsonObject

import io.iohk.atala.mercury.{_, given}
import io.iohk.atala.mercury.model._
import io.iohk.atala.mercury.protocol.coordinatemediation._
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation

object CoordinateMediationPrograms {

  def replyToInvitation(from: DidId, invitation: Invitation) = {
    val requestMediation = MediateRequest()
    Message(
      from = from,
      to = DidId(invitation.from),
      id = requestMediation.id,
      piuri = requestMediation.`type`
    )
  }

  def toPrettyJson(parseToJson: String) = {
    parse(parseToJson).getOrElse(???).spaces2
  }

  def senderMediationRequestProgram() = {
    val mediatorURL = "http://localhost:8000"

    def makeMsg(from: Agent, to: DidId) = Message(
      piuri = "http://atalaprism.io/lets_connect/proposal",
      from = from.id,
      to = to,
      body = Map(
        "connectionId" -> "8fb9ea21-d094-4506-86b6-c7c1627d753a",
        "msg" -> "Hello Bob"
      ),
    )

    for {
      _ <- ZIO.log("#### Send Mediation request  ####")
      link <- InvitationPrograms.getInvitationProgram(mediatorURL + "/oob_url")
      planMessage = link.map(to => replyToInvitation(Agent.Charlie.id, to)).get
      invitationFrom = DidId(link.get.from)
      _ <- ZIO.log(s"Invitation from $invitationFrom")

      charlie <- ZIO.service[DidComm]
      encryptedMessage <- charlie.packEncrypted(planMessage, to = invitationFrom)
      _ <- ZIO.log("Sending bytes ...")
      jsonString = encryptedMessage.string
      _ <- ZIO.log(jsonString)

      res <- Client.request(
        url = mediatorURL,
        method = Method.POST,
        headers = Headers("content-type" -> MediaTypes.contentTypeEncrypted),
        content = HttpData.fromChunk(Chunk.fromArray(jsonString.getBytes)),
        // ssl = ClientSSLOptions.DefaultSSL,
      )
      data <- res.bodyAsString
      _ <- Console.printLine("  ")
      _ <- Console.printLine(data)
      _ <- Console.printLine("  ")
      data2 = parse(data).getOrElse(???).asString.get // FIXME https://github.com/roots-id/didcomm-mediator/issues/16
      _ <- Console.printLine(parse(parse(data).getOrElse(???).asString.get).getOrElse(???).spaces2)

      messageReceived <- charlie.unpack(data2)
      _ <- Console.printLine("Unpacking and decrypting the received message ...")
      _ <- Console.printLine(
        "\n*********************************************************************************************************************************\n"
          + toPrettyJson(messageReceived.getMessage.toString)
          + "\n********************************************************************************************************************************\n"
      )
    } yield ()
  }

}
