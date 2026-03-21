package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionResponse(
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

@Serializable
data class Choice(
    @SerialName("index")
    val index: Int,
    @SerialName("message")
    val message: Message,
    @SerialName("finish_reason")
    val finishReason: String? = null
)
