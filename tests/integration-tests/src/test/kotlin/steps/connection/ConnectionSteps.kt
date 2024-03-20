package steps.connection

import abilities.ListenToEvents
import common.Utils.wait
import interactions.Get
import interactions.Post
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.prism.models.AcceptConnectionInvitationRequest
import io.iohk.atala.prism.models.Connection
import io.iohk.atala.prism.models.CreateConnectionRequest
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_OK
import org.assertj.core.api.Assertions.assertThat

class ConnectionSteps {

    @When("{actor} generates a connection invitation to {actor}")
    fun inviterGeneratesAConnectionInvitation(inviter: Actor, invitee: Actor) {
        // Acme(Issuer) initiates a connection
        // and sends it to Bob(Holder) out-of-band, e.g. using QR-code
        val connectionLabel = "Connection with ${invitee.name}"
        inviter.attemptsTo(
            Post.to("/connections")
                .with {
                    it.body(
                        CreateConnectionRequest(label = connectionLabel),
                    )
                },
        )

        val connection = SerenityRest.lastResponse().get<Connection>()

        inviter.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
            Ensure.that(connection.label!!).isEqualTo(connectionLabel),
            Ensure.that(connection.state).isEqualTo(Connection.State.INVITATION_GENERATED),
            Ensure.that(connection.role).isEqualTo(Connection.Role.INVITER),
        )

        // Acme remembers connection to send it out of band to Bob
        inviter.remember("connection", connection)
    }

    @When("{actor} sends a connection request to {actor}")
    fun inviteeSendsAConnectionRequestToInviter(invitee: Actor, inviter: Actor) {
        // Bob accepts connection using achieved out-of-band invitation
        val inviterConnection = inviter.recall<Connection>("connection")
        invitee.attemptsTo(
            Post.to("/connection-invitations")
                .with {
                    it.body(
                        AcceptConnectionInvitationRequest(
                            inviterConnection.invitation.invitationUrl.split("=")[1],
                        ),
                    )
                },
        )
        val inviteeConnection = SerenityRest.lastResponse().get<Connection>()

        invitee.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
            Ensure.that(inviteeConnection.invitation.from).isEqualTo(inviterConnection.invitation.from),
            Ensure.that(inviteeConnection.invitation.id).isEqualTo(inviterConnection.invitation.id),
            Ensure.that(inviteeConnection.invitation.invitationUrl).isEqualTo(inviterConnection.invitation.invitationUrl),
            Ensure.that(inviteeConnection.invitation.type).isEqualTo(inviterConnection.invitation.type),
            Ensure.that(inviteeConnection.state).isEqualTo(Connection.State.CONNECTION_REQUEST_PENDING),
            Ensure.that(inviteeConnection.role).isEqualTo(Connection.Role.INVITEE),
        )

        invitee.remember("connection", inviteeConnection)
    }

    @When("{actor} receives the connection request and sends back the response")
    fun inviterReceivesTheConnectionRequest(inviter: Actor) {
        wait(
            {
                val lastEvent = ListenToEvents.with(inviter).connectionEvents.lastOrNull {
                    it.data.thid == inviter.recall<Connection>("connection").thid
                }
                lastEvent != null &&
                    lastEvent.data.state == Connection.State.CONNECTION_RESPONSE_SENT
            },
            "Inviter connection didn't reach ${Connection.State.CONNECTION_RESPONSE_SENT} state",
        )
    }

    @When("{actor} receives the connection response")
    fun inviteeReceivesTheConnectionResponse(invitee: Actor) {
        // Bob (Holder) receives final connection response
        wait(
            {
                val lastEvent = ListenToEvents.with(invitee).connectionEvents.lastOrNull {
                    it.data.thid == invitee.recall<Connection>("connection").thid
                }
                lastEvent != null &&
                    lastEvent.data.state == Connection.State.CONNECTION_RESPONSE_RECEIVED
            },
            "Invitee connection didn't reach ${Connection.State.CONNECTION_RESPONSE_RECEIVED} state.",
        )
    }

    @Then("{actor} and {actor} have a connection")
    fun inviterAndInviteeHaveAConnection(inviter: Actor, invitee: Actor) {
        // Connection established. Both parties exchanged their DIDs with each other
        inviter.attemptsTo(
            Get.resource("/connections/${inviter.recall<Connection>("connection").connectionId}"),
        )
        inviter.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
        )
        inviter.remember("connection-with-${invitee.name}", SerenityRest.lastResponse().get<Connection>())

        invitee.attemptsTo(
            Get.resource("/connections/${invitee.recall<Connection>("connection").connectionId}"),
        )
        invitee.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
        )
        invitee.remember("connection-with-${inviter.name}", SerenityRest.lastResponse().get<Connection>())

        assertThat(inviter.recall<Connection>("connection-with-${invitee.name}").myDid)
            .isEqualTo(invitee.recall<Connection>("connection-with-${inviter.name}").theirDid)
        assertThat(inviter.recall<Connection>("connection-with-${invitee.name}").theirDid)
            .isEqualTo(invitee.recall<Connection>("connection-with-${inviter.name}").myDid)
        assertThat(inviter.recall<Connection>("connection-with-${invitee.name}").state)
            .isEqualTo(Connection.State.CONNECTION_RESPONSE_SENT)
        assertThat(invitee.recall<Connection>("connection-with-${inviter.name}").state)
            .isEqualTo(Connection.State.CONNECTION_RESPONSE_RECEIVED)
    }
}
