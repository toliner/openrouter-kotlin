package dev.toliner.openrouter.l1.embeddings

import dev.toliner.openrouter.l1.chat.Usage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing generated text embeddings.
 *
 * Returned by the OpenRouter `/embeddings` endpoint. Contains the embedding vectors
 * for the provided input text(s), along with metadata about token usage.
 *
 * @property data List of embedding objects, one per input text. Order matches the input order.
 * @property model The identifier of the model that generated these embeddings.
 * @property usage Optional token usage statistics for this embedding request.
 *
 * @see EmbeddingData
 * @see EmbeddingRequest
 * @see Usage
 */
@Serializable
public data class EmbeddingResponse(
    @SerialName("data")
    val data: List<EmbeddingData>,
    @SerialName("model")
    val model: String,
    @SerialName("usage")
    val usage: Usage? = null
)
