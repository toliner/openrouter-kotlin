package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.models.EmbeddingModel
import dev.toliner.openrouter.l1.models.ModelEndpoint
import dev.toliner.openrouter.l1.models.ModelList
import dev.toliner.openrouter.l1.models.ModelsCount
import dev.toliner.openrouter.l1.models.ZdrEndpoint
import io.ktor.client.HttpClient
import io.ktor.client.request.get

/**
 * API for querying available models and endpoints.
 *
 * This API provides methods for discovering and querying information about models available
 * on OpenRouter, including chat models, embedding models, and their associated endpoints.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.models].
 *
 * @see ModelList
 * @see ModelEndpoint
 * @see EmbeddingModel
 */
public class ModelsApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Lists all available models on OpenRouter.
     *
     * Calls the `/models` endpoint to retrieve a complete list of available models,
     * including their capabilities, pricing, and context limits.
     *
     * @return A [ModelList] containing all available models.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see ModelList
     */
    public suspend fun list(): ModelList = getAndDecode("${config.baseUrl}/models")

    /**
     * Retrieves the count of available models.
     *
     * Calls the `/models/count` endpoint to get statistics about model availability.
     *
     * @return A [ModelsCount] containing model count information.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see ModelsCount
     */
    public suspend fun count(): ModelsCount = getAndDecode("${config.baseUrl}/models/count")

    /**
     * Lists models available to the current user.
     *
     * Calls the `/models/user` endpoint to retrieve models accessible with the current API key,
     * including any custom or private models.
     *
     * @return A [ModelList] containing user-accessible models.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see ModelList
     */
    public suspend fun userModels(): ModelList = getAndDecode("${config.baseUrl}/models/user")

    /**
     * Lists all model endpoints.
     *
     * Calls the `/models/endpoints` endpoint to retrieve information about all available
     * model endpoints and their configurations.
     *
     * @return A list of [ModelEndpoint] objects.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see ModelEndpoint
     */
    public suspend fun endpoints(): List<ModelEndpoint> = getAndDecode("${config.baseUrl}/models/endpoints")

    /**
     * Lists Zero Data Retention (ZDR) endpoints.
     *
     * Calls the `/models/endpoints/zdr` endpoint to retrieve endpoints that guarantee
     * zero data retention for privacy-sensitive use cases.
     *
     * @return A list of [ZdrEndpoint] objects.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see ZdrEndpoint
     */
    public suspend fun zdrEndpoints(): List<ZdrEndpoint> = getAndDecode("${config.baseUrl}/models/endpoints/zdr")

    /**
     * Lists available embedding models.
     *
     * Calls the `/models/embedding` endpoint to retrieve models specifically designed
     * for generating text embeddings.
     *
     * @return A list of [EmbeddingModel] objects.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see EmbeddingModel
     */
    public suspend fun embeddingModels(): List<EmbeddingModel> = getAndDecode("${config.baseUrl}/models/embedding")

    private suspend inline fun <reified T> getAndDecode(url: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
        }
        return response.decodeBodyOrThrow()
    }
}
