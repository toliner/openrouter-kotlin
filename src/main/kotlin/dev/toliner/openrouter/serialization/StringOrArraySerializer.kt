package dev.toliner.openrouter.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = StringOrArraySerializer::class)
sealed interface StringOrArray {
    @Serializable
    data class Single(val value: String) : StringOrArray
    
    @Serializable
    data class Multiple(val values: List<String>) : StringOrArray
}

object StringOrArraySerializer : KSerializer<StringOrArray> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("StringOrArray")
    
    override fun deserialize(decoder: Decoder): StringOrArray {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }
        
        val element = decoder.decodeJsonElement()
        
        return when {
            element is JsonPrimitive && element.isString -> {
                StringOrArray.Single(element.content)
            }
            else -> {
                val list = element.jsonArray.map { it.jsonPrimitive.content }
                StringOrArray.Multiple(list)
            }
        }
    }
    
    override fun serialize(encoder: Encoder, value: StringOrArray) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }
        
        when (value) {
            is StringOrArray.Single -> {
                encoder.encodeString(value.value)
            }
            is StringOrArray.Multiple -> {
                encoder.encodeSerializableValue(ListSerializer(String.serializer()), value.values)
            }
        }
    }
}
