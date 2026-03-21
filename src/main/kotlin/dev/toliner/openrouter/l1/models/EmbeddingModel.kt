package dev.toliner.openrouter.l1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingModel(
    val id: String,
    val name: String,
    val pricing: Pricing,
    @SerialName("context_length")
    val contextLength: Int
)
