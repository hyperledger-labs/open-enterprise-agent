package org.hyperledger.identus.mercury

import io.circe.*
import org.hyperledger.identus.mercury.model.*
import org.hyperledger.identus.mercury.model.error.*
import org.hyperledger.identus.mercury.protocol.routing.*
import org.hyperledger.identus.resolvers.DIDResolver
import zio.*

import scala.jdk.CollectionConverters.*

type HttpOrDID = String //TODO
case class ServiceEndpoint(uri: HttpOrDID, accept: Option[Seq[String]], routingKeys: Option[Seq[String]])
case class MessageAndAddress(msg: Message, url: String)

object MessagingService {

  /** Encrypted payload (message) and make the Forward Message */
  def makeForwardMessage(message: Message, mediator: DidId): URIO[DidOps & DidAgent, ForwardMessage] =
    for {
      didCommService <- ZIO.service[DidOps]
      encrypted <- didCommService.packEncrypted(message, to = message.to.head) // TODO head
      msg = ForwardMessage(
        to = mediator,
        expires_time = None,
        body = ForwardBody(next = message.to.head), // TODO check msg head
        attachments = Seq(AttachmentDescriptor.buildJsonAttachment(payload = encrypted.asJson)),
      )
    } yield (msg)

  /** Create a Message and any Forward Message as needed */
  def makeMessage(msg: Message): ZIO[
    DidOps & DidAgent & DIDResolver & HttpClient,
    SendMessageError,
    MessageAndAddress
  ] =
  for {
    didCommService <- ZIO.service[DidOps]
    resolver <- ZIO.service[DIDResolver]
    sendToDID <- msg.to match {
      case Seq() => // TODO support for anonymous message
        ZIO.fail(
          SendMessageError(
            new RuntimeException("Missing the destination DID - TODO support for anonymous message")
          )
        )
      case Seq(value) =>
        ZIO.succeed(value)
      case head +: tail => // TODO support for multiple destinations
        ZIO.fail(
          SendMessageError(
            new RuntimeException("TODO multiple destinations")
          )
        )
    }

    serviceEndpoint <- resolver
      .didCommServices(sendToDID) /* Seq[DIDCommService] */
      .catchAll { case ex => ZIO.fail(SendMessageError(ex)) }
      .flatMap {
        case Seq() =>
          ZIO.fail(
            SendMessageError(
              new RuntimeException("To send a Message you need a destination") // TODO ERROR
            )
          )
        case Seq(v) =>
          ZIO.succeed(
            ServiceEndpoint(
              uri = v.getServiceEndpoint(),
              accept = Option(v.getAccept()).map(_.asScala.toSeq),
              routingKeys = Option(v.getRoutingKeys()).map(_.asScala.toSeq)
            )
          )
        case headDID +: tail =>
          ZIO.logError("TODO multiple destinations") *>
            ZIO.succeed(
              ServiceEndpoint(
                uri = headDID.getServiceEndpoint(),
                accept = Option(headDID.getAccept()).map(_.asScala.toSeq),
                routingKeys = Option(headDID.getRoutingKeys()).map(_.asScala.toSeq)
              )
            )
      }

    msgToSend <- serviceEndpoint match {
      case ServiceEndpoint(url, _, None) if url.startsWith("http") =>
        ZIO.log(s"No Forward Message needed. (send to $url)") *>
          ZIO.succeed(MessageAndAddress(msg, url))
      case ServiceEndpoint(url, _, Some(Seq())) if url.startsWith("http") =>
        ZIO.log(s"No Forward Message needed. (send to $url)") *>
          ZIO.succeed(MessageAndAddress(msg, url))
      case ServiceEndpoint(did, _, _) if did.startsWith("did:") =>
        for {
          _ <- ZIO.log(s"Make Forward Message for Mediator '$did'")
          mediator = DidId(did)
          forwardMessage <- makeForwardMessage(message = msg, mediator = mediator)
          finalMessage <- makeMessage(forwardMessage.asMessage) // Maybe it needs a double warping
        } yield finalMessage
      case ServiceEndpoint(uri, _, Some(routingKeys)) =>
        ZIO.log(s"RoutingDID: $routingKeys") *>
          ZIO.fail(
            SendMessageError(
              RuntimeException("routingKeys is not supported at the moment")
            )
          )
      case s @ ServiceEndpoint(_, _, None) =>
        ZIO.logError(s"Unxpected ServiceEndpoint $s") *> ZIO.fail(
          SendMessageError(new RuntimeException(s"Unxpected ServiceEndpoint $s"))
        )
    }
  } yield (msgToSend)

  /** Encrypt and send a Message via HTTP
    *
    * TODO Move this method to another model
    */
  def send(msg: Message): ZIO[DidOps & DidAgent & DIDResolver & HttpClient, SendMessageError, HttpResponse] =
    ZIO.logAnnotate("msgId", msg.id) {
      for {
        auxFinalMessage <- makeMessage(msg)
        MessageAndAddress(finalMessage, serviceEndpoint) = auxFinalMessage
        didCommService <- ZIO.service[DidOps]
        to <- finalMessage.to match {
          case Seq()            => ZIO.fail(SendMessageError(new RuntimeException("Message must have a recipient")))
          case firstTo +: Seq() => ZIO.succeed(firstTo)
          case all @ (firstTo +: _) =>
            ZIO.logWarning(s"Message have multi recipients: $all") *> ZIO.succeed(firstTo)
        }
        encryptedMessage <-
          if (finalMessage.`type` == ForwardMessage.PIURI) didCommService.packEncryptedAnon(msg = finalMessage, to = to)
          else didCommService.packEncrypted(msg = finalMessage, to = to)

        _ <- ZIO.log(s"Sending a Message to '$serviceEndpoint'")
        resp <- org.hyperledger.identus.mercury.HttpClient
          .postDIDComm(url = serviceEndpoint, data = encryptedMessage.string)
          .catchAll { case ex => ZIO.fail(SendMessageError(ex, Some(encryptedMessage.string))) }
        _ <- ZIO.when(resp.status >= 300)(
          ZIO.logWarning(
            s"Message to '$serviceEndpoint' return [${resp.status}] '${resp.bodyAsString}' for the request '${encryptedMessage.string}'"
          )
        )

      } yield (resp)
    }

}
