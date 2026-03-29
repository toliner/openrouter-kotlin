package dev.toliner.openrouter.l1.embeddings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single embedding vector with metadata.
 *
 * Represents one text input's corresponding embedding vector. Multiple instances
 * appear in an [EmbeddingResponse] when batch processing multiple inputs.
 *
 * @property embedding The dense vector representation of the input text, as a list of floating-point numbers.
 * @property index Zero-based position of this embedding in the response, matching the input order.
 * @property object Type identifier for this object, typically "embedding".
 *
 * @see EmbeddingResponse
 */
@Serializable
public data class EmbeddingData(
    @SerialName("embedding")
    val embedding: List<Double>,
    @SerialName("index")
    val index: Int,
    @SerialName("object")
    val `object`: String
)
