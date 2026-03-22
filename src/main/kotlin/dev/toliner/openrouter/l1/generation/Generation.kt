package dev.toliner.openrouter.l1.generation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Generation(
    val id: String,
    val model: String,
    val usage: GenerationUsage,
    val tokens: Int,
    val streamed: Boolean,
    @SerialName("generation_time")
    val generationTime: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("native_tokens_prompt")
    val nativeTokensPrompt: Int? = null,
    @SerialName("native_tokens_completion")
    val nativeTokensCompletion: Int? = null
)

@Serializable
data class GenerationUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
    val cost: Double
)
