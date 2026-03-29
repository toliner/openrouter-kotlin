package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.error.ErrorBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a chunk in a streaming chat completion response.
 *
 * When streaming is enabled (`stream = true`), the OpenRouter API returns a series
 * of server-sent events (SSE), each containing a ChatCompletionChunk. These chunks
 * contain incremental updates ([Delta]) rather than complete messages.
 *
 * @property id Unique identifier for this completion (same across all chunks in a stream).
 * @property model The model used to generate the completion.
 * @property objectType The object type, always "chat.completion.chunk" for streaming responses.
 * @property created Unix timestamp (seconds) of when the completion was created.
 * @property choices List of delta choices. Each chunk typically contains one choice.
 * @property usage Token usage statistics, typically only present in the final chunk.
 *
 * @see ChatCompletionRequest
 * @see ChatCompletionResponse
 * @see ChunkChoice
 * @see Delta
 */
@Serializable
public data class ChatCompletionChunk(
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

/**
 * Represents a single delta choice in a streaming chunk.
 *
 * Unlike [Choice] in non-streaming responses, ChunkChoice contains a [Delta]
 * representing an incremental update rather than a complete message.
 *
 * @property index The index of this choice in the list of choices.
 * @property delta The incremental content update for this chunk.
 * @property finishReason Why the model stopped generating. Only present in the final chunk ("stop", "length", etc.).
 * @property error Error information if this specific choice encountered an error.
 *
 * @see ChatCompletionChunk
 * @see Delta
 * @see Choice
 */
@Serializable
public data class ChunkChoice(
    @SerialName("index")
    val index: Int,
    @SerialName("delta")
    val delta: Delta,
    @SerialName("finish_reason")
    val finishReason: String? = null,
    @SerialName("error")
    val error: ErrorBody? = null
)

/**
 * Represents an incremental content update in a streaming response.
 *
 * Deltas contain partial information that must be accumulated across chunks to
 * reconstruct the complete message. The first chunk typically contains the role,
 * subsequent chunks contain content fragments, and tool calls are sent incrementally.
 *
 * @property role The role of the message sender. Only present in the first chunk ("assistant").
 * @property content Incremental text content. Concatenate across chunks to build the full message.
 * @property toolCalls Incremental tool call updates. Tool calls are built up across multiple chunks.
 *
 * @see ChunkChoice
 * @see DeltaToolCall
 */
@Serializable
public data class Delta(
    @SerialName("role")
    val role: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<DeltaToolCall>? = null
)

/**
 * Represents an incremental tool call update in a streaming delta.
 *
 * Tool calls are sent incrementally across multiple chunks. The first chunk for a tool
 * call contains the ID and type, subsequent chunks contain function name and argument
 * fragments. Use the [index] to match chunks belonging to the same tool call.
 *
 * @property index The index of this tool call in the array of tool calls for this message.
 * @property id Unique identifier for this tool call. Only present in the first chunk.
 * @property type The type of tool call ("function"). Only present in the first chunk.
 * @property function Incremental function details. Name and arguments are sent across chunks.
 *
 * @see Delta
 * @see DeltaFunction
 * @see ToolCall
 */
@Serializable
public data class DeltaToolCall(
    @SerialName("index")
    val index: Int,
    @SerialName("id")
    val id: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("function")
    val function: DeltaFunction? = null
)

/**
 * Represents incremental function details in a streaming tool call.
 *
 * Function name is sent in one chunk, then arguments are sent as JSON fragments
 * across subsequent chunks. Concatenate all [arguments] strings to build the
 * complete JSON arguments.
 *
 * @property name The name of the function to call. Only present in the first relevant chunk.
 * @property arguments Incremental JSON argument string. Concatenate across chunks to build complete JSON.
 *
 * @see DeltaToolCall
 * @see FunctionCall
 */
@Serializable
public data class DeltaFunction(
    @SerialName("name")
    val name: String? = null,
    @SerialName("arguments")
    val arguments: String? = null
)
