package io.iohk.atala.iam.oidc.http

import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.encodedName
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, jsonField}

case class NonceResponse(
    @jsonField("c_nonce") @encodedName("c_nonce") nonce: String,
    @jsonField("c_nonce_expires_in") @encodedName("c_nonce_expires_in") nonceExpiresIn: Long
) {
  require(nonce.nonEmpty, "nonce must not be empty")
  require(nonceExpiresIn > 0, "nonceExpiresIn must be greater than 0")
}

object NonceResponse {
  given schema: Schema[NonceResponse] = Schema.derived
  given encoder: JsonEncoder[NonceResponse] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[NonceResponse] = DeriveJsonDecoder.gen
}
