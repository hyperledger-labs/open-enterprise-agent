package io.iohk.atala.iam.oidc.http

import sttp.tapir.Schema
import sttp.tapir.json.zio.schemaForZioJsonValue
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class CredentialOfferRequest(
    credentialConfigurationId: Option[String], // TODO: this field should be requried
    claims: zio.json.ast.Json,
)

object CredentialOfferRequest {
  given schema: Schema[CredentialOfferRequest] = Schema.derived
  given encoder: JsonEncoder[CredentialOfferRequest] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialOfferRequest] = DeriveJsonDecoder.gen
}

case class CredentialOfferResponse(credentialOffer: String)

object CredentialOfferResponse {
  given schema: Schema[CredentialOfferResponse] = Schema.derived
  given encoder: JsonEncoder[CredentialOfferResponse] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialOfferResponse] = DeriveJsonDecoder.gen
}
