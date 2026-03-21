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

@Serializable(with = ToolChoiceSerializer::class)
sealed interface ToolChoice {
    @Serializable
    data class Mode(val value: String) : ToolChoice
    
    @Serializable
    data class Function(
        @SerialName("function")
        val function: FunctionChoice
    ) : ToolChoice
}

@Serializable
data class FunctionChoice(
    @SerialName("name")
    val name: String
)

object ToolChoiceSerializer : KSerializer<ToolChoice> {
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
