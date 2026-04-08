package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.error.ErrorBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
 * @property systemFingerprint System fingerprint for the model, if available.
 * @property serviceTier The service tier used by the upstream provider for this request.
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
    @SerialName("system_fingerprint")
    val systemFingerprint: String? = null,
    @SerialName("service_tier")
    val serviceTier: String? = null,
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
 * @property finishReason Why the model stopped generating ("stop", "length", "tool_calls", "content_filter", etc.). Nullable for streaming.
 * @property index The index of this choice in the list of choices.
 * @property message The generated message from the model.
 * @property logprobs Log probability information for the choice, if requested.
 *
 * @see ChatCompletionResponse
 * @see Message
 */
@Serializable
public data class Choice(
    @SerialName("finish_reason")
    val finishReason: String?,
    @SerialName("index")
    val index: Int,
    @SerialName("message")
    val message: Message,
    @SerialName("logprobs")
    val logprobs: JsonElement? = null,
    @SerialName("error")
    val error: ErrorBody? = null
)
