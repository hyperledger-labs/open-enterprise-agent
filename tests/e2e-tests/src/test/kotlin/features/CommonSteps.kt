package features

import api_models.Connection
import api_models.ConnectionState
import api_models.Credential
import common.Environments
import common.ListenToEvents
import common.Utils.lastResponseList
import features.connection.ConnectionSteps
import features.did.PublishDidSteps
import features.issue_credentials.IssueCredentialsSteps
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.ParameterType
import io.cucumber.java.en.Given
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.actors.Cast
import net.serenitybdd.screenplay.actors.OnStage
import net.serenitybdd.screenplay.rest.abilities.CallAnApi
import net.serenitybdd.screenplay.rest.questions.ResponseConsequence
import org.apache.http.HttpStatus.SC_OK
import interactions.Get

class CommonSteps {

    @Before
    fun setStage() {
        val cast = Cast()
        cast.actorNamed("Acme", CallAnApi.at(Environments.ACME_AGENT_URL), ListenToEvents.at(Environments.ACME_AGENT_WEBHOOK_HOST, Environments.ACME_AGENT_WEBHOOK_PORT))
        cast.actorNamed("Bob", CallAnApi.at(Environments.BOB_AGENT_URL), ListenToEvents.at(Environments.BOB_AGENT_WEBHOOK_HOST, Environments.BOB_AGENT_WEBHOOK_PORT))
        cast.actorNamed("Faber", CallAnApi.at(Environments.FABER_AGENT_URL), ListenToEvents.at(Environments.FABER_AGENT_WEBHOOK_HOST, Environments.FABER_AGENT_WEBHOOK_PORT))
        cast.actors.forEach { actor ->
            when(actor.name) {
                "Acme" -> {
                    actor.remember("AUTH_KEY", Environments.ACME_AUTH_KEY)
                }
                "Bob" -> {
                    actor.remember("AUTH_KEY", Environments.BOB_AUTH_KEY)
                }
                "Faber" -> {
                    actor.remember("AUTH_KEY", Environments.FABER_AUTH_KEY)
                }
            }
        }
        OnStage.setTheStage(cast)
    }

    @After
    fun clearStage() {
        OnStage.drawTheCurtain()
    }

    @ParameterType(".*")
    fun actor(actorName: String): Actor {
        return OnStage.theActorCalled(actorName)
    }

    @Given("{actor} has an issued credential from {actor}")
    fun holderHasIssuedCredentialFromIssuer(holder: Actor, issuer: Actor) {
        holder.attemptsTo(
            Get.resource("/issue-credentials/records"),
        )
        holder.should(
            ResponseConsequence.seeThatResponse {
                it.statusCode(SC_OK)
            },
        )
        val receivedCredential = lastResponseList("contents", Credential::class).findLast { credential ->
            credential.protocolState == "CredentialReceived"
        }

        if (receivedCredential != null) {
            holder.remember("issuedCredential", receivedCredential)
        } else {
            val publishDidSteps = PublishDidSteps()
            val issueSteps = IssueCredentialsSteps()
            actorsHaveExistingConnection(issuer, holder)
            publishDidSteps.createsUnpublishedDid(holder)
            publishDidSteps.createsUnpublishedDid(issuer)
            publishDidSteps.hePublishesDidToLedger(issuer)
            issueSteps.acmeOffersACredential(issuer, holder, "short")
            issueSteps.bobRequestsTheCredential(holder)
            issueSteps.acmeIssuesTheCredential(issuer)
            issueSteps.bobHasTheCredentialIssued(holder)
        }
    }

    @Given("{actor} and {actor} have an existing connection")
    fun actorsHaveExistingConnection(inviter: Actor, invitee: Actor) {
        inviter.attemptsTo(
            Get.resource("/connections"),
        )
        inviter.should(
            ResponseConsequence.seeThatResponse {
                it.statusCode(SC_OK)
            },
        )
        val inviterConnection = lastResponseList("contents", Connection::class).firstOrNull {
            it.label == "Connection with ${invitee.name}" && it.state == ConnectionState.CONNECTION_RESPONSE_SENT
        }

        var inviteeConnection: Connection? = null
        if (inviterConnection != null) {
            invitee.attemptsTo(
                Get.resource("/connections"),
            )
            invitee.should(
                ResponseConsequence.seeThatResponse {
                    it.statusCode(SC_OK)
                },
            )
            inviteeConnection = lastResponseList("contents", Connection::class).firstOrNull {
                it.theirDid == inviterConnection.myDid && it.state == ConnectionState.CONNECTION_RESPONSE_RECEIVED
            }
        }

        if (inviterConnection != null && inviteeConnection != null) {
            inviter.remember("connection-with-${invitee.name}", inviterConnection)
            invitee.remember("connection-with-${inviter.name}", inviteeConnection)
        } else {
            val connectionSteps = ConnectionSteps()
            connectionSteps.inviterGeneratesAConnectionInvitation(inviter, invitee)
            connectionSteps.inviteeReceivesTheConnectionInvitation(invitee, inviter)
            connectionSteps.inviteeSendsAConnectionRequestToInviter(invitee, inviter)
            connectionSteps.inviterReceivesTheConnectionRequest(inviter)
            connectionSteps.inviteeReceivesTheConnectionResponse(invitee)
            connectionSteps.inviterAndInviteeHaveAConnection(inviter, invitee)
        }
    }
}