package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.error.ErrorBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a complete response from the OpenRouter Chat Completions endpoint.
 *
 * This is returned for non-streaming requests to `/chat/completions`.
 * For streaming requests, see [ChatCompletionChunk].
 *
 * @property id Unique identifier for this completion.
 * @property model The model used to generate the completion.
 * @property objectType The object type, always "chat.completion" for non-streaming responses.
 * @property created Unix timestamp (seconds) of when the completion was created.
 * @property choices List of completion choices. Typically contains one choice, but may have more if `n > 1`.
 * @property usage Token usage statistics for this request, if available.
 *
 * @see ChatCompletionRequest
 * @see Choice
 * @see Usage
 * @see ChatCompletionChunk
 */
@Serializable
public data class ChatCompletionResponse(
    @SerialName("id")
    val id: String,
    @SerialName("model")
    val model: String,
    @SerialName("object")
    val objectType: String,
    @SerialName("created")
    val created: Long,
    @SerialName("choices")
    val choices: List<Choice>,
    @SerialName("usage")
    val usage: Usage? = null
)

/**
 * Represents a single completion choice in a chat completion response.
 *
 * Each choice contains a complete message from the model and metadata about
 * how the generation finished. If multiple choices were requested (`n > 1`),
 * the response will contain multiple Choice objects.
 *
 * @property index The index of this choice in the list of choices.
 * @property message The generated message from the model.
 * @property finishReason Why the model stopped generating ("stop", "length", "tool_calls", "content_filter", etc.).
 * @property error Error information if this specific choice encountered an error.
 *
 * @see ChatCompletionResponse
 * @see Message
 * @see ErrorBody
 */
@Serializable
public data class Choice(
    @SerialName("index")
    val index: Int,
    @SerialName("message")
    val message: Message,
    @SerialName("finish_reason")
    val finishReason: String? = null,
    @SerialName("error")
    val error: ErrorBody? = null
)
