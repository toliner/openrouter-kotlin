package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.models.EmbeddingModel
import dev.toliner.openrouter.l1.models.ModelEndpoint
import dev.toliner.openrouter.l1.models.ModelList
import dev.toliner.openrouter.l1.models.ModelsCount
import dev.toliner.openrouter.l1.models.ZdrEndpoint
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class ModelsApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun list(): ModelList = getAndDecode("${config.baseUrl}/models")

    suspend fun count(): ModelsCount = getAndDecode("${config.baseUrl}/models/count")

    suspend fun userModels(): ModelList = getAndDecode("${config.baseUrl}/models/user")

    suspend fun endpoints(): List<ModelEndpoint> = getAndDecode("${config.baseUrl}/models/endpoints")

    suspend fun zdrEndpoints(): List<ZdrEndpoint> = getAndDecode("${config.baseUrl}/models/endpoints/zdr")

    suspend fun embeddingModels(): List<EmbeddingModel> = getAndDecode("${config.baseUrl}/models/embedding")

    private suspend inline fun <reified T> getAndDecode(url: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
        }
        return response.decodeBodyOrThrow()
    }
}
