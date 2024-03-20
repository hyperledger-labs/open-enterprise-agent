package steps.credentials

import interactions.*
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.automation.utils.Wait
import io.iohk.atala.prism.models.IssueCredentialRecord
import models.JwtCredential
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import org.apache.http.HttpStatus
import kotlin.time.Duration.Companion.seconds

class RevokeCredentialSteps {
    @When("{actor} revokes the credential issued to {actor}")
    fun issuerRevokesCredentialsIssuedToHolder(issuer: Actor, holder: Actor) {
        val issuedCredential = issuer.recall<IssueCredentialRecord>("issuedCredential")
        val jwtCred = JwtCredential(issuedCredential.credential!!)
        val statusListId = jwtCred.statusListId()
        issuer.remember("statusListId", statusListId)

        issuer.attemptsTo(
            Get.resource("/credential-status/${statusListId}")
        )
        val encodedList = SerenityRest.lastResponse().jsonPath().get<String>("credentialSubject.encodedList")
        issuer.remember("encodedStatusList", encodedList)

        issuer.attemptsTo(
            Patch.to("/credential-status/revoke-credential/${issuedCredential.recordId}"),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_OK)
        )
    }

    @When("{actor} tries to revoke credential from {actor}")
    fun holderTriesToRevokeCredentialFromIssuer(holder: Actor, issuer: Actor) {
        val issuedCredential = issuer.recall<IssueCredentialRecord>("issuedCredential")
        val receivedCredential = holder.recall<IssueCredentialRecord>("issuedCredential")
        holder.attemptsTo(
            Patch.to("/credential-status/revoke-credential/${issuedCredential.recordId}"),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_NOT_FOUND)
        )
        holder.attemptsTo(
            Patch.to("/credential-status/revoke-credential/${receivedCredential.recordId}"),
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_NOT_FOUND)
        )
    }

    @Then("{actor} should see the credential was revoked")
    fun credentialShouldBeRevoked(issuer: Actor) {
        Wait.until(
            timeout = 30.seconds,
            errorMessage = "Encoded Status List didn't change after revoking."
        ) {
            val statusListId = issuer.recall<String>("statusListId")
            val encodedList = issuer.recall<String>("statusEncodedList")
            issuer.attemptsTo(
                Get.resource("/credential-status/$statusListId")
            )
            val actualEncodedList = SerenityRest.lastResponse().jsonPath().get<String>("credentialSubject.encodedList")
            actualEncodedList != encodedList
        }
    }

    @Then("{actor} should see the credential is not revoked")
    fun issuerShouldSeeTheCredentialIsNotRevoked(issuer: Actor) {
        val issuedCredential = issuer.recall<IssueCredentialRecord>("issuedCredential")
        val jwtCred = JwtCredential(issuedCredential.credential!!)
        val statusListId = jwtCred.statusListId()
        issuer.remember("statusListId", statusListId)

        issuer.attemptsTo(
            Get.resource("/credential-status/${statusListId}")
        )
    }
}
