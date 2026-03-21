package dev.toliner.openrouter.l1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Model(
    val id: String,
    val name: String,
    val description: String? = null,
    val pricing: Pricing,
    @SerialName("context_length")
    val contextLength: Int,
    val architecture: Architecture,
    @SerialName("top_provider")
    val topProvider: TopProvider,
    @SerialName("per_request_limits")
    val perRequestLimits: PerRequestLimits? = null
)

@Serializable
data class Pricing(
    val prompt: String,
    val completion: String,
    val request: String,
    val image: String
)

@Serializable
data class Architecture(
    val modality: String,
    val tokenizer: String,
    @SerialName("instruct_type")
    val instructType: String? = null
)

@Serializable
data class TopProvider(
    @SerialName("context_length")
    val contextLength: Int,
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    @SerialName("is_moderated")
    val isModerated: Boolean
)

@Serializable
data class PerRequestLimits(
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerialName("completion_tokens")
    val completionTokens: Int? = null
)
