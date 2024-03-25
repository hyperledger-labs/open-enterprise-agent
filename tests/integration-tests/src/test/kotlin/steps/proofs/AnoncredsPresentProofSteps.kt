package steps.proofs

import abilities.ListenToEvents
import common.Utils.wait
import interactions.Patch
import interactions.Post
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.prism.models.*
import models.PresentationEvent
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.rest.abilities.CallAnApi
import org.apache.http.HttpStatus.SC_CREATED

class AnoncredsPresentProofSteps {

    private var proofEvent: PresentationEvent? = null

    @When("{actor} sends a anoncreds request for proof presentation to {actor} using credential definition issued by {actor}")
    fun faberSendsAnAnoncredsRequestForProofPresentationToBob(faber: Actor, bob: Actor, issuer: Actor) {
        val credentialDefinitionRegistryUrl =
            issuer.usingAbilityTo(CallAnApi::class.java)
                .resolve("/credential-definition-registry/definitions")
                .replace("localhost", "host.docker.internal")
        val credentialDefinitionId =
            "$credentialDefinitionRegistryUrl/${issuer.recall<CredentialDefinitionResponse>("anoncredsCredentialDefinition").guid}/definition"

        val anoncredsPresentationRequestV1 = AnoncredPresentationRequestV1(
            requestedAttributes = mapOf(
                "sex" to
                    AnoncredRequestedAttributeV1(
                        name = "sex",
                        restrictions = listOf(
                            mapOf(
                                ("attr::sex::value" to "M"),
                                ("cred_def_id" to credentialDefinitionId)
                            )
                        )
                    )
            ),
            requestedPredicates = mapOf(
                "age" to AnoncredRequestedPredicateV1(
                    name = "age",
                    pType = ">=",
                    pValue = 18,
                    restrictions = emptyList()
                )
            ),
            name = "proof_req_1",
            nonce = "1103253414365527824079144",
            version = "0.1"
        )
        val presentationRequest = RequestPresentationInput(
            connectionId = faber.recall<Connection>("connection-with-${bob.name}").connectionId,
            credentialFormat = "AnonCreds",
            anoncredPresentationRequest = anoncredsPresentationRequestV1,
            proofs = emptyList()
        )
        faber.attemptsTo(
            Post.to("/present-proof/presentations")
                .with {
                    it.body(
                        presentationRequest
                    )
                }
        )
        faber.attemptsTo(
            Ensure.thatTheLastResponse().apply { println(this.contentType()) }.statusCode().isEqualTo(SC_CREATED)
        )
        val presentationStatus = SerenityRest.lastResponse().get<PresentationStatus>()
        faber.remember("thid", presentationStatus.thid)
        bob.remember("thid", presentationStatus.thid)
    }

    @When("{actor} receives the anoncreds request")
    fun bobReceivesTheAnoncredsRequest(bob: Actor) {
        wait(
            {
                proofEvent = ListenToEvents.`as`(bob).presentationEvents.lastOrNull {
                    it.data.thid == bob.recall<String>("thid")
                }
                proofEvent != null &&
                    proofEvent!!.data.status == PresentationStatus.Status.REQUEST_RECEIVED
            },
            "ERROR: Bob did not achieve any presentation request!"
        )
        bob.remember("presentationId", proofEvent!!.data.presentationId)
    }

    @When("{actor} accepts the anoncreds presentation request from {actor}")
    fun bobAcceptsTheAnoncredsPresentationWithProof(bob: Actor, faber: Actor) {
        val requestPresentationAction = RequestPresentationAction(
            anoncredPresentationRequest =
            AnoncredCredentialProofsV1(
                listOf(
                    AnoncredCredentialProofV1(
                        bob.recall<IssueCredentialRecord>("issuedCredential").recordId,
                        listOf("sex"),
                        listOf("age")
                    )
                )
            ),
            action = RequestPresentationAction.Action.REQUEST_MINUS_ACCEPT
        )

        bob.attemptsTo(
            Patch.to("/present-proof/presentations/${bob.recall<String>("presentationId")}").with {
                it.body(
                    requestPresentationAction
                )
            }
        )
    }
}
