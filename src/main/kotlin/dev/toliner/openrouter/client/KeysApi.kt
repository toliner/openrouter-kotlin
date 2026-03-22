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

class KeysApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun list(): List<ApiKey> = getAndDecode("${config.baseUrl}/keys")

    suspend fun create(request: CreateKeyRequest): ApiKey {
        val response = httpClient.post("${config.baseUrl}/keys") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    suspend fun get(hash: String): ApiKey = getAndDecode("${config.baseUrl}/keys/$hash")

    suspend fun update(hash: String, request: UpdateKeyRequest): ApiKey {
        val response = httpClient.patch("${config.baseUrl}/keys/$hash") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    suspend fun delete(hash: String) {
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
