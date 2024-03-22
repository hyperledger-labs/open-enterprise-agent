package io.iohk.atala.iam.oidc.domain

import io.iohk.atala.castor.core.model.did.CanonicalPrismDID
import io.iohk.atala.castor.core.model.did.DID

import java.util.UUID

case class IssuanceSession(
    id: UUID,
    nonce: String,
    issuerState: String,
    schemaId: Option[String],
    claims: zio.json.ast.Json,
    subjectDid: Option[DID],
    issuingDid: CanonicalPrismDID,
)
