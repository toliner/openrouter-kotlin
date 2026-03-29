package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.embeddings.EmbeddingRequest
import dev.toliner.openrouter.l1.embeddings.EmbeddingResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API for creating text embeddings.
 *
 * This API provides methods for generating vector embeddings from text using OpenRouter's
 * embedding models. Embeddings are useful for semantic search, clustering, and similarity comparisons.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.embeddings].
 *
 * @see EmbeddingRequest
 * @see EmbeddingResponse
 */
public class EmbeddingsApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Creates text embeddings for the given input.
     *
     * Calls the `/embeddings` endpoint to generate vector embeddings from text input.
     * The input can be a single string or an array of strings.
     *
     * @param request The embedding request containing model and input text.
     * @return An [EmbeddingResponse] containing the generated embeddings.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see EmbeddingRequest
     * @see EmbeddingResponse
     */
    public suspend fun create(request: EmbeddingRequest): EmbeddingResponse {
        val response = httpClient.post("${config.baseUrl}/embeddings") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }
}
