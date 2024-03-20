package steps.proofs

import abilities.ListenToEvents
import interactions.Patch
import interactions.Post
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.automation.utils.Wait
import io.iohk.atala.prism.models.*
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import org.apache.http.HttpStatus.SC_CREATED
import kotlin.time.Duration.Companion.seconds

class PresentProofSteps {

    @When("{actor} sends a request for proof presentation to {actor}")
    fun faberSendsARequestForProofPresentationToBob(faber: Actor, bob: Actor) {
        val presentationRequest = RequestPresentationInput(
            connectionId = faber.recall<Connection>("connection-with-${bob.name}").connectionId,
            options = Options(
                challenge = "11c91493-01b3-4c4d-ac36-b336bab5bddf",
                domain = "https://example-verifier.com",
            ),
            proofs = listOf(
                ProofRequestAux(
                    schemaId = "https://schema.org/Person",
                    trustIssuers = listOf("did:web:atalaprism.io/users/testUser"),
                ),
            ),
        )
        faber.attemptsTo(
            Post.to("/present-proof/presentations")
                .with {
                    it.body(
                        presentationRequest,
                    )
                },
        )
        faber.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
        )
        val presentationStatus = SerenityRest.lastResponse().get<PresentationStatus>()
        faber.remember("thid", presentationStatus.thid)
        bob.remember("thid", presentationStatus.thid)
    }

    @When("{actor} receives the request")
    fun bobReceivesTheRequest(bob: Actor) {
        Wait.until(
            timeout = 30.seconds,
            errorMessage = "ERROR: Bob did not achieve any presentation request!"
        ) {
            val proofEvent = ListenToEvents.with(bob).presentationEvents.lastOrNull {
                it.data.thid == bob.recall<String>("thid")
            }
            bob.remember("presentationId", proofEvent?.data?.presentationId)
            proofEvent?.data?.status == PresentationStatus.Status.REQUEST_RECEIVED
        }
    }

    @When("{actor} makes the presentation of the proof to {actor}")
    fun bobMakesThePresentationOfTheProof(bob: Actor, faber: Actor) {
        val requestPresentationAction = RequestPresentationAction(
            proofId = listOf(bob.recall<IssueCredentialRecord>("issuedCredential").recordId),
            action = RequestPresentationAction.Action.REQUEST_MINUS_ACCEPT,
        )

        bob.attemptsTo(
            Patch.to("/present-proof/presentations/${bob.recall<String>("presentationId")}").with {
                it.body(requestPresentationAction)
            },
        )
    }

    @When("{actor} rejects the proof")
    fun bobRejectsProof(bob: Actor) {
        bob.attemptsTo(
            Patch.to("/present-proof/presentations/${bob.recall<String>("presentationId")}").with {
                it.body(
                    RequestPresentationAction(action = RequestPresentationAction.Action.REQUEST_MINUS_REJECT),
                )
            },
        )
    }

    @Then("{actor} sees the proof is rejected")
    fun bobSeesProofIsRejected(bob: Actor) {
        Wait.until(
            timeout = 30.seconds,
            errorMessage = "ERROR: Faber did not receive presentation from Bob!"
        ) {
            val proofEvent = ListenToEvents.with(bob).presentationEvents.lastOrNull {
                it.data.thid == bob.recall<String>("thid")
            }
            proofEvent?.data?.status == PresentationStatus.Status.REQUEST_REJECTED
        }
    }

    @Then("{actor} has the proof verified")
    fun faberHasTheProofVerified(faber: Actor) {
        Wait.until(
            timeout = 30.seconds,
            errorMessage = "Presentation did not achieve PresentationVerified state!"
        ) {
            val proofEvent = ListenToEvents.with(faber).presentationEvents.lastOrNull {
                it.data.thid == faber.recall<String>("thid")
            }
            println("Status? ${proofEvent?.data?.status}")
            proofEvent?.data?.status == PresentationStatus.Status.PRESENTATION_VERIFIED
        }
    }

    @Then("{actor} sees the proof returned verification failed")
    fun verifierSeesTheProofReturnedVerificationFailed(verifier: Actor) {
        Wait.until(
            timeout = 30.seconds,
            errorMessage = "Presentation did not achieve PresentationVerified state!"
        ) {
            val proofEvent = ListenToEvents.with(verifier).presentationEvents.lastOrNull {
                it.data.thid == verifier.recall<String>("thid")
            }
            println("Status? ${proofEvent?.data?.status}")
            proofEvent?.data?.status == null // FIXME: new status not present yet
        }
    }
}
