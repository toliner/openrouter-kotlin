package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents token usage statistics for a chat completion request.
 *
 * Token usage is important for understanding API costs and monitoring consumption.
 * Different providers may have different tokenization schemes, so token counts
 * are approximate and provider-dependent.
 *
 * @property promptTokens Number of tokens in the input prompt (messages and tools).
 * @property completionTokens Number of tokens generated in the model's completion.
 * @property totalTokens Total tokens used (promptTokens + completionTokens).
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
    val totalTokens: Int
)
