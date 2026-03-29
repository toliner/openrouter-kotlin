package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.providers.Provider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API for listing model providers.
 *
 * This API provides methods for querying information about the providers that
 * supply models on the OpenRouter platform.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.providers].
 *
 * @see Provider
 */
public class ProvidersApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Lists all available providers.
     *
     * Calls the `/providers` endpoint to retrieve information about all model
     * providers available on OpenRouter, including their capabilities and status.
     *
     * @return A list of [Provider] objects.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see Provider
     */
    public suspend fun list(): List<Provider> {
        val response: ProvidersResponse = getAndDecode("${config.baseUrl}/providers")
        return response.data
    }

    private suspend inline fun <reified T> getAndDecode(url: String): T {
        val response = httpClient.get(url) { applyOpenRouterHeaders(config) }
        return response.decodeBodyOrThrow()
    }
}

@Serializable
private data class ProvidersResponse(
    @SerialName("data") val data: List<Provider>
)
