package io.iohk.atala.pollux.vc.jwt

import io.iohk.atala.castor.core.model.did.w3c.{
  DIDDocumentRepr,
  DIDResolutionErrorRepr,
  PublicKeyJwk,
  PublicKeyRepr,
  PublicKeyReprOrRef,
  ServiceRepr,
  makeW3CResolver
}
import io.iohk.atala.castor.core.service.DIDService
import zio.*

import java.time.Instant
import scala.annotation.unused
import io.circe.Json

trait DidResolver {
  def resolve(didUrl: String): UIO[DIDResolutionResult]
}

trait DIDResolutionResult

sealed case class DIDResolutionFailed(
    error: DIDResolutionError
) extends DIDResolutionResult

sealed case class DIDResolutionSucceeded(
    didDocument: DIDDocument,
    didDocumentMetadata: DIDDocumentMetadata
) extends DIDResolutionResult

sealed trait DIDResolutionError(@unused error: String, @unused message: String)
case class InvalidDid(message: String) extends DIDResolutionError("invalidDid", message)
case class NotFound(message: String) extends DIDResolutionError("notFound", message)
case class RepresentationNotSupported(message: String) extends DIDResolutionError("RepresentationNotSupported", message)
case class InvalidPublicKeyLength(message: String) extends DIDResolutionError("invalidPublicKeyLength", message)
case class InvalidPublicKeyType(message: String) extends DIDResolutionError("invalidPublicKeyType", message)
case class UnsupportedPublicKeyType(message: String) extends DIDResolutionError("unsupportedPublicKeyType", message)
case class Error(error: String, message: String) extends DIDResolutionError(error, message)

case class DIDDocumentMetadata(
    created: Option[Instant] = Option.empty,
    updated: Option[Instant] = Option.empty,
    deactivated: Option[Boolean] = Option.empty,
    versionId: Option[Instant] = Option.empty,
    nextUpdate: Option[Instant] = Option.empty,
    nextVersionId: Option[Instant] = Option.empty,
    equivalentId: Option[Instant] = Option.empty,
    canonicalId: Option[Instant] = Option.empty
)

case class DIDDocument(
    id: String,
    alsoKnowAs: Vector[String],
    controller: Vector[String],
    verificationMethod: Vector[VerificationMethod] = Vector.empty,
    authentication: Vector[VerificationMethodOrRef] = Vector.empty,
    assertionMethod: Vector[VerificationMethodOrRef] = Vector.empty,
    keyAgreement: Vector[VerificationMethodOrRef] = Vector.empty,
    capabilityInvocation: Vector[VerificationMethodOrRef] = Vector.empty,
    capabilityDelegation: Vector[VerificationMethodOrRef] = Vector.empty,
    service: Vector[Service] = Vector.empty
)

type VerificationMethodOrRef = VerificationMethod | String

case class VerificationMethod(
    id: String,
    `type`: String,
    controller: String,
    publicKeyBase58: Option[String] = Option.empty,
    publicKeyBase64: Option[String] = Option.empty,
    publicKeyJwk: Option[JsonWebKey] = Option.empty,
    publicKeyHex: Option[String] = Option.empty,
    publicKeyMultibase: Option[String] = Option.empty,
    blockchainAccountId: Option[String] = Option.empty,
    ethereumAddress: Option[String] = Option.empty
)
case class JsonWebKey(
    alg: Option[String] = Option.empty,
    crv: Option[String] = Option.empty,
    e: Option[String] = Option.empty,
    d: Option[String] = Option.empty,
    ext: Option[Boolean] = Option.empty,
    key_ops: Vector[String] = Vector.empty,
    kid: Option[String] = Option.empty,
    kty: String,
    n: Option[String] = Option.empty,
    use: Option[String] = Option.empty,
    x: Option[String] = Option.empty,
    y: Option[String] = Option.empty
)
case class Service(id: String, `type`: String | Seq[String], serviceEndpoint: Json)

/** An adapter for translating Castor resolver to resolver defined in JWT library */
class PrismDidResolver(didService: DIDService) extends DidResolver {

  private val w3cResolver = makeW3CResolver(didService)

  override def resolve(didUrl: String): UIO[DIDResolutionResult] = {
    w3cResolver(didUrl)
      .fold(
        toPolluxResolutionErrorModel,
        { case (didDocumentMetadata, didDocument) =>
          DIDResolutionSucceeded(
            didDocument = toPolluxDIDDocumentModel(didDocument),
            didDocumentMetadata = DIDDocumentMetadata(
              deactivated = Some(didDocumentMetadata.deactivated)
            )
          )
        }
      )
  }

  private def toPolluxDIDDocumentModel(didDocument: DIDDocumentRepr): DIDDocument = {
    DIDDocument(
      id = didDocument.id,
      alsoKnowAs = Vector.empty,
      controller = Vector(didDocument.controller),
      verificationMethod = didDocument.verificationMethod.map(toPolluxVerificationMethodModel).toVector,
      authentication = didDocument.authentication.map(toPolluxVerificationMethodOrRefModel).toVector,
      assertionMethod = didDocument.assertionMethod.map(toPolluxVerificationMethodOrRefModel).toVector,
      keyAgreement = didDocument.keyAgreement.map(toPolluxVerificationMethodOrRefModel).toVector,
      capabilityInvocation = didDocument.capabilityInvocation.map(toPolluxVerificationMethodOrRefModel).toVector,
      capabilityDelegation = didDocument.capabilityDelegation.map(toPolluxVerificationMethodOrRefModel).toVector,
      service = didDocument.service.map(toPolluxServiceModel).toVector
    )
  }

  private def toPolluxResolutionErrorModel(error: DIDResolutionErrorRepr): DIDResolutionFailed = {
    val e = error match {
      case DIDResolutionErrorRepr.InvalidDID(_)              => InvalidDid(error.value)
      case DIDResolutionErrorRepr.InvalidDIDUrl(_)           => InvalidDid(error.value)
      case DIDResolutionErrorRepr.NotFound                   => NotFound(error.value)
      case DIDResolutionErrorRepr.RepresentationNotSupported => RepresentationNotSupported(error.value)
      case DIDResolutionErrorRepr.InternalError(_)           => Error(error.value, error.value)
      case DIDResolutionErrorRepr.InvalidPublicKeyLength     => InvalidPublicKeyLength(error.value)
      case DIDResolutionErrorRepr.InvalidPublicKeyType       => InvalidPublicKeyType(error.value)
      case DIDResolutionErrorRepr.UnsupportedPublicKeyType   => UnsupportedPublicKeyType(error.value)
    }
    DIDResolutionFailed(e)
  }

  private def toPolluxServiceModel(service: ServiceRepr): Service = {
    Service(
      id = service.id,
      `type` = service.`type`,
      serviceEndpoint = service.serviceEndpoint
    )
  }

  private def toPolluxVerificationMethodModel(verificationMethod: PublicKeyRepr): VerificationMethod = {
    VerificationMethod(
      id = verificationMethod.id,
      `type` = verificationMethod.`type`,
      controller = verificationMethod.controller,
      publicKeyJwk = Some(toPolluxJwtModel(verificationMethod.publicKeyJwk))
    )
  }

  private def toPolluxVerificationMethodOrRefModel(verificationMethod: PublicKeyReprOrRef): VerificationMethodOrRef = {
    verificationMethod match {
      case uri: String       => uri
      case pk: PublicKeyRepr => toPolluxVerificationMethodModel(pk)
    }
  }

  private def toPolluxJwtModel(jwk: PublicKeyJwk): JsonWebKey = {
    JsonWebKey(
      crv = Some(jwk.crv),
      kty = jwk.kty,
      x = jwk.x,
      y = jwk.y
    )
  }

}

object PrismDidResolver {
  val layer: URLayer[DIDService, DidResolver] = ZLayer.fromFunction(PrismDidResolver(_))
}
