package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l1.chat.FunctionDefinition
import dev.toliner.openrouter.l1.chat.FunctionTool
import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.serialization.json.JsonObject

/**
 * Fluent DSL builder for configuring a single function tool.
 *
 * This builder configures the metadata and parameter schema for a function that the model
 * can call. The function name is provided at construction time, and this builder allows
 * setting the description and defining the JSON Schema for the function's parameters.
 *
 * Example usage:
 * ```kotlin
 * function("search_database") {
 *     description = "Search the product database with filters"
 *     parameters {
 *         property("query", "string") {
 *             description = "The search query"
 *         }
 *         property("category", "string") {
 *             description = "Product category filter"
 *         }
 *         property("max_results", "integer") {
 *             description = "Maximum number of results to return"
 *         }
 *         required("query")
 *     }
 * }
 * ```
 *
 * @param name The unique name identifying this function tool
 * @see FunctionTool for the underlying l1 data model
 * @see FunctionDefinition for the function definition structure
 * @see JsonSchemaBuilder for parameter schema configuration
 */
@OpenRouterDslMarker
public class FunctionToolBuilder(private val name: String) {
    /**
     * A human-readable description of what the function does.
     *
     * This description helps the model understand when to call the function and what it's for.
     * Be clear and specific about the function's purpose, inputs, and outputs.
     *
     * Example: "Get the current weather in a given location. Returns temperature, conditions, and humidity."
     */
    public var description: String? = null
    private var parametersSchema: JsonObject? = null
    
    /**
     * Defines the JSON Schema for the function's parameters using a DSL builder.
     *
     * The parameters schema describes the structure, types, and constraints of the function's
     * input arguments. Use [property] to define individual parameters and [required] to mark
     * which ones are mandatory.
     *
     * Example:
     * ```kotlin
     * parameters {
     *     property("user_id", "string") {
     *         description = "The unique identifier for the user"
     *     }
     *     property("include_metadata", "boolean") {
     *         description = "Whether to include user metadata in the response"
     *     }
     *     required("user_id")
     * }
     * ```
     *
     * @param block Configuration block executed in the context of [JsonSchemaBuilder]
     * @see JsonSchemaBuilder for schema configuration options
     */
    public fun parameters(block: JsonSchemaBuilder.() -> Unit) {
        parametersSchema = JsonSchemaBuilder().apply(block).build()
    }
    
    internal fun build(): FunctionTool {
        return FunctionTool(
            type = "function",
            function = FunctionDefinition(
                name = name,
                description = description,
                parameters = parametersSchema
            )
        )
    }
}
