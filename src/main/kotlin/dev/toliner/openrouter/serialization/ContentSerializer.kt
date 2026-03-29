package dev.toliner.openrouter.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = ContentSerializer::class)
public sealed interface Content {
    @Serializable
    public data class Text(val value: String) : Content
    
    @Serializable
    public data class Parts(val parts: List<ContentPart>) : Content
}

@Serializable
public sealed interface ContentPart {
    @Serializable
    @SerialName("text")
    public data class TextPart(
        @SerialName("text")
        val text: String
    ) : ContentPart
}

internal object ContentSerializer : KSerializer<Content> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Content")
    
    override fun deserialize(decoder: Decoder): Content {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }
        
        val element = decoder.decodeJsonElement()
        
        return when {
            element is JsonPrimitive && element.isString -> {
                Content.Text(element.content)
            }
            else -> {
                val parts = decoder.json.decodeFromJsonElement(
                    ListSerializer(ContentPart.serializer()),
                    element
                )
                Content.Parts(parts)
            }
        }
    }
    
    override fun serialize(encoder: Encoder, value: Content) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }
        
        when (value) {
            is Content.Text -> {
                encoder.encodeString(value.value)
            }
            is Content.Parts -> {
                encoder.encodeSerializableValue(
                    ListSerializer(ContentPart.serializer()),
                    value.parts
                )
            }
        }
    }
}
