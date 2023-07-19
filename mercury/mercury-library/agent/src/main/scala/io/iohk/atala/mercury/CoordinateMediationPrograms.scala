package io.iohk.atala.mercury

import scala.util.chaining._
import zio._
import io.circe.parser._
import io.circe.JsonObject

import io.iohk.atala.mercury._
import io.iohk.atala.mercury.model._
import io.iohk.atala.mercury.protocol.coordinatemediation._
import io.iohk.atala.mercury.protocol.invitation.v2.Invitation

object CoordinateMediationPrograms {

  def replyToInvitation(replier: DidId, invitation: Invitation) = {
    val requestMediation = MediateRequest()
    Message(
      from = Some(replier),
      to = Seq(invitation.from),
      id = requestMediation.id,
      `type` = requestMediation.`type`
    )
  }

  private def toPrettyJson(parseToJson: String) = {
    parse(parseToJson).getOrElse(???).spaces2
  }

  def senderMediationRequestProgram(mediatorURL: String = "http://localhost:8000") = {

    for {
      _ <- ZIO.log("#### Send Mediation request ####")
      link <- InvitationPrograms
        .getInvitationProgram(mediatorURL + "/oob_url")
        .map(_.toOption) // FIXME
      opsService <- ZIO.service[DidOps]
      agentService <- ZIO.service[DidAgent]

      planMessage = link.map(to => replyToInvitation(agentService.id, to)).get
      invitationFrom = link.get.from
      _ <- ZIO.log(s"Invitation from $invitationFrom")

      encryptedMessage <- opsService.packEncrypted(planMessage, to = invitationFrom)
      _ <- ZIO.log("Sending bytes ...")
      jsonString = encryptedMessage.string
      _ <- ZIO.log(jsonString)

      client <- ZIO.service[HttpClient]
      res <- client.postDIDComm(mediatorURL, jsonString)
      _ <- ZIO.log(res.bodyAsString)

      messageReceived <- opsService.unpack(res.bodyAsString)
      _ <- Console.printLine("Unpacking and decrypting the received message ...")
      _ <- Console.printLine("*" * 100)
      _ <- Console.printLine(toPrettyJson(messageReceived.message.toString))
      _ <- Console.printLine("*" * 100)
      ret = parse(messageReceived.message.toString)
        .getOrElse(???)
        // .flatMap()
        .pipe { json =>
          json.as[MediateGrant] match {
            case Right(mediateGrant) => Right(mediateGrant)
            case Left(_) =>
              json.as[MediateDeny] match {
                case Right(mediateDeny) => Left(mediateDeny)
                case Left(_)            => ???
              }
          }
        }
      _ <- Console.printLine(ret)
      _ <- Console.printLine("*" * 100)
    } yield (ret)
  }

}
