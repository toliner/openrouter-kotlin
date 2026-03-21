package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.embeddings.EmbeddingRequest
import dev.toliner.openrouter.l1.embeddings.EmbeddingResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class EmbeddingsApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun create(request: EmbeddingRequest): EmbeddingResponse {
        val response = httpClient.post("${config.baseUrl}/embeddings") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }
}
