package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.generation.Generation
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GenerationApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun get(id: String): Generation = getAndDecode("${config.baseUrl}/generation", id)

    private suspend inline fun <reified T> getAndDecode(url: String, id: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
            parameter("id", id)
        }
        return response.decodeBodyOrThrow()
    }
}
