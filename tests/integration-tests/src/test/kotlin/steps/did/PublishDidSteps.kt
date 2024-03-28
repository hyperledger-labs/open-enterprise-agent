package steps.did

import abilities.ListenToEvents
import common.TestConstants
import common.Utils.wait
import interactions.Get
import interactions.Post
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.prism.models.*
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import org.apache.http.HttpStatus
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_OK

class PublishDidSteps {

    @Given("{actor} have published PRISM DID")
    fun actorHavePublishedPrismDid(actor: Actor) {
        actor.attemptsTo(
            Get.resource("/did-registrar/dids"),
        )
        actor.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
        )
        val publishedDids = SerenityRest.lastResponse().get<ManagedDIDPage>().contents!!.filter {
            // TODO: fix openapi spec to have statuses as enum
            it.status == "PUBLISHED"
        }
        val did = publishedDids.firstOrNull {
            actor.attemptsTo(
                Get.resource("/dids/${it.did}"),
            )
            !SerenityRest.lastResponse().get<DIDResolutionResult>().didDocumentMetadata.deactivated!!
        }
        if (did == null) {
            createsUnpublishedDid(actor)
            hePublishesDidToLedger(actor)
        } else {
            actor.remember("shortFormDid", did.did)
        }
    }

    @Given("{actor} creates unpublished DID")
    fun createsUnpublishedDid(actor: Actor) {
        val createDidRequest = CreateManagedDidRequest(
            CreateManagedDidRequestDocumentTemplate(
                publicKeys = listOf(
                    ManagedDIDKeyTemplate("auth-1", Purpose.AUTHENTICATION),
                    ManagedDIDKeyTemplate("assertion-1", Purpose.ASSERTION_METHOD),
                ),
                services = listOf(
                    Service("https://foo.bar.com", listOf("LinkedDomains"), Json("https://foo.bar.com/")),
                    Service("https://update.com", listOf("LinkedDomains"), Json("https://update.com/")),
                    Service("https://remove.com", listOf("LinkedDomains"), Json("https://remove.com/")),
                ),
            ),
        )
        actor.attemptsTo(
            Post.to("/did-registrar/dids")
                .with {
                    it.body(createDidRequest)
                },
        )

        val managedDid = SerenityRest.lastResponse().get<ManagedDID>()
        actor.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
            Ensure.that(managedDid.longFormDid!!).isNotEmpty(),
        )

        actor.remember("longFormDid", managedDid.longFormDid)

        actor.attemptsTo(
            Get.resource("/did-registrar/dids/${managedDid.longFormDid}"),
        )
        actor.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
        )
        val did = SerenityRest.lastResponse().get<ManagedDID>()
        actor.remember(
            "shortFormDid",
            did.did,
        )
        actor.forget<String>("hasPublishedDid")
    }

    @Given("{actor} has a published DID")
    fun agentHasAPublishedDID(agent: Actor) {
        if (agent.recallAll().containsKey("hasPublishedDid")) {
            return
        }
        if (!agent.recallAll().containsKey("shortFormDid")
            && !agent.recallAll().containsKey("longFormDid")) {
            createsUnpublishedDid(agent)
        }
        hePublishesDidToLedger(agent)
    }

    @Given("{actor} has an unpublished DID")
    fun agentHasAnUnpublishedDID(agent: Actor) {
        if (agent.recallAll().containsKey("shortFormDid")
            || agent.recallAll().containsKey("longFormDid")) {
            // is not published
            if (!agent.recallAll().containsKey("hasPublishedDid")) {
                return
            }
        }
        createsUnpublishedDid(agent)
    }

    @When("{actor} publishes DID to ledger")
    fun hePublishesDidToLedger(actor: Actor) {
        actor.attemptsTo(
            Post.to("/did-registrar/dids/${actor.recall<String>("shortFormDid")}/publications"),
        )
        val didOperationResponse = SerenityRest.lastResponse().get<DIDOperationResponse>()

        actor.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(HttpStatus.SC_ACCEPTED),
            Ensure.that(didOperationResponse.scheduledOperation.didRef).isNotEmpty(),
            Ensure.that(didOperationResponse.scheduledOperation.id).isNotEmpty(),
        )

        wait(
            {
                val didEvent =
                    ListenToEvents.`as`(actor).didEvents.lastOrNull {
                        it.data.did == actor.recall<String>("shortFormDid")
                    }
                didEvent != null && didEvent.data.status == "PUBLISHED"
            },
            "ERROR: DID was not published to ledger!",
            timeout = TestConstants.DID_UPDATE_PUBLISH_MAX_WAIT_5_MIN,
        )
        actor.attemptsTo(
            Get.resource("/dids/${actor.recall<String>("shortFormDid")}"),
        )

        val didDocument = SerenityRest.lastResponse().get<DIDResolutionResult>().didDocument!!

        actor.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
            Ensure.that(didDocument.id).isEqualTo(actor.recall("shortFormDid")),
        )

        actor.remember("hasPublishedDid", true)
    }

    @Then("{actor} resolves DID document corresponds to W3C standard")
    fun heSeesDidDocumentCorrespondsToW3cStandard(actor: Actor) {
        val didResolutionResult = SerenityRest.lastResponse().get<DIDResolutionResult>()
        val didDocument = didResolutionResult.didDocument!!
        val shortFormDid = actor.recall<String>("shortFormDid")
        actor.attemptsTo(
            Ensure.that(didDocument.id).isEqualTo(shortFormDid),
            Ensure.that(didDocument.authentication!![0])
                .isEqualTo("$shortFormDid#${TestConstants.PRISM_DID_AUTH_KEY.id}"),
            Ensure.that(didDocument.verificationMethod!![0].controller).isEqualTo(shortFormDid),
            Ensure.that(didResolutionResult.didDocumentMetadata.deactivated!!).isFalse(),
        )
    }
}
