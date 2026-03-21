package dev.toliner.openrouter.l1.embeddings

import dev.toliner.openrouter.serialization.StringOrArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingRequest(
    @SerialName("model")
    val model: String,
    @SerialName("input")
    val input: StringOrArray,
    @SerialName("encoding_format")
    val encodingFormat: String? = null
)
