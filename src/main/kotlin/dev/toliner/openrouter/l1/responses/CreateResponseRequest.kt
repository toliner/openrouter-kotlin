package dev.toliner.openrouter.l1.responses

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive

@ExperimentalOpenRouterApi
@Serializable(with = ResponseInputSerializer::class)
sealed interface ResponseInput {
    @Serializable
    data class Text(val value: String) : ResponseInput
    
    @Serializable
    data class Items(val items: List<InputItem>) : ResponseInput
}

object ResponseInputSerializer : KSerializer<ResponseInput> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ResponseInput")
    
    override fun deserialize(decoder: Decoder): ResponseInput {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }
        
        val element = decoder.decodeJsonElement()
        
        return when {
            element is JsonPrimitive && element.isString -> {
                ResponseInput.Text(element.content)
            }
            element is JsonArray -> {
                val items = decoder.json.decodeFromJsonElement(
                    ListSerializer(InputItem.serializer()),
                    element
                )
                ResponseInput.Items(items)
            }
            else -> throw kotlinx.serialization.SerializationException("Unknown input format")
        }
    }
    
    override fun serialize(encoder: Encoder, value: ResponseInput) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }
        
        when (value) {
            is ResponseInput.Text -> {
                encoder.encodeString(value.value)
            }
            is ResponseInput.Items -> {
                encoder.encodeSerializableValue(
                    ListSerializer(InputItem.serializer()),
                    value.items
                )
            }
        }
    }
}

@ExperimentalOpenRouterApi
@Serializable
data class CreateResponseRequest(
    @SerialName("model")
    val model: String,
    @SerialName("input")
    val input: ResponseInput,
    @SerialName("instructions")
    val instructions: String? = null,
    @SerialName("tools")
    val tools: List<ResponseTool>? = null,
    @SerialName("temperature")
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null
)
