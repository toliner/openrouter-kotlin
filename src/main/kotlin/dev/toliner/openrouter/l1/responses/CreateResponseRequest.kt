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

/**
 * Input format for a response request in the OpenRouter Responses API.
 *
 * This is an experimental API that mirrors OpenAI's responses endpoint. The input can be either
 * a simple text string or a list of structured input items (messages and function call outputs).
 *
 * **Note:** This API is experimental and subject to change. It is marked with [ExperimentalOpenRouterApi].
 *
 * @see Text
 * @see Items
 * @see CreateResponseRequest
 */
@ExperimentalOpenRouterApi
@Serializable(with = ResponseInputSerializer::class)
public sealed interface ResponseInput {
    /**
     * Simple text input for a response request.
     *
     * @property value The text content to process
     */
    @Serializable
    public data class Text(val value: String) : ResponseInput
    
    /**
     * Structured input consisting of multiple items (messages and function call outputs).
     *
     * @property items List of input items including messages and function call outputs
     *
     * @see InputItem
     */
    @Serializable
    public data class Items(val items: List<InputItem>) : ResponseInput
}

/**
 * Custom serializer for [ResponseInput] that handles JSON union type (string | array).
 *
 * Serializes [ResponseInput.Text] as a JSON string and [ResponseInput.Items] as a JSON array.
 */
public object ResponseInputSerializer : KSerializer<ResponseInput> {
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

/**
 * Request body for creating a response using the OpenRouter Responses API.
 *
 * This is an experimental API similar to OpenAI's responses endpoint, allowing structured input
 * with tool support and generation parameters.
 *
 * **Note:** This API is experimental and subject to change. It is marked with [ExperimentalOpenRouterApi].
 *
 * @property model Model identifier to use for generating the response
 * @property input Input content as either text or structured items
 * @property instructions Optional system instructions to guide the model's behavior
 * @property tools Optional list of tools (functions) available to the model
 * @property temperature Optional sampling temperature (0.0 to 2.0); higher values increase randomness
 * @property topP Optional nucleus sampling parameter (0.0 to 1.0)
 * @property maxTokens Optional maximum number of tokens to generate
 *
 * @see ResponseInput
 * @see ResponseTool
 * @see ResponseObject
 */
@ExperimentalOpenRouterApi
@Serializable
public data class CreateResponseRequest(
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
