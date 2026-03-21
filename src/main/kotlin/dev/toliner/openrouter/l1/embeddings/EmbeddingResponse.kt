package dev.toliner.openrouter.l1.embeddings

import dev.toliner.openrouter.l1.chat.Usage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingResponse(
    @SerialName("data")
    val data: List<EmbeddingData>,
    @SerialName("model")
    val model: String,
    @SerialName("usage")
    val usage: Usage? = null
)
