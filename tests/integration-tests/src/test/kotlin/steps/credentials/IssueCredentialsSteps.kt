package steps.credentials

import abilities.ListenToEvents
import common.CredentialSchema
import common.Utils.wait
import interactions.Post
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.prism.models.*
import models.AnoncredsSchema
import models.CredentialEvent
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import net.serenitybdd.screenplay.rest.abilities.CallAnApi
import org.apache.http.HttpStatus.*
import java.util.UUID

class IssueCredentialsSteps {

    private var credentialEvent: CredentialEvent? = null

    private fun sendCredentialOffer(
        issuer: Actor,
        holder: Actor,
        didForm: String,
        schemaGuid: String?,
        claims: Map<String, Any>
    ) {
        val did: String = if (didForm == "short") {
            issuer.recall("shortFormDid")
        } else {
            issuer.recall("longFormDid")
        }

        val schemaId: String? = if (schemaGuid != null) {
            val baseUrl = issuer.recall<String>("baseUrl")
            "$baseUrl/schema-registry/schemas/$schemaGuid"
        } else {
            null
        }

        val credentialOfferRequest = CreateIssueCredentialRecordRequest(
            schemaId = schemaId,
            claims = claims,
            issuingDID = did,
            connectionId = issuer.recall<Connection>("connection-with-${holder.name}").connectionId,
            validityPeriod = 3600.0,
            credentialFormat = "JWT",
            automaticIssuance = false,
        )

        issuer.attemptsTo(
            Post.to("/issue-credentials/credential-offers")
                .with {
                    it.body(credentialOfferRequest)
                },
        )
    }

    private fun saveCredentialOffer(issuer: Actor, holder: Actor) {
        val credentialRecord = SerenityRest.lastResponse().get<IssueCredentialRecord>()

        issuer.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
        )

        issuer.remember("thid", credentialRecord.thid)
        holder.remember("thid", credentialRecord.thid)
    }

    @When("{actor} offers a credential to {actor} with {string} form DID")
    fun issuerOffersACredential(issuer: Actor, holder: Actor, format: String) {
        val claims = linkedMapOf(
            "firstName" to "FirstName",
            "lastName" to "LastName",
        )
        sendCredentialOffer(issuer, holder, format, null, claims)
        saveCredentialOffer(issuer, holder)
    }

    @When("{actor} offers a credential to {actor} with {} form using {} schema")
    fun issuerOffersCredentialToHolderUsingSchema(
        issuer: Actor,
        holder: Actor,
        format: String,
        schema: CredentialSchema
    ) {
        val schemaGuid = issuer.recall<String>(schema.name)!!
        val claims = linkedMapOf(
            "name" to "Name",
            "age" to 18
        )
        sendCredentialOffer(issuer, holder, format, schemaGuid, claims)
        saveCredentialOffer(issuer, holder)
    }

    @When("{actor} offers a credential to {actor} with {} form DID with wrong claims structure using {} schema")
    fun issuerOffersCredentialToHolderWithWrongClaimStructure(
        issuer: Actor,
        holder: Actor,
        format: String,
        schema: CredentialSchema
    ) {
        val schemaGuid = issuer.recall<String>(schema.name)!!
        val claims = linkedMapOf(
            "name" to "Name",
            "surname" to "Surname"
        )
        sendCredentialOffer(issuer, holder, "short", schemaGuid, claims)
    }

    @When("{actor} creates anoncred schema")
    fun acmeCreatesAnoncredSchema(issuer: Actor) {
        issuer.attemptsTo(
            Post.to("/schema-registry/schemas")
                .with {
                    it.body(
                        CredentialSchemaInput(
                            author = issuer.recall("shortFormDid"),
                            name = UUID.randomUUID().toString(),
                            description = "Simple student credentials schema",
                            type = "AnoncredSchemaV1",
                            schema = AnoncredsSchema(
                                name = "StudentCredential",
                                version = "1.0",
                                issuerId = issuer.recall("shortFormDid"),
                                attrNames = listOf("name", "age", "sex"),
                            ),
                            tags = listOf("school", "students"),
                            version = "1.0.0",
                        ),
                    )
                },
        )
        issuer.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
        )
        val schema = SerenityRest.lastResponse().get<CredentialSchemaResponse>()
        issuer.remember("anoncredsSchema", schema)
    }

    @When("{actor} creates anoncred credential definition")
    fun acmeCreatesAnoncredCredentialDefinition(issuer: Actor) {
        val schemaRegistryUrl = issuer.usingAbilityTo(CallAnApi::class.java).resolve("/schema-registry/schemas")
            .replace("localhost", "host.docker.internal")
        issuer.attemptsTo(
            Post.to("/credential-definition-registry/definitions")
                .with {
                    it.body(
                        CredentialDefinitionInput(
                            name = "StudentCredential",
                            version = "1.0.0",
                            schemaId = "$schemaRegistryUrl/${issuer.recall<CredentialSchemaResponse>("anoncredsSchema").guid}/schema",
                            description = "Simple student credentials definition",
                            author = issuer.recall("shortFormDid"),
                            signatureType = "CL",
                            tag = "student",
                            supportRevocation = false,
                        ),
                    )
                },
        )
        issuer.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
        )
        val credentialDefinition = SerenityRest.lastResponse().get<CredentialDefinitionResponse>()
        issuer.remember("anoncredsCredentialDefinition", credentialDefinition)
    }

    @When("{actor} offers anoncred to {actor}")
    fun acmeOffersAnoncredToBob(issuer: Actor, holder: Actor) {
        val credentialOfferRequest = CreateIssueCredentialRecordRequest(
            credentialDefinitionId = issuer.recall<CredentialDefinitionResponse>("anoncredsCredentialDefinition").guid,
            claims = linkedMapOf(
                "name" to "Bob",
                "age" to "21",
                "sex" to "M",
            ),
            issuingDID = issuer.recall("shortFormDid"),
            connectionId = issuer.recall<Connection>("connection-with-${holder.name}").connectionId,
            validityPeriod = 3600.0,
            credentialFormat = "AnonCreds",
            automaticIssuance = false,
        )

        issuer.attemptsTo(
            Post.to("/issue-credentials/credential-offers")
                .with {
                    it.body(credentialOfferRequest)
                },
        )

        val credentialRecord = SerenityRest.lastResponse().get<IssueCredentialRecord>()

        issuer.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
        )

        issuer.remember("thid", credentialRecord.thid)
        holder.remember("thid", credentialRecord.thid)
    }

    @When("{actor} receives the credential offer")
    fun holderReceivesCredentialOffer(holder: Actor) {
        wait(
            {
                credentialEvent = ListenToEvents.`as`(holder).credentialEvents.lastOrNull {
                    it.data.thid == holder.recall<String>("thid")
                }
                credentialEvent != null &&
                        credentialEvent!!.data.protocolState == IssueCredentialRecord.ProtocolState.OFFER_RECEIVED
            },
            "Holder was unable to receive the credential offer from Issuer! " +
                    "Protocol state did not achieve ${IssueCredentialRecord.ProtocolState.OFFER_RECEIVED} state.",
        )

        val recordId = ListenToEvents.`as`(holder).credentialEvents.last().data.recordId
        holder.remember("recordId", recordId)
    }

    @When("{actor} accepts credential offer for JWT")
    fun holderAcceptsCredentialOfferForJwt(holder: Actor) {
        holder.attemptsTo(
            Post.to("/issue-credentials/records/${holder.recall<String>("recordId")}/accept-offer")
                .with {
                    it.body(
                        AcceptCredentialOfferRequest(holder.recall("longFormDid")),
                    )
                },
        )
        holder.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
        )
    }

    @When("{actor} accepts credential offer for anoncred")
    fun holderAcceptsCredentialOfferForAnoncred(holder: Actor) {
        holder.attemptsTo(
            Post.to("/issue-credentials/records/${holder.recall<String>("recordId")}/accept-offer")
                .with {
                    it.body(
                        "{}",
                    )
                },
        )
        holder.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
        )
    }

    @When("{actor} issues the credential")
    fun acmeIssuesTheCredential(issuer: Actor) {
        wait(
            {
                credentialEvent = ListenToEvents.`as`(issuer).credentialEvents.lastOrNull {
                    it.data.thid == issuer.recall<String>("thid")
                }
                credentialEvent != null &&
                        credentialEvent!!.data.protocolState == IssueCredentialRecord.ProtocolState.REQUEST_RECEIVED
            },
            "Issuer was unable to receive the credential request from Holder! Protocol state did not achieve RequestReceived state.",
        )
        val recordId = credentialEvent!!.data.recordId
        issuer.attemptsTo(
            Post.to("/issue-credentials/records/$recordId/issue-credential"),
        )
        issuer.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
        )

        wait(
            {
                credentialEvent = ListenToEvents.`as`(issuer).credentialEvents.lastOrNull {
                    it.data.thid == issuer.recall<String>("thid")
                }
                credentialEvent != null &&
                        credentialEvent!!.data.protocolState == IssueCredentialRecord.ProtocolState.CREDENTIAL_SENT
            },
            "Issuer was unable to issue the credential! " +
                    "Protocol state did not achieve ${IssueCredentialRecord.ProtocolState.CREDENTIAL_SENT} state.",
        )
    }

    @Then("{actor} receives the issued credential")
    fun bobHasTheCredentialIssued(holder: Actor) {
        wait(
            {
                credentialEvent = ListenToEvents.`as`(holder).credentialEvents.lastOrNull {
                    it.data.thid == holder.recall<String>("thid")
                }
                credentialEvent != null &&
                        credentialEvent!!.data.protocolState == IssueCredentialRecord.ProtocolState.CREDENTIAL_RECEIVED
            },
            "Holder was unable to receive the credential from Issuer! " +
                    "Protocol state did not achieve ${IssueCredentialRecord.ProtocolState.CREDENTIAL_RECEIVED} state.",
        )
        holder.remember("issuedCredential", ListenToEvents.`as`(holder).credentialEvents.last().data)
    }

    @Then("{actor} should see that credential issuance has failed")
    fun issuerShouldSeeThatCredentialIssuanceHasFailed(issuer: Actor) {
        issuer.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_BAD_REQUEST)
        )
    }
}
