package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.chat.ChatCompletionChunk
import dev.toliner.openrouter.l1.chat.ChatCompletionRequest
import dev.toliner.openrouter.l1.chat.ChatCompletionResponse
import dev.toliner.openrouter.streaming.checkInBandError
import dev.toliner.openrouter.streaming.toChatCompletionChunks
import dev.toliner.openrouter.serialization.OpenRouterJson
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * API for chat completions (conversational text generation).
 *
 * This API provides methods for generating text completions using OpenRouter's chat models.
 * It supports both standard (complete) and streaming responses.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.chat].
 *
 * @see ChatCompletionRequest
 * @see ChatCompletionResponse
 * @see ChatCompletionChunk
 */
public class ChatApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Creates a chat completion and returns the full response.
     *
     * This method calls the `/chat/completions` endpoint with `stream = false` and waits for
     * the complete response. The response is checked for in-band errors before being returned.
     *
     * @param request The chat completion request containing model, messages, and generation parameters.
     * @return The complete chat completion response.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or an error occurs.
     * @see ChatCompletionRequest
     * @see ChatCompletionResponse
     */
    public suspend fun complete(request: ChatCompletionRequest): ChatCompletionResponse {
        val response = httpClient.post("${config.baseUrl}/chat/completions") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request.copy(stream = false))
        }

        return response.decodeBodyOrThrow<ChatCompletionResponse>().checkInBandError()
    }

    /**
     * Creates a streaming chat completion and returns a Flow of chunks.
     *
     * This method calls the `/chat/completions` endpoint with `stream = true` and returns
     * a Flow that emits completion chunks as they arrive from the server via Server-Sent Events.
     *
     * The Flow will emit chunks until the stream is complete (indicated by a `[DONE]` message)
     * or an error occurs.
     *
     * @param request The chat completion request containing model, messages, and generation parameters.
     * @return A Flow emitting chat completion chunks as they arrive.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or an error occurs during streaming.
     * @see ChatCompletionRequest
     * @see ChatCompletionChunk
     */
    public fun stream(request: ChatCompletionRequest): Flow<ChatCompletionChunk> = flow {
        val response = httpClient.post("${config.baseUrl}/chat/completions") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, "text/event-stream")
            setBody(request.copy(stream = true))
        }
        response.throwIfErrorStatus()

        val events = response
            .bodyAsText()
            .lineSequence()
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trimStart() }
            .map { ServerSentEvent(data = it) }
            .asFlow()

        emitAll(events.toChatCompletionChunks(OpenRouterJson))
    }
}
