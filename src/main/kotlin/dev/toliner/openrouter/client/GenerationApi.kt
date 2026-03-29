package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.generation.Generation
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * API for retrieving generation statistics.
 *
 * This API provides methods for querying detailed statistics about specific generation requests,
 * including token usage, cost, and timing information.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.generation].
 *
 * @see Generation
 */
public class GenerationApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Retrieves statistics for a specific generation.
     *
     * Calls the `/generation` endpoint with the provided generation ID to retrieve
     * detailed information about token usage, cost, and performance metrics.
     *
     * @param id The generation ID to query (obtained from completion responses).
     * @return A [Generation] object containing statistics for the specified generation.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the generation is not found.
     * @see Generation
     */
    public suspend fun get(id: String): Generation = getAndDecode("${config.baseUrl}/generation", id)

    private suspend inline fun <reified T> getAndDecode(url: String, id: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
            parameter("id", id)
        }
        return response.decodeBodyOrThrow()
    }
}
