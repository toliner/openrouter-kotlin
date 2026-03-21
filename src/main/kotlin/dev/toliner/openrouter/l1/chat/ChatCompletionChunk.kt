package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionChunk(
    @SerialName("id")
    val id: String,
    @SerialName("model")
    val model: String,
    @SerialName("object")
    val objectType: String,
    @SerialName("created")
    val created: Long,
    @SerialName("choices")
    val choices: List<ChunkChoice>,
    @SerialName("usage")
    val usage: Usage? = null
)

@Serializable
data class ChunkChoice(
    @SerialName("index")
    val index: Int,
    @SerialName("delta")
    val delta: Delta,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class Delta(
    @SerialName("role")
    val role: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<DeltaToolCall>? = null
)

@Serializable
data class DeltaToolCall(
    @SerialName("index")
    val index: Int,
    @SerialName("id")
    val id: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("function")
    val function: DeltaFunction? = null
)

@Serializable
data class DeltaFunction(
    @SerialName("name")
    val name: String? = null,
    @SerialName("arguments")
    val arguments: String? = null
)
