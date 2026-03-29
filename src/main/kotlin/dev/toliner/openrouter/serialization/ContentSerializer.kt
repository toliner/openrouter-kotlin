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

/**
 * Message content that can be either plain text or structured parts.
 *
 * Represents a JSON union type that deserializes from either:
 * - A string value → [Content.Text]
 * - An array of content parts → [Content.Parts]
 *
 * This type is used in chat messages and other API surfaces that accept multi-modal content.
 *
 * @see ContentPart
 */
@Serializable(with = ContentSerializer::class)
public sealed interface Content {
    /**
     * Plain text content.
     *
     * Serializes to and from a JSON string.
     *
     * @property value The text content
     */
    @Serializable
    public data class Text(val value: String) : Content
    
    /**
     * Structured multi-part content.
     *
     * Serializes to and from a JSON array of content parts. Used for messages containing
     * multiple text segments, images, or other structured content.
     *
     * @property parts The list of content parts
     * @see ContentPart
     */
    @Serializable
    public data class Parts(val parts: List<ContentPart>) : Content
}

/**
 * A single part of structured message content.
 *
 * Represents one element in a [Content.Parts] array. Each part has a specific type
 * (text, image, etc.) and associated data.
 *
 * @see Content.Parts
 */
@Serializable
public sealed interface ContentPart {
    /**
     * A text content part.
     *
     * @property text The text content
     */
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
