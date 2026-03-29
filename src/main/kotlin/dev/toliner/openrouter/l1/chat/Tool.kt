package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents a tool (function) that can be called by the model.
 *
 * Tools enable the model to request external function calls during generation.
 * When provided in a [ChatCompletionRequest], the model can decide to invoke these
 * functions and will return [ToolCall] objects in the response.
 *
 * @property type The type of tool. Currently only "function" is supported.
 * @property function The function definition including name, description, and parameter schema.
 *
 * @see ChatCompletionRequest.tools
 * @see FunctionDefinition
 * @see ToolCall
 */
@Serializable
public data class FunctionTool(
    @SerialName("type")
    val type: String,
    @SerialName("function")
    val function: FunctionDefinition
)

/**
 * Represents the definition of a function that can be called by the model.
 *
 * The function definition includes a name, optional description, and a JSON Schema
 * describing the expected parameters. The model uses this information to decide
 * when and how to call the function.
 *
 * @property name The name of the function. Must be unique within the tools array.
 * @property description A description of what the function does. Helps the model decide when to use it.
 * @property parameters JSON Schema (as JsonElement) describing the function parameters.
 *   Should be an object schema with properties for each parameter.
 *
 * @see FunctionTool
 */
@Serializable
public data class FunctionDefinition(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("parameters")
    val parameters: JsonElement? = null
)
