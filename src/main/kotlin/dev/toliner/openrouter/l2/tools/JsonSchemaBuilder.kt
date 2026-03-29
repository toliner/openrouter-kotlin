package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Fluent DSL builder for constructing JSON Schema objects for function tool parameters.
 *
 * This builder creates JSON Schema definitions that describe the structure and types of
 * function parameters. The generated schema is used by the model to understand what
 * arguments it should provide when calling a function.
 *
 * The builder generates schemas with `type: "object"` and supports defining properties
 * with various JSON Schema types (string, integer, boolean, array, object, etc.) and
 * marking fields as required.
 *
 * Example usage:
 * ```kotlin
 * parameters {
 *     property("name", "string") {
 *         description = "The user's full name"
 *     }
 *     property("age", "integer") {
 *         description = "The user's age in years"
 *     }
 *     property("email", "string") {
 *         description = "The user's email address"
 *     }
 *     property("subscribe", "boolean") {
 *         description = "Whether to subscribe to newsletter"
 *     }
 *     required("name", "email")
 * }
 * ```
 *
 * @see PropertyBuilder for individual property configuration
 * @see FunctionToolBuilder.parameters for usage in context
 */
@OpenRouterDslMarker
public class JsonSchemaBuilder {
    private val properties = mutableMapOf<String, JsonObject>()
    private val requiredFields = mutableListOf<String>()
    
    /**
     * Defines a property in the JSON Schema.
     *
     * Each property represents a parameter that can be passed to the function. The type
     * should be a valid JSON Schema type: "string", "integer", "number", "boolean",
     * "array", "object", or "null".
     *
     * Example:
     * ```kotlin
     * property("location", "string") {
     *     description = "The city and country, e.g. San Francisco, CA"
     * }
     * property("temperature_unit", "string") {
     *     description = "The unit for temperature (celsius or fahrenheit)"
     * }
     * property("days", "integer") {
     *     description = "Number of days to forecast (1-7)"
     * }
     * ```
     *
     * @param name The parameter name
     * @param type The JSON Schema type (e.g., "string", "integer", "boolean")
     * @param block Optional configuration block executed in the context of [PropertyBuilder]
     * @see PropertyBuilder for property-level configuration
     */
    public fun property(name: String, type: String, block: PropertyBuilder.() -> Unit = {}) {
        val prop = PropertyBuilder(type).apply(block).build()
        properties[name] = prop
    }
    
    /**
     * Marks one or more properties as required parameters.
     *
     * Required parameters must be provided when the model calls the function. Any parameters
     * not listed as required are considered optional.
     *
     * Example:
     * ```kotlin
     * property("query", "string")
     * property("limit", "integer")
     * property("offset", "integer")
     * required("query")  // Only query is required; limit and offset are optional
     * ```
     *
     * You can also mark multiple properties as required in a single call:
     * ```kotlin
     * required("username", "password", "email")
     * ```
     *
     * @param names The names of properties to mark as required
     */
    public fun required(vararg names: String) {
        requiredFields.addAll(names)
    }
    
    internal fun build(): JsonObject {
        return buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                properties.forEach { (name, schema) ->
                    put(name, schema)
                }
            })
            if (requiredFields.isNotEmpty()) {
                put("required", JsonArray(requiredFields.map { JsonPrimitive(it) }))
            }
        }
    }
}
