package io.iohk.atala.pollux.core.service.verification

import zio.*

trait VcVerificationService {
  def verify(request: List[VcVerificationRequest]): IO[VcVerificationServiceError, List[VcVerificationResult]]
}

final case class VcVerificationRequest(
    credential: String,
    verifications: List[VcVerification]
)

final case class VcVerificationResult(
    credential: String,
    checks: List[VcVerification],
    successfulChecks: List[VcVerification],
    failedChecks: List[VcVerification],
    failedAsWarningChecks: List[VcVerification]
)

final case class VcVerificationOutcome(verification: VcVerification, success: Boolean)
