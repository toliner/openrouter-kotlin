package dev.toliner.openrouter.serialization

import dev.toliner.openrouter.l1.chat.PercentileCutoffs
import dev.toliner.openrouter.l1.chat.PreferredLatency
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object PreferredLatencySerializer : KSerializer<PreferredLatency> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PreferredLatency")

    override fun deserialize(decoder: Decoder): PreferredLatency {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }

        val element = decoder.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> PreferredLatency.Value(element.jsonPrimitive.content.toDouble())
            else -> {
                val obj = element.jsonObject
                PreferredLatency.Percentile(
                    PercentileCutoffs(
                        p50 = obj["p50"]?.jsonPrimitive?.content?.toDouble(),
                        p75 = obj["p75"]?.jsonPrimitive?.content?.toDouble(),
                        p90 = obj["p90"]?.jsonPrimitive?.content?.toDouble(),
                        p99 = obj["p99"]?.jsonPrimitive?.content?.toDouble()
                    )
                )
            }
        }
    }

    override fun serialize(encoder: Encoder, value: PreferredLatency) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }

        when (value) {
            is PreferredLatency.Value -> encoder.encodeDouble(value.seconds)
            is PreferredLatency.Percentile -> {
                encoder.encodeSerializableValue(
                    PercentileCutoffs.serializer(),
                    value.cutoffs
                )
            }
        }
    }
}
