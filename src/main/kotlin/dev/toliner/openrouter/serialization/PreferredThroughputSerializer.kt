package dev.toliner.openrouter.serialization

import dev.toliner.openrouter.l1.chat.PercentileCutoffs
import dev.toliner.openrouter.l1.chat.PreferredThroughput
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

internal object PreferredThroughputSerializer : KSerializer<PreferredThroughput> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PreferredThroughput")

    override fun deserialize(decoder: Decoder): PreferredThroughput {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }

        val element = decoder.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> PreferredThroughput.Value(element.jsonPrimitive.content.toDouble())
            else -> {
                val obj = element.jsonObject
                PreferredThroughput.Percentile(
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

    override fun serialize(encoder: Encoder, value: PreferredThroughput) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }

        when (value) {
            is PreferredThroughput.Value -> encoder.encodeDouble(value.tokensPerSecond)
            is PreferredThroughput.Percentile -> {
                encoder.encodeSerializableValue(
                    PercentileCutoffs.serializer(),
                    value.cutoffs
                )
            }
        }
    }
}
