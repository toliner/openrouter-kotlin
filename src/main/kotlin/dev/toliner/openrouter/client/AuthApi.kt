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

/**
 * API for authentication and key validation.
 *
 * This API provides methods for OAuth-style authentication flows, including
 * creating authorization codes and exchanging them for API keys.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.auth].
 *
 * @see AuthCodeRequest
 * @see AuthCodeResponse
 * @see AuthKeyRequest
 * @see AuthKeyResponse
 */
public class AuthApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Creates an authentication code for OAuth-style flows.
     *
     * Calls the `/auth/keys/code` endpoint to generate an authorization code
     * that can be used to authenticate users and grant API access.
     *
     * @param request The auth code creation request.
     * @return An [AuthCodeResponse] containing the authorization code.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see AuthCodeRequest
     * @see AuthCodeResponse
     */
    public suspend fun createAuthCode(request: AuthCodeRequest): AuthCodeResponse {
        val response = httpClient.post("${config.baseUrl}/auth/keys/code") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    /**
     * Exchanges an authorization code for an API key.
     *
     * Calls the `/auth/keys` endpoint to exchange a previously generated
     * authorization code for a usable API key.
     *
     * @param request The key exchange request containing the authorization code.
     * @return An [AuthKeyResponse] containing the API key.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the code is invalid.
     * @see AuthKeyRequest
     * @see AuthKeyResponse
     */
    public suspend fun exchangeCode(request: AuthKeyRequest): AuthKeyResponse {
        val response = httpClient.post("${config.baseUrl}/auth/keys") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }
}
