package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Fluent DSL builder for configuring individual JSON Schema properties.
 *
 * This builder allows you to configure metadata for a single property in a JSON Schema,
 * such as its description. The property type is set at construction time and cannot be
 * changed.
 *
 * Example usage:
 * ```kotlin
 * property("email", "string") {
 *     description = "User's email address in RFC 5322 format"
 * }
 * property("retry_count", "integer") {
 *     description = "Number of retry attempts (0-10)"
 * }
 * ```
 *
 * @param type The JSON Schema type for this property (e.g., "string", "integer", "boolean")
 * @see JsonSchemaBuilder.property for defining properties in a schema
 */
@OpenRouterDslMarker
public class PropertyBuilder(private val type: String) {
    /**
     * A human-readable description of this property.
     *
     * This description helps the model understand what the parameter is for and what
     * values are expected. Be specific about format, constraints, and examples.
     *
     * Examples:
     * - "The city and state, e.g. San Francisco, CA"
     * - "Temperature unit: 'celsius' or 'fahrenheit'"
     * - "Maximum number of results to return (1-100)"
     */
    public var description: String? = null
    
    internal fun build(): JsonObject {
        return buildJsonObject {
            put("type", type)
            description?.let { put("description", it) }
        }
    }
}
