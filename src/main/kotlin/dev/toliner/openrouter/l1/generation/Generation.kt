package dev.toliner.openrouter.l1.generation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Generation statistics for a single API request.
 *
 * This data is retrieved from the `/api/v1/generation?id={generationId}` endpoint
 * and provides detailed metrics about a completed generation request, including
 * token usage, costs, and timing information.
 *
 * @property id Unique identifier for this generation
 * @property model Model name used for the generation (e.g., "openai/gpt-4")
 * @property usage Token usage and cost details
 * @property tokens Total tokens processed (prompt + completion)
 * @property streamed Whether the response was streamed
 * @property generationTime Time taken for generation in milliseconds
 * @property createdAt Timestamp when the generation was created (ISO 8601 format)
 * @property nativeTokensPrompt Native prompt token count (if different from tokenizer)
 * @property nativeTokensCompletion Native completion token count (if different from tokenizer)
 * @see GenerationUsage
 */
@Serializable
public data class Generation(
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

/**
 * Token usage and cost breakdown for a generation.
 *
 * Provides detailed information about token consumption and associated costs
 * for both the prompt and completion portions of a generation request.
 *
 * @property promptTokens Number of tokens in the prompt
 * @property completionTokens Number of tokens in the completion
 * @property totalTokens Total tokens (prompt + completion)
 * @property cost Total cost for the generation in USD
 * @see Generation
 */
@Serializable
public data class GenerationUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
    val cost: Double
)
