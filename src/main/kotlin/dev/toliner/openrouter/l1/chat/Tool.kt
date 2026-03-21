package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FunctionTool(
    @SerialName("type")
    val type: String,
    @SerialName("function")
    val function: FunctionDefinition
)

@Serializable
data class FunctionDefinition(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("parameters")
    val parameters: JsonElement? = null
)
