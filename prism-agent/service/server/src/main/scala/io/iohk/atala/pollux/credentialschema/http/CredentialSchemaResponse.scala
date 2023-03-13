package io.iohk.atala.pollux.credentialschema.http

import io.iohk.atala.pollux.core.model
import io.iohk.atala.pollux.core.model.CredentialSchema.Input
import io.iohk.atala.pollux.credentialschema.http.CredentialSchemaInput
import io.iohk.atala.pollux.credentialschema.http.Proof
import sttp.model.Uri
import sttp.model.Uri.*
import sttp.tapir.EndpointIO.annotations.{example, query}
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.{default, description, encodedExample, encodedName}
import sttp.tapir.json.zio.schemaForZioJsonValue
import zio.json.ast.Json
import zio.json.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import io.iohk.atala.api.http.*

import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID
import CredentialSchemaResponse.annotations

case class CredentialSchemaResponse(
    @description(annotations.guid.description)
    @encodedExample(annotations.guid.example)
    guid: UUID,
    @description(annotations.id.description)
    @encodedExample(annotations.id.example)
    id: String,
    @description(annotations.longId.description)
    @encodedExample(annotations.longId.example)
    longId: Option[String],
    @description(annotations.name.description)
    @encodedExample(annotations.name.example)
    name: String,
    @description(annotations.version.description)
    @encodedExample(annotations.version.example)
    version: String,
    @description(annotations.tags.description)
    @encodedExample(annotations.tags.example)
    tags: Seq[String],
    @description(annotations.description.description)
    @encodedExample(annotations.description.example)
    description: String,
    @description(annotations.`type`.description)
    @encodedExample(annotations.`type`.example)
    `type`: String,
    @description(annotations.schema.description)
    @encodedExample(annotations.schema.example.toJson)
    schema: Json,
    @description(annotations.author.description)
    @encodedExample(annotations.author.example)
    author: String,
    @description(annotations.authored.description)
    @encodedExample(annotations.authored.example)
    authored: OffsetDateTime,
    @description(annotations.proof.description)
    @encodedExample(annotations.proof.example.toJson)
    proof: Option[Proof],
    @description(annotations.kind.description)
    @encodedExample(annotations.kind.example)
    kind: String = "CredentialSchema",
    @description(annotations.self.description)
    @encodedExample(annotations.self.example)
    self: String = ""
) {
  def withBaseUri(base: Uri) = withSelf(base.addPath(guid.toString).toString)
  def withSelf(self: String) = copy(self = self)
}

object CredentialSchemaResponse {

  def fromDomain(cs: model.CredentialSchema): CredentialSchemaResponse =
    CredentialSchemaResponse(
      guid = cs.guid,
      id = cs.id.toString,
      longId = Option(cs.longId),
      name = cs.name,
      version = cs.version,
      tags = cs.tags,
      description = cs.description,
      `type` = cs.`type`,
      schema = cs.schema,
      author = cs.author,
      authored = cs.authored,
      proof = None
    )

  given scala.Conversion[model.CredentialSchema, CredentialSchemaResponse] = fromDomain

  given encoder: zio.json.JsonEncoder[CredentialSchemaResponse] =
    DeriveJsonEncoder.gen[CredentialSchemaResponse]
  given decoder: zio.json.JsonDecoder[CredentialSchemaResponse] =
    DeriveJsonDecoder.gen[CredentialSchemaResponse]
  given schema: Schema[CredentialSchemaResponse] = Schema.derived

  object annotations {
    object guid
        extends Annotation[UUID](
          description = "Globally unique id of the credential schema<br/>" +
            "It's composed from the bytes of the string that contain the `author`, `name`, and `version` values<br/>" +
            "The string format looks like the resource identifier:<br/>" +
            "`author`/`id`?version=`version`",
          example = UUID.fromString("0527aea1-d131-3948-a34d-03af39aba8b4")
        )

    object id
        extends Annotation[UUID](
          description = "A locally unique identifier to address the schema. UUID is generated by the backend.",
          example = UUID.fromString("0527aea1-d131-3948-a34d-03af39aba8b5")
        )
    object schema
        extends Annotation[Json](
          description = "Valid JSON Schema where the Credential Schema data fields are defined. <br/>" +
            "A piece of Metadata",
          example = DrivingLicenseSchemaExample.fromJson[Json].toOption.getOrElse(Json.Null)
        )

    object `type`
        extends Annotation[String](
          description =
            "This field resolves to a JSON schema with details about the schema metadata that applies to the schema. <br/>" +
              "A piece of Metadata.",
          example = "https://w3c-ccg.github.io/vc-json-schemas/schema/2.0/schema.json"
        )
    object longId
        extends Annotation[String](
          description = "Resource id of the credential schema. Contains the `author`'s DID, `id` and `version` fields.",
          example = "did:prism:4a5b5cf0a513e83b598bbea25cd6196746747f361a73ef77068268bc9bd732ff" +
            "/0527aea1-d131-3948-a34d-03af39aba8b4?version=1.0.0"
        )
    object self
        extends Annotation[String](
          description = "The URL that uniquely identifies the resource being returned in the response.",
          example = "/prism-agent/schema-registry/schemas/0527aea1-d131-3948-a34d-03af39aba8b4"
        )
    object kind
        extends Annotation[String](
          description = "A string that identifies the type of resource being returned in the response.",
          example = "CredentialSchema"
        )
    object proof
        extends Annotation[Proof](
          description = "A digital signature over the Credential Schema for the sake of asserting authorship. <br/>" +
            "A piece of Metadata.",
          example = Proof.Example
        )

    object name
        extends Annotation[String](
          description = "A human-readable name for the credential schema. A piece of Metadata.",
          example = "DrivingLicense"
        )

    object version
        extends Annotation[String](
          description = "Denotes the revision of a given Credential Schema. <br/>" +
            "It should follow semantic version convention to describe the impact of the schema evolution",
          example = "1.0.0"
        )
    object tags
        extends Annotation[Seq[String]](
          description = "Tokens that allow to lookup and filter the credential schema records.",
          example = Seq("driving", "licence", "id")
        )

    object description
        extends Annotation[String](
          description = "A human-readable description of the credential schema",
          example = "Simple credential schema for the driving licence verifiable credential."
        )

    object author
        extends Annotation[String](
          description = "DID of the identity which authored the credential schema. <br/>" +
            "A piece of Metadata.",
          example = "did:prism:4a5b5cf0a513e83b598bbea25cd6196746747f361a73ef77068268bc9bd732ff"
        )

    object authored
        extends Annotation[OffsetDateTime](
          description =
            "[RFC3339](https://www.rfc-editor.org/rfc/rfc3339) date on which the credential schema was created. <br/>" +
              "A piece of Metadata.",
          example = OffsetDateTime.parse("2022-03-10T12:00:00Z")
        )

    val DrivingLicenseSchemaExample =
      """{
          |  "$id": "driving-license-1.0",
          |  "$schema": "https://json-schema.org/draft/2020-12/schema",
          |  "description": "Driving License",
          |  "type": "object",
          |  "properties": {
          |    "credentialSubject": {
          |      "type": "object",
          |      "properties": {
          |        "emailAddress": {
          |          "type": "string",
          |          "format": "email"
          |        },
          |        "givenName": {
          |           "type": "string"
          |        },
          |        "familyName": {
          |           "type": "string"
          |        },
          |        "dateOfIssuance": {
          |           "type": "datetime"
          |        },
          |        "drivingLicenseID": {
          |           "type": "string"
          |        },
          |        "drivingClass": {
          |           "type": "integer"
          |        },
          |        "required": [
          |          "emailAddress",
          |          "familyName",
          |          "dateOfIssuance",
          |          "drivingLicenseID",
          |          "drivingClass"
          |        ],
          |        "additionalProperties": true
          |      }
          |    }
          |  }
          |}""".stripMargin
  }
}
