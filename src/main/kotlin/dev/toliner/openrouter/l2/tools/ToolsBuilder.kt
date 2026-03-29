package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l1.chat.FunctionTool
import dev.toliner.openrouter.l2.OpenRouterDslMarker

/**
 * Fluent DSL builder for configuring function tools in a chat completion request.
 *
 * This builder allows you to define one or more function tools that the model can call
 * during generation. Each tool is defined using the [function] method with a nested DSL
 * for specifying the function's metadata and parameters schema.
 *
 * Example usage:
 * ```kotlin
 * chatRequest {
 *     model = "openai/gpt-4"
 *     userMessage("What's the weather in Tokyo and Paris?")
 *     tools {
 *         function("get_weather") {
 *             description = "Get the current weather in a given location"
 *             parameters {
 *                 property("location", "string") {
 *                     description = "The city name"
 *                 }
 *                 property("unit", "string") {
 *                     description = "Temperature unit (celsius/fahrenheit)"
 *                 }
 *                 required("location")
 *             }
 *         }
 *         function("get_time") {
 *             description = "Get the current time in a timezone"
 *             parameters {
 *                 property("timezone", "string")
 *                 required("timezone")
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @see FunctionTool for the underlying l1 data model
 * @see FunctionToolBuilder for function configuration options
 */
@OpenRouterDslMarker
public class ToolsBuilder {
    private val tools = mutableListOf<FunctionTool>()
    
    /**
     * Defines a function tool with the given name.
     *
     * Function tools represent callable functions that the model can invoke during generation.
     * The model will provide arguments based on the parameter schema, and you can execute
     * the function and return results using toolMessage().
     *
     * Example:
     * ```kotlin
     * tools {
     *     function("calculate") {
     *         description = "Perform a mathematical calculation"
     *         parameters {
     *             property("expression", "string") {
     *                 description = "The mathematical expression to evaluate"
     *             }
     *             required("expression")
     *         }
     *     }
     * }
     * ```
     *
     * @param name The unique identifier for this function tool
     * @param block Configuration block executed in the context of [FunctionToolBuilder]
     * @see FunctionToolBuilder for available configuration options
     * @see FunctionTool for the generated data structure
     */
    public fun function(name: String, block: FunctionToolBuilder.() -> Unit) {
        tools.add(FunctionToolBuilder(name).apply(block).build())
    }
    
    internal fun build(): List<FunctionTool> = tools.toList()
}
