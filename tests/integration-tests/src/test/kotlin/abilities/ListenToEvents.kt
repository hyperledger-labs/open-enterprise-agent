package abilities

import com.google.gson.GsonBuilder
import common.TestConstants
import io.iohk.atala.automation.restassured.CustomGsonObjectMapperFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.*
import net.serenitybdd.screenplay.Ability
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.HasTeardown
import java.net.URL
import java.time.OffsetDateTime

open class ListenToEvents(
    private val url: URL,
    webhookPort: Int?,
) : Ability, HasTeardown {

    private val server: ApplicationEngine
    private val gson = GsonBuilder()
        .registerTypeAdapter(OffsetDateTime::class.java, CustomGsonObjectMapperFactory.OffsetDateTimeDeserializer())
        .create()

    var connectionEvents: MutableList<ConnectionEvent> = mutableListOf()
    var credentialEvents: MutableList<CredentialEvent> = mutableListOf()
    var presentationEvents: MutableList<PresentationEvent> = mutableListOf()
    var didEvents: MutableList<DidEvent> = mutableListOf()

    private fun route(application: Application) {
        application.routing {
            post("/") {
                val eventString = call.receiveText()
                val event = gson.fromJson(eventString, Event::class.java)
                when (event.type) {
                    TestConstants.EVENT_TYPE_CONNECTION_UPDATED -> connectionEvents.add(gson.fromJson(eventString, ConnectionEvent::class.java))
                    TestConstants.EVENT_TYPE_ISSUE_CREDENTIAL_RECORD_UPDATED -> credentialEvents.add(gson.fromJson(eventString, CredentialEvent::class.java))
                    TestConstants.EVENT_TYPE_PRESENTATION_UPDATED -> presentationEvents.add(gson.fromJson(eventString, PresentationEvent::class.java))
                    TestConstants.EVENT_TYPE_DID_STATUS_UPDATED -> didEvents.add(gson.fromJson(eventString, DidEvent::class.java))
                    else -> {
                        throw IllegalArgumentException("ERROR: unknown event type ${event.type}")
                    }
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }

    companion object {
        fun at(url: URL, webhookPort: Int?): ListenToEvents {
            return ListenToEvents(url, webhookPort)
        }

        fun with(actor: Actor): ListenToEvents {
            return actor.abilityTo(ListenToEvents::class.java)
        }
    }

    init {
        server = embeddedServer(
            Netty,
            port = webhookPort ?: url.port,
            host = "0.0.0.0",
            module = { route(this) },
        )
            .start(wait = false)
    }

    override fun toString(): String {
        return "Listen HTTP port at $url"
    }

    override fun tearDown() {
        server.stop()
    }
}
