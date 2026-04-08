package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents token usage statistics for a chat completion request.
 *
 * @property promptTokens Number of tokens in the input prompt (messages and tools).
 * @property completionTokens Number of tokens generated in the model's completion.
 * @property totalTokens Total tokens used (promptTokens + completionTokens).
 * @property completionTokensDetails Detailed breakdown of completion token usage.
 * @property promptTokensDetails Detailed breakdown of prompt token usage.
 *
 * @see ChatCompletionResponse.usage
 * @see ChatCompletionChunk.usage
 */
@Serializable
public data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
    @SerialName("completion_tokens_details")
    val completionTokensDetails: CompletionTokensDetails? = null,
    @SerialName("prompt_tokens_details")
    val promptTokensDetails: PromptTokensDetails? = null
)

/**
 * Detailed breakdown of completion token usage.
 *
 * @property reasoningTokens Tokens used for reasoning (e.g., chain-of-thought).
 * @property audioTokens Tokens used for audio output.
 * @property acceptedPredictionTokens Accepted prediction tokens.
 * @property rejectedPredictionTokens Rejected prediction tokens.
 */
@Serializable
public data class CompletionTokensDetails(
    @SerialName("reasoning_tokens")
    val reasoningTokens: Int? = null,
    @SerialName("audio_tokens")
    val audioTokens: Int? = null,
    @SerialName("accepted_prediction_tokens")
    val acceptedPredictionTokens: Int? = null,
    @SerialName("rejected_prediction_tokens")
    val rejectedPredictionTokens: Int? = null
)

/**
 * Detailed breakdown of prompt token usage.
 *
 * @property cachedTokens Cached prompt tokens.
 * @property cacheWriteTokens Tokens written to cache. Only returned for models with explicit caching.
 * @property audioTokens Audio input tokens.
 * @property videoTokens Video input tokens.
 */
@Serializable
public data class PromptTokensDetails(
    @SerialName("cached_tokens")
    val cachedTokens: Int? = null,
    @SerialName("cache_write_tokens")
    val cacheWriteTokens: Int? = null,
    @SerialName("audio_tokens")
    val audioTokens: Int? = null,
    @SerialName("video_tokens")
    val videoTokens: Int? = null
)
