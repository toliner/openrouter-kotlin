package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.keys.ApiKey
import dev.toliner.openrouter.l1.keys.CreateKeyRequest
import dev.toliner.openrouter.l1.keys.UpdateKeyRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API for managing API keys.
 *
 * This API provides methods for creating, listing, updating, and deleting API keys
 * associated with your OpenRouter account.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.keys].
 *
 * @see ApiKey
 * @see CreateKeyRequest
 * @see UpdateKeyRequest
 */
public class KeysApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Lists all API keys for the account.
     *
     * Calls the `/keys` endpoint to retrieve all API keys associated with the
     * authenticated account.
     *
     * @return A list of [ApiKey] objects.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see ApiKey
     */
    public suspend fun list(): List<ApiKey> = getAndDecode("${config.baseUrl}/keys")

    /**
     * Creates a new API key.
     *
     * Calls the `/keys` endpoint with a POST request to create a new API key
     * with the specified configuration.
     *
     * @param request The key creation request containing name, permissions, and limits.
     * @return The newly created [ApiKey].
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see CreateKeyRequest
     * @see ApiKey
     */
    public suspend fun create(request: CreateKeyRequest): ApiKey {
        val response = httpClient.post("${config.baseUrl}/keys") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    /**
     * Retrieves details for a specific API key.
     *
     * Calls the `/keys/{hash}` endpoint to get information about a specific API key
     * identified by its hash.
     *
     * @param hash The hash identifier of the API key.
     * @return The [ApiKey] details.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the key is not found.
     * @see ApiKey
     */
    public suspend fun get(hash: String): ApiKey = getAndDecode("${config.baseUrl}/keys/$hash")

    /**
     * Updates an existing API key.
     *
     * Calls the `/keys/{hash}` endpoint with a PATCH request to update the
     * configuration of an existing API key.
     *
     * @param hash The hash identifier of the API key to update.
     * @param request The update request containing modified key settings.
     * @return The updated [ApiKey].
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the key is not found.
     * @see UpdateKeyRequest
     * @see ApiKey
     */
    public suspend fun update(hash: String, request: UpdateKeyRequest): ApiKey {
        val response = httpClient.patch("${config.baseUrl}/keys/$hash") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    /**
     * Deletes an API key.
     *
     * Calls the `/keys/{hash}` endpoint with a DELETE request to permanently
     * remove the specified API key.
     *
     * @param hash The hash identifier of the API key to delete.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the key is not found.
     */
    public suspend fun delete(hash: String) {
        val response = httpClient.delete("${config.baseUrl}/keys/$hash") {
            applyOpenRouterHeaders(config)
        }
        response.throwIfErrorStatus()
    }

    private suspend inline fun <reified T> getAndDecode(url: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
        }
        return response.decodeBodyOrThrow()
    }
}
