package dev.toliner.openrouter.l1.embeddings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingData(
    @SerialName("embedding")
    val embedding: List<Double>,
    @SerialName("index")
    val index: Int,
    @SerialName("object")
    val `object`: String
)
