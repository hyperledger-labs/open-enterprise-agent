package org.hyperledger.identus.mercury.protocol.issuecredential

import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Json
import munit.*
import org.hyperledger.identus.mercury.model.{AttachmentDescriptor, DidId}

class IssueCredentialSpec extends ZSuite {

  test("Issuer IssueCredential") {

    val attribute1 = Attribute(name = "name", value = "Joe Blog")
    val attribute2 = Attribute(name = "dob", value = "01/10/1947")
    val credentialPreview = CredentialPreview(attributes = Seq(attribute1, attribute2))
    val body = IssueCredential.Body(goal_code = Some("Issued Credential"))
    val attachmentDescriptor = AttachmentDescriptor.buildJsonAttachment[CredentialPreview](payload = credentialPreview)
    val attachmentDescriptorJson =
    attachmentDescriptor
      .asJson(AttachmentDescriptor.attachmentDescriptorEncoderV2)
      .deepDropNullValues
      .noSpaces

    // FIXME !!! THIS WILL FAIL!
    val expectedProposalJson = parse(
      s"""{
         |  "id": "061bf917-2cbe-460b-8d12-b1a9609505c2",
         |  "type": "https://didcomm.org/issue-credential/3.0/issue-credential",
         |  "body": { "goal_code": "Issued Credential" },
         |  "attachments": [
         |    $attachmentDescriptorJson
         |  ],
         |  "to" : "did:prism:test123",
         |  "from" : "did:prism:test123"
         |}""".stripMargin
    ).getOrElse(Json.Null)

    val issueCredential = IssueCredential(
      id = "061bf917-2cbe-460b-8d12-b1a9609505c2",
      body = body,
      attachments = Seq(attachmentDescriptor),
      to = DidId("did:prism:test123"),
      from = DidId("did:prism:test123")
    )

    val result = issueCredential.asJson.deepDropNullValues
    assertEquals(result, expectedProposalJson)
  }
}
