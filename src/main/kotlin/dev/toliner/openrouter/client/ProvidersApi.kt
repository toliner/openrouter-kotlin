package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.providers.Provider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProvidersApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun list(): List<Provider> {
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
