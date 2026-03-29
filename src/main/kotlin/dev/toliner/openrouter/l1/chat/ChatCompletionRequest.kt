package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.StringOrArray
import dev.toliner.openrouter.serialization.ToolChoice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a request to the OpenRouter Chat Completions endpoint.
 *
 * This is the primary interface for generating chat completions via OpenRouter.
 * It mirrors the request structure of the `/chat/completions` API endpoint.
 *
 * @property model The ID of the model to use (e.g., "openai/gpt-4", "anthropic/claude-3-opus").
 * @property messages The conversation history as a list of messages.
 * @property temperature Controls randomness (0.0 to 2.0). Higher values make output more random. Defaults to 1.0.
 * @property maxTokens The maximum number of tokens to generate in the completion.
 * @property topP Nucleus sampling parameter (0.0 to 1.0). Alternative to temperature.
 * @property topK Top-k sampling parameter. Only sample from the top K tokens.
 * @property frequencyPenalty Penalizes tokens based on their frequency in the text so far (-2.0 to 2.0).
 * @property presencePenalty Penalizes tokens based on whether they appear in the text so far (-2.0 to 2.0).
 * @property repetitionPenalty Alternative repetition penalty (typically 0.0 to 2.0).
 * @property seed Random seed for deterministic sampling. Same seed should produce same output.
 * @property stop Sequence(s) where the model will stop generating. Can be a single string or array.
 * @property stream Whether to stream the response as server-sent events. Defaults to false.
 * @property tools List of tools (functions) the model can call.
 * @property toolChoice Controls which tool the model should use ("auto", "none", or specific tool).
 * @property responseFormat Specifies the format of the model's output (e.g., JSON mode).
 * @property provider Provider-specific preferences and routing options.
 * @property trace Tracing information for observability and debugging.
 * @property transforms List of transform IDs to apply to the request/response.
 * @property route Specific routing strategy (e.g., "fallback").
 * @property models List of fallback models to try if the primary model fails.
 *
 * @see ChatCompletionResponse
 * @see ChatCompletionChunk
 * @see Message
 * @see FunctionTool
 * @see ProviderPreferences
 * @see ResponseFormat
 */
@Serializable
public data class ChatCompletionRequest(
    @SerialName("model")
    val model: String,
    @SerialName("messages")
    val messages: List<Message>,
    @SerialName("temperature")
    val temperature: Double? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("top_k")
    val topK: Int? = null,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Double? = null,
    @SerialName("presence_penalty")
    val presencePenalty: Double? = null,
    @SerialName("repetition_penalty")
    val repetitionPenalty: Double? = null,
    @SerialName("seed")
    val seed: Int? = null,
    @SerialName("stop")
    val stop: StringOrArray? = null,
    @SerialName("stream")
    val stream: Boolean? = null,
    @SerialName("tools")
    val tools: List<FunctionTool>? = null,
    @SerialName("tool_choice")
    val toolChoice: ToolChoice? = null,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null,
    @SerialName("provider")
    val provider: ProviderPreferences? = null,
    @SerialName("trace")
    val trace: Trace? = null,
    @SerialName("transforms")
    val transforms: List<String>? = null,
    @SerialName("route")
    val route: String? = null,
    @SerialName("models")
    val models: List<String>? = null
)
