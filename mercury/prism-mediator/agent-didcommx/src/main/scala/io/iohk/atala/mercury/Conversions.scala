package io.iohk.atala.mercury

import org.didcommx.didcomm.model._
import org.didcommx.didcomm.message.MessageBuilder
import org.didcommx.didcomm.message.{Attachment => XAttachment}

import scala.jdk.CollectionConverters._

import io.iohk.atala.mercury.model._
import java.util.Random
import io.circe._

// TODO move you another module
given Conversion[PackEncryptedResult, EncryptedMessage] with {
  def apply(msg: PackEncryptedResult): EncryptedMessage =
    EncryptedMessage(msg)
}

// TODO move you another module
given Conversion[Message, org.didcommx.didcomm.message.Message] with {
  def apply(msg: Message): org.didcommx.didcomm.message.Message = {
    val attachments = msg.attachments.map { e => e: XAttachment } // cast
    new MessageBuilder(msg.id, msg.body.asJava, msg.piuri)
      .from(msg.from.value)
      .to(Seq(msg.to.value).asJava)
      .createdTime(msg.createdTime)
      .expiresTime(msg.createdTime + msg.expiresTimePlus)
      .attachments(attachments.toList.asJava) // TODO test
      .build()
  }
}

def json2Map(json: Json): Any = json match {
  case e if e.isArray   => e.asArray.get.toList.map(j => json2Map(j)).asJava
  case e if e.isBoolean => e.asBoolean.get
  case e if e.isNull    => null
  case e if e.isNumber  => e.asNumber.flatMap(_.toBigDecimal).get
  case e if e.isObject  => e.asObject.get.toMap.mapValues(json2Map).toMap.asJava
  case e if e.isString  => e.asString.get
}

given Conversion[Attachment, XAttachment] with {
  def apply(attachment: Attachment): XAttachment = {

    val hack: Map[String, ?] = attachment.data.toMap.mapValues(json2Map).toMap
    val hack2 = Map[String, Any]("jws" -> null, "hash" -> null, "json" -> hack.asJava) // OMG

    val id = attachment.id
    val data = XAttachment.Data.Companion.parse(hack2.asJava)
    println("%%" * 120)
    new XAttachment.Builder(id, data).build()
  }
}

// TODO move you another module
given Conversion[PackSignedResult, SignedMesage] with {
  def apply(msg: PackSignedResult): SignedMesage =
    SignedMesage(msg)
}

// TODO move you another module
given Conversion[UnpackResult, UnpackMesage] with {
  def apply(msg: UnpackResult): UnpackMesage =
    UnpackMesage(msg)
}

// given Conversion[UnpackMesage, UnpackResult] with {
//   def apply(msg: UnpackMesage): UnpackResult = msg.msg
// }
