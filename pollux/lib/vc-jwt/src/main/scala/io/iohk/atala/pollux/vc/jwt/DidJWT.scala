package io.iohk.atala.pollux.vc.jwt

import com.nimbusds.jose.{JOSEObjectType, JWSAlgorithm, JWSHeader}
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.jwk.{Curve, ECKey}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import io.circe
import io.circe.*
import pdi.jwt.algorithms.JwtECDSAAlgorithm
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import java.security.*

opaque type JWT = String

object JWT {
  def apply(value: String): JWT = value

  extension (jwt: JWT) {
    def value: String = jwt
  }
}

trait Signer {
  def encode(claim: Json): JWT
}

class ES256Signer(privateKey: PrivateKey) extends Signer {
  val algorithm: JwtECDSAAlgorithm = JwtAlgorithm.ES256

  override def encode(claim: Json): JWT = JWT(JwtCirce.encode(claim, privateKey, algorithm))
}

// works with java 7, 8, 11 & bouncycastle provider
// https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support#alg-support-table
class ES256KSigner(privateKey: PrivateKey) extends Signer {
  lazy val signer: ECDSASigner = {
    val ecdsaSigner = ECDSASigner(privateKey, Curve.SECP256K1)
    val bouncyCastleProvider = BouncyCastleProviderSingleton.getInstance
    ecdsaSigner.getJCAContext.setProvider(bouncyCastleProvider)
    ecdsaSigner
  }
  override def encode(claim: Json): JWT = {
    val claimSet = JWTClaimsSet.parse(claim.noSpaces)
    val signedJwt = SignedJWT(
      new JWSHeader.Builder(JWSAlgorithm.ES256K).`type`(JOSEObjectType.JWT).build(),
      claimSet
    )
    signedJwt.sign(signer)
    JWT(signedJwt.serialize())
  }
}

def toJWKFormat(holderJwk: ECKey): JsonWebKey = {
  JsonWebKey(
    kty = "EC",
    crv = Some(holderJwk.getCurve.getName),
    x = Some(holderJwk.getX.toJSONString),
    y = Some(holderJwk.getY.toJSONString),
    d = Some(holderJwk.getD.toJSONString)
  )
}
