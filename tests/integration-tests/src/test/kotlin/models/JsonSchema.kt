package models

import com.google.gson.annotations.SerializedName

data class JsonSchema(
    @SerializedName("\$id")
    var id: String = "",

    @SerializedName("\$schema")
    var schema: String = "",

    @SerializedName("description")
    var description: String = "",

    @SerializedName("type")
    var type: String = "",

    @SerializedName("required")
    var required: List<String> = emptyList(),

    @SerializedName("properties")
    val properties: MutableMap<String, JsonSchemaProperty> = mutableMapOf(),
)
