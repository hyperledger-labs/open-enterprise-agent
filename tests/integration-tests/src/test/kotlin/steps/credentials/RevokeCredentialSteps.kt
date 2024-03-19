package steps.credentials

import interactions.*
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.utils.Wait
import io.iohk.atala.prism.models.IssueCredentialRecord
import io.restassured.RestAssured
import models.JwtCredential
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import steps.common.CommonSteps
import steps.did.PublishDidSteps

class RevokeCredentialSteps {

    @Given("{actor} has issued a revocable credential to {actor}")
    fun issuerHasIssuedARevocableCredentialToHolder(issuer: Actor, holder: Actor) {
        CommonSteps().holderHasIssuedCredentialFromIssuer(holder, issuer)
        val credential = holder.forget<IssueCredentialRecord>("issuedCredential")
        holder.remember("revocableCredential", credential)
    }

    @When("{actor} revokes the credential from {actor}")
    fun revokesCredential(issuer: Actor, holder: Actor) {
        val revocableCredential = holder.recall<IssueCredentialRecord>("revocableCredential")
        val jwtCred = JwtCredential(revocableCredential.credential!!)
        val statusListUrl = jwtCred.statusListCredential()
        issuer.remember("statusList", statusListUrl)

        val statusList = RestAssured.get(statusListUrl).thenReturn().jsonPath()
        val encodedList = statusList.get<String>("credentialSubject.encodedList")
        issuer.remember("statusListBit", encodedList)


    //        issuer.attemptsTo(
//            Patch.to("/credential-status/revoke-credential/$recordId")
//        )
//        SerenityRest.lastResponse().prettyPrint()

    }

    @Then("{actor} should see the credential was revoked")
    fun credentialShouldBeRevoked(issuer: Actor) {
        Wait.until {
            val recordId = issuer.recall<String>("revokeRecordId")
            val encodedList = issuer.recall<String>("statusEncodedList")

            issuer.attemptsTo(
                Get.resource("/credential-status/$recordId")
            )
            SerenityRest.lastResponse().prettyPrint()

            val actualEncodedList = SerenityRest.lastResponse().jsonPath().get<String>("credentialSubject.encodedList")

            actualEncodedList == encodedList
        }
    }
}
