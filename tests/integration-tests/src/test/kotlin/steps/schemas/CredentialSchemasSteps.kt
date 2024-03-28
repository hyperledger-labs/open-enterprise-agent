package steps.schemas

import com.google.gson.Gson
import com.google.gson.JsonObject
import common.CredentialSchema
import common.CredentialSchema.STUDENT_SCHEMA
import interactions.Get
import interactions.Post
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.automation.extensions.get
import io.iohk.atala.automation.serenity.ensure.Ensure
import io.iohk.atala.prism.models.CredentialSchemaInput
import io.iohk.atala.prism.models.CredentialSchemaResponse
import models.JsonSchema
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import org.apache.http.HttpStatus.*
import java.util.UUID

class CredentialSchemasSteps {

    @Given("{actor} has published {} schema")
    fun agentHasAPublishedSchema(agent: Actor, schema: CredentialSchema) {
        if (agent.recallAll().containsKey(schema.name)) {
            return
        }
        agentCreatesANewCredentialSchema(agent, schema)
    }

    @When("{actor} creates a new credential {} schema")
    fun agentCreatesANewCredentialSchema(actor: Actor, schema: CredentialSchema) {
        actor.attemptsTo(
            Post.to("/schema-registry/schemas").with {
                it.body(
                    schema.credentialSchema.copy(author = actor.recall("shortFormDid")),
                )
            },
        )
        actor.remember(schema.name, SerenityRest.lastResponse().get<String>("guid"))
    }

    @When("{actor} creates a schema containing '{}' issue")
    fun agentCreatesASchemaContainingIssue(actor: Actor, schema: SchemaErrorTemplate) {
        actor.attemptsTo(
            Post.to("/schema-registry/schemas").with {
                it.body(schema.schema(actor))
            },
        )
    }

    @Then("{actor} sees new credential schema is available")
    fun newCredentialSchemaIsAvailable(actor: Actor) {
        val credentialSchema = SerenityRest.lastResponse().get<CredentialSchemaResponse>()
        val jsonSchema = SerenityRest.lastResponse().get<JsonSchema>("schema")

        actor.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
            Ensure.that(credentialSchema.guid).isNotNull(),
            Ensure.that(credentialSchema.id).isNotNull(),
            Ensure.that(credentialSchema.longId!!).isNotNull(),
            Ensure.that(credentialSchema.authored).isNotNull(),
            Ensure.that(credentialSchema.kind).isEqualTo("CredentialSchema"),
            Ensure.that(credentialSchema.name).contains(STUDENT_SCHEMA.credentialSchema.name),
            Ensure.that(credentialSchema.description).contains(STUDENT_SCHEMA.credentialSchema.description!!),
            Ensure.that(credentialSchema.version).contains(STUDENT_SCHEMA.credentialSchema.version),
            Ensure.that(credentialSchema.type).isEqualTo(STUDENT_SCHEMA.credentialSchemaType),
            Ensure.that(credentialSchema.tags!!)
                .containsExactlyInAnyOrderElementsFrom(STUDENT_SCHEMA.credentialSchema.tags!!),
            Ensure.that(jsonSchema.toString()).isEqualTo(STUDENT_SCHEMA.schema.toString()),
        )
    }

    @When("{actor} creates {int} new schemas")
    fun acmeCreatesMultipleSchemas(actor: Actor, numberOfSchemas: Int) {
        val createdSchemas: MutableList<CredentialSchemaResponse> = mutableListOf()
        repeat(numberOfSchemas) { i: Int ->
            actor.attemptsTo(
                Post.to("/schema-registry/schemas").with {
                    it.body(
                        CredentialSchemaInput(
                            author = actor.recall("shortFormDid"),
                            name = "${UUID.randomUUID()} $i",
                            description = "Simple student credentials schema",
                            type = STUDENT_SCHEMA.credentialSchemaType,
                            schema = STUDENT_SCHEMA.schema,
                            tags = listOf("school", "students"),
                            version = "1.0.0",
                        ),
                    )
                },
            )
            actor.attemptsTo(
                Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_CREATED),
            )
            createdSchemas.add(SerenityRest.lastResponse().get<CredentialSchemaResponse>())
        }
        actor.remember("createdSchemas", createdSchemas)
    }

    @Then("{actor} can access all of them one by one")
    fun theyCanBeAccessedWithPagination(actor: Actor) {
        actor.recall<List<CredentialSchemaResponse>>("createdSchemas").forEach { schema ->
            actor.attemptsTo(
                Get.resource("/schema-registry/schemas/${schema.guid}"),
            )
            actor.attemptsTo(
                Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_OK),
            )
        }
    }

    @Then("{actor} should see the schema creation failed")
    fun schemaCreationShouldFail(agent: Actor) {
        agent.attemptsTo(
            Ensure.thatTheLastResponse().statusCode().isEqualTo(SC_BAD_REQUEST)
        )
    }
}

enum class SchemaErrorTemplate {
    TYPE_AND_PROPERTIES_WITHOUT_SCHEMA_TYPE {
        override fun inner_schema(): String {
            return """
                {
                    "type": "object",
                    "properties": {
                        "name": {
                            "type": "string"
                        },
                        "age": {
                            "type": "integer"
                        }
                    },
                    "required": ["name"]
                }
                """.trimIndent()
        }
    },
    CUSTOM_WORDS_NOT_DEFINED {
        override fun inner_schema(): String {
            return """
                {
                  "${"$"}schema": "http://json-schema.org/draft-2020-12/schema#",
                  "type": "object",
                  "properties": {
                    "name": {
                      "type": "string"
                    },
                    "age": {
                      "type": "integer"
                    }
                  },
                  "customKeyword": "value"
                }
            """.trimIndent()
        }
    },
    MISSING_REQUIRED_FOR_MANDATORY_PROPERTY {
        override fun inner_schema(): String {
            return """
            {
              "${"$"}schema": "http://json-schema.org/draft-2020-12/schema#",
              "type": "object",
              "properties": {
                "name": {
                  "type": "string"
                },
                "age": {
                  "type": "integer"
                }
              }
            }
            """
        }
    };

    abstract fun inner_schema(): String

    fun schema(actor: Actor): String {
        val innerSchema = Gson().fromJson(inner_schema(), JsonObject::class.java)
        val json = getJson(actor)
        json.add("schema", innerSchema)
        return json.toString()
    }

    private fun getJson(actor: Actor): JsonObject {
        val jsonString = Gson().toJson(STUDENT_SCHEMA.credentialSchema.copy(author = actor.recall("shortFormDid")))
        return Gson().fromJson(jsonString, JsonObject::class.java)
    }
}
