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

@Serializable(with = MessageSerializer::class)
sealed class Message {
    abstract val role: String
    
    @Serializable
    data class System(
        @SerialName("content")
        val content: String
    ) : Message() {
        @SerialName("role")
        override val role: String = "system"
    }
    
    @Serializable
    data class User(
        @SerialName("content")
        val content: Content
    ) : Message() {
        @SerialName("role")
        override val role: String = "user"
    }
    
    @Serializable
    data class Assistant(
        @SerialName("content")
        val content: String?,
        @SerialName("tool_calls")
        val toolCalls: List<ToolCall>? = null
    ) : Message() {
        @SerialName("role")
        override val role: String = "assistant"
    }
    
    @Serializable
    data class Tool(
        @SerialName("tool_call_id")
        val toolCallId: String,
        @SerialName("content")
        val content: String
    ) : Message() {
        @SerialName("role")
        override val role: String = "tool"
    }
}

object MessageSerializer : KSerializer<Message> {
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

@Serializable
data class ToolCall(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String,
    @SerialName("function")
    val function: FunctionCall
)

@Serializable
data class FunctionCall(
    @SerialName("name")
    val name: String,
    @SerialName("arguments")
    val arguments: String
)
