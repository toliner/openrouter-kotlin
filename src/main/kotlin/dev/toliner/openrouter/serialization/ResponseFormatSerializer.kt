package dev.toliner.openrouter.serialization

import dev.toliner.openrouter.l1.chat.JsonSchemaConfig
import dev.toliner.openrouter.l1.chat.ResponseFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal object ResponseFormatSerializer : KSerializer<ResponseFormat> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ResponseFormat")

    override fun deserialize(decoder: Decoder): ResponseFormat {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }

        val element = decoder.decodeJsonElement().jsonObject
        val type = element["type"]?.jsonPrimitive?.content
            ?: error("ResponseFormat must have a 'type' field")

        return when (type) {
            "text" -> ResponseFormat.Text
            "json_object" -> ResponseFormat.JsonObject
            "json_schema" -> {
                @Serializable
                data class JsonSchemaWrapper(
                    @SerialName("json_schema")
                    val jsonSchema: JsonSchemaConfig
                )
                val wrapper = decoder.json.decodeFromJsonElement(JsonSchemaWrapper.serializer(), JsonObject(element))
                ResponseFormat.JsonSchema(jsonSchema = wrapper.jsonSchema)
            }
            "grammar" -> {
                val grammar = element["grammar"]?.jsonPrimitive?.content
                    ?: error("ResponseFormat 'grammar' variant must have a 'grammar' field")
                ResponseFormat.Grammar(grammar = grammar)
            }
            "python" -> {
                val python = element["python"]?.jsonPrimitive?.content
                    ?: error("ResponseFormat 'python' variant must have a 'python' field")
                ResponseFormat.Python(python = python)
            }
            else -> error("Unknown ResponseFormat type: $type")
        }
    }

    override fun serialize(encoder: Encoder, value: ResponseFormat) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }

        val jsonElement = when (value) {
            is ResponseFormat.Text -> buildJsonObject { put("type", "text") }
            is ResponseFormat.JsonObject -> buildJsonObject { put("type", "json_object") }
            is ResponseFormat.JsonSchema -> {
                @Serializable
                data class JsonSchemaFormat(
                    @SerialName("type") val type: String,
                    @SerialName("json_schema") val jsonSchema: JsonSchemaConfig
                )
                encoder.json.encodeToJsonElement(
                    JsonSchemaFormat.serializer(),
                    JsonSchemaFormat(type = "json_schema", jsonSchema = value.jsonSchema)
                )
            }
            is ResponseFormat.Grammar -> buildJsonObject {
                put("type", "grammar")
                put("grammar", value.grammar)
            }
            is ResponseFormat.Python -> buildJsonObject {
                put("type", "python")
                put("python", value.python)
            }
        }
        encoder.encodeJsonElement(jsonElement)
    }
}
