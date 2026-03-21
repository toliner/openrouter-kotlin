package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.StringOrArray
import dev.toliner.openrouter.serialization.ToolChoice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatCompletionRequest(
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
