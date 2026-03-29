package dev.toliner.openrouter.client

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import dev.toliner.openrouter.l1.responses.CreateResponseRequest
import dev.toliner.openrouter.l1.responses.ResponseObject
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API for the experimental Responses endpoint.
 *
 * This API provides methods for the Responses API, which is currently experimental
 * and may change in future versions. All methods require [ExperimentalOpenRouterApi] opt-in.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.responses].
 *
 * @see CreateResponseRequest
 * @see ResponseObject
 */
@ExperimentalOpenRouterApi
public class ResponsesApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Creates a response object.
     *
     * Calls the `/responses` endpoint to create a response. This is an experimental
     * feature and its behavior may change.
     *
     * @param request The response creation request.
     * @return A [ResponseObject] containing the created response.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see CreateResponseRequest
     * @see ResponseObject
     */
    @ExperimentalOpenRouterApi
    public suspend fun create(request: CreateResponseRequest): ResponseObject {
        val response = httpClient.post("${config.baseUrl}/responses") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }
}
