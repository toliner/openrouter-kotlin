package dev.toliner.openrouter.l1.responses

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ExperimentalOpenRouterApi
@Serializable
sealed class InputItem {
    @Serializable
    @SerialName("message")
    data class Message(
        @SerialName("role")
        val role: String,
        @SerialName("content")
        val content: String
    ) : InputItem()
    
    @Serializable
    @SerialName("function_call_output")
    data class FunctionCallOutput(
        @SerialName("call_id")
        val callId: String,
        @SerialName("output")
        val output: String
    ) : InputItem()
}
