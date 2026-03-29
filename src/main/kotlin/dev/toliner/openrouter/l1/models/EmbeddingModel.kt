package dev.toliner.openrouter.l1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an embedding model available through the OpenRouter API.
 *
 * Embedding models convert text into dense vector representations useful for semantic search,
 * similarity comparison, and other NLP tasks. This is a simplified model type focused on
 * embedding-specific capabilities.
 *
 * @property id Unique identifier for the embedding model (e.g., "openai/text-embedding-ada-002").
 * @property name Human-readable display name of the embedding model.
 * @property pricing Cost structure for using this embedding model.
 * @property contextLength Maximum number of tokens the model can process in a single embedding request.
 *
 * @see Pricing
 * @see dev.toliner.openrouter.l1.embeddings.EmbeddingRequest
 */
@Serializable
public data class EmbeddingModel(
    val id: String,
    val name: String,
    val pricing: Pricing,
    @SerialName("context_length")
    val contextLength: Int
)
