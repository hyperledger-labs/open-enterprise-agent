package io.iohk.atala.pollux.core.service.verification

import io.iohk.atala.pollux.core.model.schema.CredentialSchema
import io.iohk.atala.pollux.core.service.URIDereferencer
import io.iohk.atala.pollux.vc.jwt.{DidResolver, JWT, JwtCredential}
import zio.{IO, *}

class VcVerificationServiceImpl(didResolver: DidResolver, uriDereferencer: URIDereferencer)
    extends VcVerificationService {
  override def verify(
      vcVerificationRequests: List[VcVerificationRequest]
  ): IO[VcVerificationServiceError, List[VcVerificationResult]] = {
    vcVerificationRequests.map(vcVerificationRequest =>
      val verificationOutcomesZIO = ZIO.collectAll(
        vcVerificationRequest.verifications
          .map(verification => verify(vcVerificationRequest.credential, verification))
      )

      verificationOutcomesZIO.map(verificationOutcomes => {
        val successfulChecks = verificationOutcomes.filter(_.success).map(_.verification)

        val failedVerifications = verificationOutcomes.filterNot(_.success).map(_.verification)

        val failedAsErrorChecks =
          failedVerifications.filter(verification => verification.failureType == VcVerificationFailureType.ERROR)

        val failedAsWarningChecks =
          failedVerifications.filter(verification => verification.failureType == VcVerificationFailureType.WARN)

        VcVerificationResult(
          credential = vcVerificationRequest.credential,
          checks = vcVerificationRequest.verifications,
          successfulChecks = successfulChecks,
          failedChecks = failedAsErrorChecks,
          failedAsWarningChecks = failedAsWarningChecks
        )
      })
    )
    ZIO.succeed(List.empty)
  }

  private def verify(
      credential: String,
      verification: VcVerification
  ): IO[VcVerificationServiceError, VcVerificationOutcome] = {
    verification match {
      case VcVerification.SchemaCheck           => verifySchema(credential)
      case VcVerification.SignatureVerification => verifySignature(credential)
      case VcVerification.ExpirationCheck       => verifyExpiration(credential)
      case VcVerification.NotBeforeCheck        => verifyNotBefore(credential)
      case _ => ZIO.fail(VcVerificationServiceError.UnexpectedError(s"Unsupported Verification $verification"))
    }
  }

  private def verifySchema(credential: String): IO[VcVerificationServiceError, VcVerificationOutcome] = {
    val result =
      for {
        decodedJwt <-
          JwtCredential
            .decodeJwt(JWT(credential))
            .mapError(error => VcVerificationServiceError.UnexpectedError(s"Unable decode JWT: $error"))
        credentialSchema <-
          ZIO
            .fromOption(decodedJwt.maybeCredentialSchema)
            .mapError(error => VcVerificationServiceError.UnexpectedError(s"Missing Credential Schema: $error"))
      } yield CredentialSchema
        .validateJWTClaims(
          credentialSchema.id,
          decodedJwt.credentialSubject.noSpaces,
          uriDereferencer
        )

    result.map(validation =>
      validation
        .as(
          VcVerificationOutcome(
            verification = VcVerification.SchemaCheck,
            success = true
          )
        )
        .catchAll(_ =>
          ZIO.succeed(
            VcVerificationOutcome(
              verification = VcVerification.SchemaCheck,
              success = false
            )
          )
        )
    )

    ZIO.succeed(VcVerificationOutcome(verification = VcVerification.SchemaCheck, success = true))
  }

  private def verifySignature(credential: String): IO[VcVerificationServiceError, VcVerificationOutcome] = {
    JwtCredential
      .validateEncodedJWT(JWT(credential))(didResolver)
      .mapError(error => VcVerificationServiceError.UnexpectedError(error))
      .map(validation =>
        VcVerificationOutcome(
          verification = VcVerification.SchemaCheck,
          success = validation
            .map(_ => true)
            .getOrElse(false)
        )
      )
  }

  private def verifyExpiration(credential: String): IO[VcVerificationServiceError, VcVerificationOutcome] = {
    ZIO.succeed(
      VcVerificationOutcome(
        verification = VcVerification.SchemaCheck,
        success = JwtCredential
          .validateExpiration(JWT(credential))
          .map(_ => true)
          .getOrElse(false)
      )
    )
  }

  private def verifyNotBefore(credential: String): IO[VcVerificationServiceError, VcVerificationOutcome] = {
    ZIO.succeed(
      VcVerificationOutcome(
        verification = VcVerification.SchemaCheck,
        success = JwtCredential
          .validateNotBefore(JWT(credential))
          .map(_ => true)
          .getOrElse(false)
      )
    )
  }
}

object VcVerificationServiceImpl {
  val layer: URLayer[DidResolver & URIDereferencer, VcVerificationService] =
    ZLayer.fromFunction(VcVerificationServiceImpl(_, _))
}
