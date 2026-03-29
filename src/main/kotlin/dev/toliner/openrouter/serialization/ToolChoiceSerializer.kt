package dev.toliner.openrouter.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Controls which tool (function) the model should call.
 *
 * Represents a JSON union type that deserializes from either:
 * - A string mode value (e.g., "auto", "none", "required") → [ToolChoice.Mode]
 * - An object specifying a specific function → [ToolChoice.Function]
 *
 * This type is used in chat completion requests to control function calling behavior.
 *
 * @see FunctionChoice
 */
@Serializable(with = ToolChoiceSerializer::class)
public sealed interface ToolChoice {
    /**
     * A string mode controlling automatic function calling behavior.
     *
     * Common values include:
     * - "auto" — model decides whether to call a function
     * - "none" — model will not call any function
     * - "required" — model must call at least one function
     *
     * Serializes to and from a JSON string.
     *
     * @property value The mode string
     */
    @Serializable
    public data class Mode(val value: String) : ToolChoice
    
    /**
     * Forces the model to call a specific function.
     *
     * Serializes to and from a JSON object with type "function" and a nested function specification.
     *
     * @property function The specific function to call
     * @see FunctionChoice
     */
    @Serializable
    public data class Function(
        @SerialName("function")
        val function: FunctionChoice
    ) : ToolChoice
}

/**
 * Identifies a specific function by name.
 *
 * Used in [ToolChoice.Function] to force the model to call a particular function.
 *
 * @property name The name of the function to call
 * @see ToolChoice.Function
 */
@Serializable
public data class FunctionChoice(
    @SerialName("name")
    val name: String
)

internal object ToolChoiceSerializer : KSerializer<ToolChoice> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ToolChoice")
    
    override fun deserialize(decoder: Decoder): ToolChoice {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }
        
        val element = decoder.decodeJsonElement()
        
        return when {
            element is JsonPrimitive && element.isString -> {
                ToolChoice.Mode(element.content)
            }
            else -> {
                @Serializable
                data class ToolChoiceObject(
                    @SerialName("type")
                    val type: String,
                    @SerialName("function")
                    val function: FunctionChoice
                )
                
                val obj = decoder.json.decodeFromJsonElement(ToolChoiceObject.serializer(), element)
                ToolChoice.Function(function = obj.function)
            }
        }
    }
    
    override fun serialize(encoder: Encoder, value: ToolChoice) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }
        
        when (value) {
            is ToolChoice.Mode -> {
                encoder.encodeString(value.value)
            }
            is ToolChoice.Function -> {
                @Serializable
                data class ToolChoiceObject(
                    @SerialName("type")
                    val type: String,
                    @SerialName("function")
                    val function: FunctionChoice
                )
                
                encoder.encodeSerializableValue(
                    ToolChoiceObject.serializer(),
                    ToolChoiceObject(type = "function", function = value.function)
                )
            }
        }
    }
}
