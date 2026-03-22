package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.auth.AuthCodeRequest
import dev.toliner.openrouter.l1.auth.AuthCodeResponse
import dev.toliner.openrouter.l1.auth.AuthKeyRequest
import dev.toliner.openrouter.l1.auth.AuthKeyResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun createAuthCode(request: AuthCodeRequest): AuthCodeResponse {
        val response = httpClient.post("${config.baseUrl}/auth/keys/code") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    suspend fun exchangeCode(request: AuthKeyRequest): AuthKeyResponse {
        val response = httpClient.post("${config.baseUrl}/auth/keys") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }
}
