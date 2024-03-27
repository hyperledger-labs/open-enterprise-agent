package common

import io.iohk.atala.prism.models.CredentialSchemaInput
import models.JsonSchema
import models.JsonSchemaProperty
import java.util.UUID

enum class CredentialSchema {
    STUDENT_SCHEMA {
        override val credentialSchemaType: String =
            "https://w3c-ccg.github.io/vc-json-schemas/schema/2.0/schema.json"
        override val schemaType: String = "https://json-schema.org/draft/2020-12/schema"
        override val schema: JsonSchema = JsonSchema(
            id = "https://example.com/student-schema-1.0",
            schema = schemaType,
            description = "Student schema",
            type = "object",
            properties = mutableMapOf(
                "name" to JsonSchemaProperty(type = "string"),
                "age" to JsonSchemaProperty(type = "integer"),
            ),
            required = listOf("name", "age")
        )
        override val credentialSchema: CredentialSchemaInput = CredentialSchemaInput(
            author = "did:prism:agent",
            name = UUID.randomUUID().toString(),
            description = "Simple student credentials schema",
            type = credentialSchemaType,
            schema = schema,
            tags = listOf("school", "students"),
            version = "1.0.0",
        )
    };

    abstract val credentialSchema: CredentialSchemaInput
    abstract val schema: JsonSchema
    abstract val credentialSchemaType: String
    abstract val schemaType: String
}
