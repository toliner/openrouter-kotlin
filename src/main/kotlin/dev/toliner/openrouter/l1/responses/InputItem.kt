package dev.toliner.openrouter.l1.responses

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Input item for structured input in the OpenRouter Responses API.
 *
 * Input items can be either messages (user/assistant messages) or function call outputs.
 * This is an experimental API that mirrors OpenAI's responses endpoint.
 *
 * **Note:** This API is experimental and subject to change. It is marked with [ExperimentalOpenRouterApi].
 *
 * @see Message
 * @see FunctionCallOutput
 * @see ResponseInput.Items
 */
@ExperimentalOpenRouterApi
@Serializable
public sealed class InputItem {
    /**
     * A message input item (user or assistant message).
     *
     * **Note:** This API is experimental and subject to change. It is marked with [ExperimentalOpenRouterApi].
     *
     * @property role Role of the message sender (e.g., "user", "assistant")
     * @property content Text content of the message
     */
    @Serializable
    @SerialName("message")
    public data class Message(
        @SerialName("role")
        val role: String,
        @SerialName("content")
        val content: String
    ) : InputItem()
    
    /**
     * A function call output input item.
     *
     * Represents the output of a function call that was previously made by the model,
     * used to continue the conversation with the function's result.
     *
     * **Note:** This API is experimental and subject to change. It is marked with [ExperimentalOpenRouterApi].
     *
     * @property callId Identifier of the function call this output corresponds to
     * @property output JSON-encoded string containing the function's output
     */
    @Serializable
    @SerialName("function_call_output")
    public data class FunctionCallOutput(
        @SerialName("call_id")
        val callId: String,
        @SerialName("output")
        val output: String
    ) : InputItem()
}
