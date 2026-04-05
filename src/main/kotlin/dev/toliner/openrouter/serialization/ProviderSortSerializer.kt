package dev.toliner.openrouter.serialization

import dev.toliner.openrouter.l1.chat.ProviderSort
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object ProviderSortSerializer : KSerializer<ProviderSort> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ProviderSort")

    override fun deserialize(decoder: Decoder): ProviderSort {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }

        val element = decoder.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> ProviderSort.Simple(element.content)
            else -> {
                val obj = element.jsonObject
                ProviderSort.Advanced(
                    by = obj.getValue("by").jsonPrimitive.content,
                    partition = obj["partition"]?.jsonPrimitive?.content
                )
            }
        }
    }

    override fun serialize(encoder: Encoder, value: ProviderSort) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }

        when (value) {
            is ProviderSort.Simple -> encoder.encodeString(value.value)
            is ProviderSort.Advanced -> {
                @Serializable
                data class AdvancedDto(
                    @SerialName("by") val by: String,
                    @SerialName("partition") val partition: String? = null
                )
                encoder.encodeSerializableValue(
                    AdvancedDto.serializer(),
                    AdvancedDto(by = value.by, partition = value.partition)
                )
            }
        }
    }
}
