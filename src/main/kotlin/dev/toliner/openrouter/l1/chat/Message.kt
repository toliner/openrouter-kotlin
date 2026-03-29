package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.Content
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Represents a message in a chat completion conversation.
 *
 * Messages in the OpenRouter Chat API have different structures depending on their role.
 * This sealed class hierarchy models all supported message types with role-specific fields.
 *
 * @property role The role of the message sender (system, user, assistant, or tool).
 *
 * @see ChatCompletionRequest
 * @see ChatCompletionResponse
 */
@Serializable(with = MessageSerializer::class)
public sealed class Message {
    public abstract val role: String
    
    /**
     * A system message that provides instructions or context to the model.
     *
     * System messages are used to set the behavior, personality, or context for the conversation.
     * They are typically placed at the beginning of the messages array.
     *
     * @property content The system instruction text.
     */
    @Serializable
    public data class System(
        @SerialName("content")
        val content: String
    ) : Message() {
        @SerialName("role")
        override val role: String = "system"
    }
    
    /**
     * A user message containing input from the end user.
     *
     * User messages can contain either plain text or multimodal content (text with images).
     * Use [Content.Text] for simple text messages or [Content.Parts] for multimodal input.
     *
     * @property content The user's message content, either text-only or multimodal.
     *
     * @see Content
     */
    @Serializable
    public data class User(
        @SerialName("content")
        val content: Content
    ) : Message() {
        @SerialName("role")
        override val role: String = "user"
    }
    
    /**
     * An assistant message representing the model's response.
     *
     * Assistant messages can contain either text content, tool calls, or both.
     * When the model decides to call tools, [toolCalls] will be populated and [content] may be null.
     *
     * @property content The assistant's text response, or null if only tool calls are present.
     * @property toolCalls List of tool calls requested by the model, if any.
     *
     * @see ToolCall
     */
    @Serializable
    public data class Assistant(
        @SerialName("content")
        val content: String?,
        @SerialName("tool_calls")
        val toolCalls: List<ToolCall>? = null
    ) : Message() {
        @SerialName("role")
        override val role: String = "assistant"
    }
    
    /**
     * A tool message containing the result of a tool call execution.
     *
     * After the model requests a tool call via [Assistant.toolCalls], the client must execute
     * the tool and send back the result using this message type. The [toolCallId] must match
     * the [ToolCall.id] from the assistant's message.
     *
     * @property toolCallId The ID of the tool call this message is responding to.
     * @property content The tool execution result as a string (typically JSON).
     *
     * @see ToolCall
     */
    @Serializable
    public data class Tool(
        @SerialName("tool_call_id")
        val toolCallId: String,
        @SerialName("content")
        val content: String
    ) : Message() {
        @SerialName("role")
        override val role: String = "tool"
    }
}

internal object MessageSerializer : KSerializer<Message> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Message")
    
    override fun deserialize(decoder: Decoder): Message {
        require(decoder is JsonDecoder) { "This serializer only works with Json format" }
        
        val element = decoder.decodeJsonElement().jsonObject
        val role = element["role"]?.jsonPrimitive?.content
            ?: error("Missing 'role' field in message")
        
        return when (role) {
            "system" -> {
                @Serializable
                data class SystemMessage(
                    @SerialName("content") val content: String
                )
                val msg = decoder.json.decodeFromJsonElement(SystemMessage.serializer(), element)
                Message.System(content = msg.content)
            }
            "user" -> {
                @Serializable
                data class UserMessage(
                    @SerialName("content") val content: Content
                )
                val msg = decoder.json.decodeFromJsonElement(UserMessage.serializer(), element)
                Message.User(content = msg.content)
            }
            "assistant" -> {
                @Serializable
                data class AssistantMessage(
                    @SerialName("content") val content: String?,
                    @SerialName("tool_calls") val toolCalls: List<ToolCall>? = null
                )
                val msg = decoder.json.decodeFromJsonElement(AssistantMessage.serializer(), element)
                Message.Assistant(content = msg.content, toolCalls = msg.toolCalls)
            }
            "tool" -> {
                @Serializable
                data class ToolMessage(
                    @SerialName("tool_call_id") val toolCallId: String,
                    @SerialName("content") val content: String
                )
                val msg = decoder.json.decodeFromJsonElement(ToolMessage.serializer(), element)
                Message.Tool(toolCallId = msg.toolCallId, content = msg.content)
            }
            else -> error("Unknown role: $role")
        }
    }
    
    override fun serialize(encoder: Encoder, value: Message) {
        require(encoder is JsonEncoder) { "This serializer only works with Json format" }
        
        val jsonObject = when (value) {
            is Message.System -> buildJsonObject {
                put("role", "system")
                put("content", value.content)
            }
            is Message.User -> {
                // Manually serialize Content to avoid nested encoder issues
                val contentElement = when (val content = value.content) {
                    is Content.Text -> JsonPrimitive(content.value)
                    is Content.Parts -> {
                        encoder.json.encodeToJsonElement(
                            kotlinx.serialization.builtins.ListSerializer(
                                dev.toliner.openrouter.serialization.ContentPart.serializer()
                            ),
                            content.parts
                        )
                    }
                }
                buildJsonObject {
                    put("role", "user")
                    put("content", contentElement)
                }
            }
            is Message.Assistant -> buildJsonObject {
                put("role", "assistant")
                if (value.content != null) {
                    put("content", value.content)
                }
                if (value.toolCalls != null) {
                    put("tool_calls", encoder.json.encodeToJsonElement(value.toolCalls))
                }
            }
            is Message.Tool -> buildJsonObject {
                put("role", "tool")
                put("tool_call_id", value.toolCallId)
                put("content", value.content)
            }
        }
        
        encoder.encodeJsonElement(jsonObject)
    }
}

/**
 * Represents a tool call requested by the model in an assistant message.
 *
 * When the model decides to use a tool, it returns a tool call object specifying which
 * function to invoke and with what arguments. The client must execute the function and
 * return the result via a [Message.Tool].
 *
 * @property id Unique identifier for this tool call, used to associate responses.
 * @property type The type of tool call, typically "function".
 * @property function Details of the function to call.
 *
 * @see Message.Assistant
 * @see Message.Tool
 * @see FunctionCall
 */
@Serializable
public data class ToolCall(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String,
    @SerialName("function")
    val function: FunctionCall
)

/**
 * Represents the function details in a tool call.
 *
 * Contains the function name and arguments as a JSON string. The client must parse
 * the arguments JSON and execute the corresponding function.
 *
 * @property name The name of the function to call.
 * @property arguments The function arguments as a JSON-encoded string.
 *
 * @see ToolCall
 */
@Serializable
public data class FunctionCall(
    @SerialName("name")
    val name: String,
    @SerialName("arguments")
    val arguments: String
)
