package features

import common.TestConstants
import interactions.Get
import interactions.Post
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.prism.models.CredentialSchemaResponse
import models.Schema
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import org.apache.http.HttpStatus.SC_CREATED
import org.apache.http.HttpStatus.SC_OK

class CredentialSchemasSteps {

    @When("{actor} creates a new credential schema")
    fun acmeCreatesANewCredentialSchema(actor: Actor) {
        actor.attemptsTo(
            Post.to("/schema-registry/schemas").with {
                it.body(
                    TestConstants.STUDENT_SCHEMA.copy(author = actor.recall("shortFormDid"))
                )
            }
        )
    }

    @Then("{actor} sees new credential schema is available")
    fun newCredentialSchemaIsAvailable(actor: Actor) {
        val credentialSchema = SerenityRest.lastResponse().get<CredentialSchemaResponse>()
        val schema = SerenityRest.lastResponse().get<Schema>("schema")

        actor.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
            Ensure.that(credentialSchema.guid).isNotNull(),
            Ensure.that(credentialSchema.id).isNotNull(),
            Ensure.that(credentialSchema.longId!!).isNotNull(),
            Ensure.that(credentialSchema.authored).isNotNull(),
            Ensure.that(credentialSchema.kind).isEqualTo("CredentialSchema"),
            Ensure.that(credentialSchema.name).contains(TestConstants.STUDENT_SCHEMA.name),
            Ensure.that(credentialSchema.description).contains(TestConstants.STUDENT_SCHEMA.description!!),
            Ensure.that(credentialSchema.version).contains(TestConstants.STUDENT_SCHEMA.version),
            Ensure.that(credentialSchema.type).isEqualTo(TestConstants.CREDENTIAL_SCHEMA_TYPE),
            Ensure.that(credentialSchema.tags!!).containsExactlyInAnyOrderElementsFrom(TestConstants.STUDENT_SCHEMA.tags!!),
            Ensure.that(schema.toString()).isEqualTo(TestConstants.jsonSchema.toString())
        )
    }

    @When("{actor} creates {int} new schemas")
    fun acmeCreatesMultipleSchemas(actor: Actor, numberOfSchemas: Int) {
        val createdSchemas: MutableList<CredentialSchemaResponse> = mutableListOf()
        repeat(numberOfSchemas) { i: Int ->
            actor.attemptsTo(
                Post.to("/schema-registry/schemas").with {
                    it.body(
                        TestConstants.generate_with_name_suffix_and_author(
                            i.toString(),
                            actor.recall("shortFormDid")
                        )
                    )
                }
            )
            actor.attemptsTo(
                Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED)
            )
            createdSchemas.add(SerenityRest.lastResponse().get<CredentialSchemaResponse>())
        }
        actor.remember("createdSchemas", createdSchemas)
    }

    @Then("{actor} can access all of them one by one")
    fun theyCanBeAccessedWithPagination(actor: Actor) {
        actor.recall<List<CredentialSchemaResponse>>("createdSchemas").forEach { schema ->
            actor.attemptsTo(
                Get.resource("/schema-registry/schemas/${schema.guid}")
            )
            actor.attemptsTo(
                Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK)
            )
        }
    }
}
