package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.l1.chat.JsonSchemaConfig
import dev.toliner.openrouter.l1.chat.ResponseFormat
import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.schema.generator.json.JsonSchemaConfig as KotlinxJsonSchemaConfig
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator
import kotlinx.schema.json.encodeToJsonObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

/**
 * DSL builder for constructing [ResponseFormat] instances.
 *
 * Provides convenient methods for each response format variant, including
 * automatic JSON Schema generation from `@Serializable` Kotlin types.
 *
 * @see ResponseFormat
 */
@OpenRouterDslMarker
public class ResponseFormatBuilder {
    private var result: ResponseFormat? = null

    /**
     * Sets the response format to plain text output.
     */
    public fun text() {
        result = ResponseFormat.Text
    }

    /**
     * Sets the response format to JSON object mode.
     */
    public fun jsonObject() {
        result = ResponseFormat.JsonObject
    }

    /**
     * Sets the response format to JSON Schema mode with a schema auto-generated from the
     * given `@Serializable` type [T].
     *
     * The schema is generated at runtime using the type's [kotlinx.serialization.descriptors.SerialDescriptor],
     * with strict mode settings suitable for structured output (all fields required, no additional properties).
     *
     * Example:
     * ```kotlin
     * @Serializable
     * data class UserInfo(val name: String, val age: Int)
     *
     * chatRequest {
     *     model = "openai/gpt-4o"
     *     userMessage("Extract user info")
     *     responseFormat {
     *         jsonSchema<UserInfo>("user_info")
     *     }
     * }
     * ```
     *
     * @param T The `@Serializable` type to generate the schema from.
     * @param name The schema name (must match `[a-zA-Z0-9_-]`, max 64 chars).
     * @param description Optional description of the schema for the model.
     * @param strict Whether to enable strict schema adherence. Defaults to `true`.
     */
    public inline fun <reified T> jsonSchema(
        name: String,
        description: String? = null,
        strict: Boolean? = true
    ) {
        jsonSchema(name, serializer<T>(), description, strict)
    }

    /**
     * Sets the response format to JSON Schema mode with a schema auto-generated from the
     * given serializer's descriptor.
     *
     * @param name The schema name (must match `[a-zA-Z0-9_-]`, max 64 chars).
     * @param serializer The [KSerializer] whose descriptor will be used to generate the schema.
     * @param description Optional description of the schema for the model.
     * @param strict Whether to enable strict schema adherence. Defaults to `true`.
     */
    public fun <T> jsonSchema(
        name: String,
        serializer: KSerializer<T>,
        description: String? = null,
        strict: Boolean? = true
    ) {
        val generator = SerializationClassJsonSchemaGenerator(
            json = Json.Default,
            jsonSchemaConfig = KotlinxJsonSchemaConfig.Strict,
        )
        val generatedSchema = generator.generateSchema(serializer.descriptor)
        val schemaJsonObject = generatedSchema.encodeToJsonObject()

        result = ResponseFormat.JsonSchema(
            jsonSchema = JsonSchemaConfig(
                name = name,
                description = description,
                schema = schemaJsonObject,
                strict = strict
            )
        )
    }

    /**
     * Sets the response format to JSON Schema mode with a manually specified schema.
     *
     * Use this when you need fine-grained control over the JSON Schema definition,
     * such as complex validation constraints or patterns not expressible through
     * `@Serializable` data classes.
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4o"
     *     userMessage("Extract data")
     *     responseFormat {
     *         jsonSchema("my_schema") {
     *             description = "Custom schema"
     *             strict = true
     *             schema = buildJsonObject {
     *                 put("type", "object")
     *                 putJsonObject("properties") {
     *                     putJsonObject("name") { put("type", "string") }
     *                 }
     *                 putJsonArray("required") { add("name") }
     *                 put("additionalProperties", false)
     *             }
     *         }
     *     }
     * }
     * ```
     *
     * @param name The schema name (must match `[a-zA-Z0-9_-]`, max 64 chars).
     * @param block Configuration block for [JsonSchemaConfigBuilder].
     */
    public fun jsonSchema(name: String, block: JsonSchemaConfigBuilder.() -> Unit) {
        val config = JsonSchemaConfigBuilder(name).apply(block).build()
        result = ResponseFormat.JsonSchema(jsonSchema = config)
    }

    /**
     * Sets the response format to GBNF grammar-constrained mode (OpenRouter-specific).
     *
     * @param grammar The GBNF grammar string.
     */
    public fun grammar(grammar: String) {
        result = ResponseFormat.Grammar(grammar = grammar)
    }

    /**
     * Sets the response format to Python type-constrained mode (OpenRouter-specific).
     *
     * @param python The Python type definition string.
     */
    public fun python(python: String) {
        result = ResponseFormat.Python(python = python)
    }

    internal fun build(): ResponseFormat {
        return requireNotNull(result) { "responseFormat must specify a format (text(), jsonObject(), jsonSchema(), etc.)" }
    }
}

/**
 * DSL builder for manually constructing [JsonSchemaConfig] instances.
 *
 * @see ResponseFormatBuilder.jsonSchema
 */
@OpenRouterDslMarker
public class JsonSchemaConfigBuilder(private val name: String) {
    /**
     * Optional description of the schema for the model.
     */
    public var description: String? = null

    /**
     * The JSON Schema object defining the output structure.
     */
    public var schema: JsonObject? = null

    /**
     * Whether to enable strict schema adherence.
     */
    public var strict: Boolean? = null

    internal fun build(): JsonSchemaConfig {
        return JsonSchemaConfig(
            name = name,
            description = description,
            schema = schema,
            strict = strict
        )
    }
}
