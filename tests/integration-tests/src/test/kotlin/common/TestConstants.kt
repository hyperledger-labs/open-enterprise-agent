package common

import io.iohk.atala.prism.models.*
import models.JsonSchema
import models.JsonSchemaProperty
import java.time.Duration
import java.util.UUID

object TestConstants {
    val TESTS_CONFIG = System.getProperty("TESTS_CONFIG") ?: "/configs/basic.conf"
    val TEST_VERIFICATION_POLICY = VerificationPolicyInput(
        name = "Trusted Issuer and SchemaID",
        description = "Verification Policy with trusted issuer and schemaId",
        constraints = listOf(
            VerificationPolicyConstraint(
                schemaId = "http://atalaprism.io/schemas/1.0/StudentCredential",
                trustedIssuers = listOf(
                    "did:example:123456789abcdefghi",
                    "did:example:123456789abcdefghj",
                ),
            ),
        ),
    )

    val DID_UPDATE_PUBLISH_MAX_WAIT_5_MIN = Duration.ofSeconds(60L)
    val PRISM_DID_AUTH_KEY = ManagedDIDKeyTemplate("auth-1", Purpose.AUTHENTICATION)
    val PRISM_DID_UPDATE_NEW_AUTH_KEY = ManagedDIDKeyTemplate("auth-2", Purpose.AUTHENTICATION)
    val PRISM_DID_SERVICE_FOR_UPDATE = Service(
        "https://update.com",
        listOf("LinkedDomains"),
        Json("https://update.com/"),
    )
    val PRISM_DID_UPDATE_NEW_SERVICE_URL = "https://bar.foo.com/"
    val PRISM_DID_UPDATE_NEW_SERVICE = Service(
        "https://new.service.com",
        listOf("LinkedDomains"),
        Json("https://new.service.com/"),
    )
    val EVENT_TYPE_CONNECTION_UPDATED = "ConnectionUpdated"
    val EVENT_TYPE_ISSUE_CREDENTIAL_RECORD_UPDATED = "IssueCredentialRecordUpdated"
    val EVENT_TYPE_PRESENTATION_UPDATED = "PresentationUpdated"
    val EVENT_TYPE_DID_STATUS_UPDATED = "DIDStatusUpdated"
    val WRONG_SEED = "wrong seed"
}
