package io.iohk.atala.iam.oidc.http

import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.encodedName
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, jsonField}

// The idea to have a hierarchy of requests is to be able to use the same endpoint for different types of requests
// This trait contains the common fields for all the requests
sealed trait CredentialRequest {
  // REQUIRED when the credential_identifiers parameter was not returned from the Token Response.
  // It MUST NOT be used otherwise
  // It not an optional in this implementation to help to define the serializer and schema
  val format: CredentialFormat
  // Object containing the proof of possession of the cryptographic key material the issued Credential would be bound to.
  // The proof object is REQUIRED if the proof_types_supported parameter is non-empty and present in the credential_configurations_supported parameter of the Issuer metadata for the requested Credential.
  val proof: Option[Proof]
  // REQUIRED when credential_identifiers parameter was returned from the Token Response. It MUST NOT be used otherwise.
  // It is a String that identifies a Credential that is being requested to be issued.
  val credentialIdentifier: Option[String]
  // OPTIONAL. Object containing information for encrypting the Credential Response.
  // If this request element is not present, the corresponding credential response returned is not encrypted
  val credentialResponseEncryption: Option[CredentialResponseEncryption]
}

object CredentialRequest {
  given schema: Schema[CredentialRequest] = Schema
    .oneOfUsingField[CredentialRequest, CredentialFormat](_.format, _.toString)(
      CredentialFormat.vc_jwt -> JwtCredentialRequest.schema,
      CredentialFormat.sd_jwt -> SdJwtCredentialRequest.schema,
      CredentialFormat.anoncreds -> AnoncredsCredentialRequest.schema
    )
  given encoder: JsonEncoder[CredentialRequest] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialRequest] = DeriveJsonDecoder.gen
}

case class JwtCredentialRequest(
    format: CredentialFormat,
    proof: Option[Proof],
    @jsonField("credential_identifier")
    @encodedName("credential_identifier")
    credentialIdentifier: Option[String],
    @jsonField("credential_response_encryption")
    @encodedName("credential_response_encryption")
    credentialResponseEncryption: Option[CredentialResponseEncryption],
    // REQUIRED when the format parameter is present in the Credential Request.
    // It MUST NOT be used otherwise. It is an object containing the detailed description of the Credential type.
    @jsonField("credential_definition")
    @encodedName("credential_definition")
    credentialDefinition: Option[CredentialDefinition]
) extends CredentialRequest

object JwtCredentialRequest {
  given schema: Schema[JwtCredentialRequest] = Schema.derived

  given encoder: JsonEncoder[JwtCredentialRequest] = DeriveJsonEncoder.gen

  given decoder: JsonDecoder[JwtCredentialRequest] = DeriveJsonDecoder.gen
}

case class SdJwtCredentialRequest(
    format: CredentialFormat,
    proof: Option[Proof],
    @jsonField("credential_identifier")
    @encodedName("credential_identifier")
    credentialIdentifier: Option[String],
    @jsonField("credential_response_encryption")
    @encodedName("credential_response_encryption")
    credentialResponseEncryption: Option[CredentialResponseEncryption],
    // REQUIRED when the format parameter is present in the Credential Request.
    // It MUST NOT be used otherwise. It is an object containing the detailed description of the Credential type.
    @jsonField("credential_definition")
    @encodedName("credential_definition")
    credentialDefinition: Option[CredentialDefinition],
    selectiveDisclosure: String // TODO: it's a fake field for now
) extends CredentialRequest

object SdJwtCredentialRequest {
  given schema: Schema[SdJwtCredentialRequest] = Schema.derived

  given encoder: JsonEncoder[SdJwtCredentialRequest] = DeriveJsonEncoder.gen

  given decoder: JsonDecoder[SdJwtCredentialRequest] = DeriveJsonDecoder.gen
}

case class AnoncredsCredentialRequest(
    format: CredentialFormat,
    proof: Option[Proof],
    @jsonField("credential_identifier")
    @encodedName("credential_identifier")
    credentialIdentifier: Option[String],
    @jsonField("credential_response_encryption")
    @encodedName("credential_response_encryption")
    credentialResponseEncryption: Option[CredentialResponseEncryption],
    // REQUIRED when the format parameter is present in the Credential Request.
    // It MUST NOT be used otherwise. It is an object containing the detailed description of the Credential type.
    @jsonField("credential_definition")
    @encodedName("credential_definition")
    credentialDefinition: Option[CredentialDefinition],
    anoncreds: String // TODO: it's a fake field for now
) extends CredentialRequest

object AnoncredsCredentialRequest {
  given schema: Schema[AnoncredsCredentialRequest] = Schema.derived

  given encoder: JsonEncoder[AnoncredsCredentialRequest] = DeriveJsonEncoder.gen

  given decoder: JsonDecoder[AnoncredsCredentialRequest] = DeriveJsonDecoder.gen
}

case class CredentialResponseEncryption(jwk: String, alg: String, enc: String)
object CredentialResponseEncryption {
  given schema: Schema[CredentialResponseEncryption] = Schema.derived
  given encoder: JsonEncoder[CredentialResponseEncryption] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialResponseEncryption] = DeriveJsonDecoder.gen
}

type CredentialSubject = Map[String, ClaimDescriptor]

case class CredentialDefinition(
    `@context`: Seq[String],
    `type`: Seq[String],
    credentialSubject: Option[CredentialSubject]
)
object CredentialDefinition {
  given schema: Schema[CredentialDefinition] = Schema.derived
  given encoder: JsonEncoder[CredentialDefinition] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialDefinition] = DeriveJsonDecoder.gen
}
case class ClaimDescriptor(
    mandatory: Option[Boolean],
    @jsonField("value_type")
    @encodedName("value_type")
    valueType: Option[String],
    display: Seq[Localization]
)

object ClaimDescriptor {
  given schema: Schema[ClaimDescriptor] = Schema.derived
  given encoder: JsonEncoder[ClaimDescriptor] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[ClaimDescriptor] = DeriveJsonDecoder.gen
}

case class Localization(name: String, locale: String)

object Localization {
  given schema: Schema[Localization] = Schema.derived
  given encoder: JsonEncoder[Localization] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[Localization] = DeriveJsonDecoder.gen
}

sealed trait Proof {
  val proofType: ProofType
}

object Proof {
  given schema: Schema[Proof] = Schema
    .oneOfUsingField[Proof, ProofType](_.proofType, _.toString)(
      ProofType.jwt -> JwtProof.schema,
      ProofType.cwt -> CwtProof.schema,
      ProofType.ldp_vp -> LdpProof.schema
    )
  given encoder: JsonEncoder[Proof] = DeriveJsonEncoder.gen[Proof]
  given decoder: JsonDecoder[Proof] = DeriveJsonDecoder.gen[Proof]
}
case class JwtProof(
    @jsonField("proof_type")
    @encodedName("proof_type")
    proofType: ProofType,
    jwt: String
) extends Proof {
  def this(jwt: String) = this(proofType = ProofType.jwt, jwt)
}

object JwtProof {
  given schema: Schema[JwtProof] = Schema.derived
  given encoder: JsonEncoder[JwtProof] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[JwtProof] = DeriveJsonDecoder.gen
}

case class CwtProof(
    @jsonField("proof_type")
    @encodedName("proof_type")
    proofType: ProofType,
    cwt: String
) extends Proof

object CwtProof {
  given schema: Schema[CwtProof] = Schema.derived
  given encoder: JsonEncoder[CwtProof] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CwtProof] = DeriveJsonDecoder.gen
}

case class LdpProof(
    @jsonField("proof_type")
    @encodedName("proof_type")
    proofType: ProofType,
    vp: String
) extends Proof

object LdpProof {
  given schema: Schema[LdpProof] = Schema.derived
  given encoder: JsonEncoder[LdpProof] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[LdpProof] = DeriveJsonDecoder.gen
}

enum CredentialFormat {
  case vc_jwt
  case sd_jwt
  case anoncreds
}

object CredentialFormat {
  given schema: Schema[CredentialFormat] = Schema.derivedEnumeration.defaultStringBased
  given encoder: JsonEncoder[CredentialFormat] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[CredentialFormat] = DeriveJsonDecoder.gen
}

enum ProofType {
  case jwt
  case cwt
  case ldp_vp
}

object ProofType {
  given schema: Schema[ProofType] = Schema.derivedEnumeration.defaultStringBased
  given encoder: JsonEncoder[ProofType] = DeriveJsonEncoder.gen
  given decoder: JsonDecoder[ProofType] = DeriveJsonDecoder.gen
}
