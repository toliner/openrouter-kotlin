package dev.toliner.openrouter.l1.embeddings

import dev.toliner.openrouter.serialization.StringOrArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request parameters for generating text embeddings.
 *
 * Submitted to the OpenRouter `/embeddings` endpoint to convert text into dense vector representations.
 * The input can be a single string or an array of strings for batch processing.
 *
 * @property model The identifier of the embedding model to use (e.g., "openai/text-embedding-ada-002").
 * @property input The text input(s) to embed, either a single string or an array of strings.
 * @property encodingFormat Optional format for the returned embeddings (e.g., "float", "base64").
 *
 * @see EmbeddingResponse
 * @see dev.toliner.openrouter.l1.models.EmbeddingModel
 */
@Serializable
public data class EmbeddingRequest(
    @SerialName("model")
    val model: String,
    @SerialName("input")
    val input: StringOrArray,
    @SerialName("encoding_format")
    val encodingFormat: String? = null
)
