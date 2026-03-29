package dev.toliner.openrouter.client

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import dev.toliner.openrouter.l1.responses.CreateResponseRequest
import dev.toliner.openrouter.l1.responses.ResponseObject
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

@ExperimentalOpenRouterApi
public class ResponsesApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
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
