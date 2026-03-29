package dev.toliner.openrouter.l1.responses

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Tool (function) definition for the OpenRouter Responses API.
 *
 * Defines a function that the model can call during response generation. This is an experimental
 * API that mirrors OpenAI's function calling capabilities.
 *
 * **Note:** This API is experimental and subject to change. It is marked with [ExperimentalOpenRouterApi].
 *
 * @property type Type of tool (typically "function")
 * @property name Name of the function
 * @property description Optional human-readable description of what the function does
 * @property parameters Optional JSON schema object defining the function's parameters
 *
 * @see CreateResponseRequest
 * @see ResponseToolCall
 */
@ExperimentalOpenRouterApi
@Serializable
public data class ResponseTool(
    @SerialName("type")
    val type: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("parameters")
    val parameters: JsonObject? = null
)
