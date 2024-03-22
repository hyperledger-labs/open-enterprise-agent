package io.iohk.atala.iam.oidc.http

import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.encodedName
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class IssuerMetadata(
    credential_issuer: String,
    authorization_servers: Option[Seq[String]],
    credential_endpoint: String
)

object IssuerMetadata {
  given schema: Schema[IssuerMetadata] = Schema.derived
  given encoder: JsonEncoder[IssuerMetadata] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[IssuerMetadata] = DeriveJsonDecoder.gen
}
