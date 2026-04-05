package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.ResponseFormatSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Specifies the format of the model's output.
 *
 * Response format constraints allow requesting specific output structures.
 * This is a union type that serializes to different JSON shapes depending on the variant.
 *
 * Common variants:
 * - [Text]: Standard text output (default). Serializes to `{"type":"text"}`.
 * - [JsonObject]: Model will return a valid JSON object. Serializes to `{"type":"json_object"}`.
 * - [JsonSchema]: Structured output with a JSON Schema definition. Serializes to
 *   `{"type":"json_schema","json_schema":{...}}`.
 * - [Grammar]: GBNF grammar-constrained output (OpenRouter-specific). Serializes to
 *   `{"type":"grammar","grammar":"..."}`.
 * - [Python]: Python type-constrained output (OpenRouter-specific). Serializes to
 *   `{"type":"python","python":"..."}`.
 *
 * Note: Not all models support all response format types.
 *
 * @see ChatCompletionRequest.responseFormat
 * @see JsonSchemaConfig
 */
@Serializable(with = ResponseFormatSerializer::class)
public sealed interface ResponseFormat {
    /**
     * Standard text output format (default).
     *
     * Serializes to `{"type":"text"}`.
     */
    public data object Text : ResponseFormat

    /**
     * JSON object output format.
     *
     * Instructs the model to return a valid JSON object.
     * Serializes to `{"type":"json_object"}`.
     */
    public data object JsonObject : ResponseFormat

    /**
     * Structured output with a JSON Schema definition.
     *
     * Instructs the model to return output conforming to the given JSON Schema.
     * Serializes to `{"type":"json_schema","json_schema":{...}}`.
     *
     * @property jsonSchema The JSON Schema configuration for structured output.
     * @see JsonSchemaConfig
     */
    @Serializable
    public data class JsonSchema(
        @SerialName("json_schema")
        val jsonSchema: JsonSchemaConfig
    ) : ResponseFormat

    /**
     * GBNF grammar-constrained output format (OpenRouter-specific).
     *
     * Instructs the model to return output conforming to the given GBNF grammar.
     * Serializes to `{"type":"grammar","grammar":"..."}`.
     *
     * @property grammar The GBNF grammar string.
     */
    @Serializable
    public data class Grammar(
        @SerialName("grammar")
        val grammar: String
    ) : ResponseFormat

    /**
     * Python type-constrained output format (OpenRouter-specific).
     *
     * Instructs the model to return output conforming to the given Python type definition.
     * Serializes to `{"type":"python","python":"..."}`.
     *
     * @property python The Python type definition string.
     */
    @Serializable
    public data class Python(
        @SerialName("python")
        val python: String
    ) : ResponseFormat
}

/**
 * Configuration for JSON Schema-based structured output.
 *
 * Defines the schema that the model's output must conform to. Used with
 * [ResponseFormat.JsonSchema].
 *
 * @property name The schema name. Must match `[a-zA-Z0-9_-]` and be at most 64 characters.
 * @property description Optional description of the schema for the model.
 * @property schema Optional JSON Schema object defining the output structure.
 *   When `null`, the model generates valid JSON without a specific schema constraint.
 * @property strict Optional flag to enable strict schema adherence.
 *   When `true`, the model will strictly follow the schema (e.g., no additional properties).
 *
 * @see ResponseFormat.JsonSchema
 */
@Serializable
public data class JsonSchemaConfig(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("schema")
    val schema: JsonObject? = null,
    @SerialName("strict")
    val strict: Boolean? = null
)
