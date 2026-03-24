package dev.toliner.openrouter.l1.responses

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ExperimentalOpenRouterApi
@Serializable
data class ResponseObject(
    @SerialName("id")
    val id: String,
    @SerialName("object")
    val objectType: String,
    @SerialName("created")
    val created: Long,
    @SerialName("model")
    val model: String,
    @SerialName("choices")
    val choices: List<ResponseChoice>
)

@ExperimentalOpenRouterApi
@Serializable
data class ResponseChoice(
    @SerialName("index")
    val index: Int,
    @SerialName("message")
    val message: ResponseMessage,
    @SerialName("finish_reason")
    val finishReason: String
)

@ExperimentalOpenRouterApi
@Serializable
data class ResponseMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String?,
    @SerialName("tool_calls")
    val toolCalls: List<ResponseToolCall>? = null
)

@ExperimentalOpenRouterApi
@Serializable
data class ResponseToolCall(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String,
    @SerialName("function")
    val function: ResponseFunctionCall
)

@ExperimentalOpenRouterApi
@Serializable
data class ResponseFunctionCall(
    @SerialName("name")
    val name: String,
    @SerialName("arguments")
    val arguments: String
)
