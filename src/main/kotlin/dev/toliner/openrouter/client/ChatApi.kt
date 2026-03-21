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

class ChatApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun complete(request: ChatCompletionRequest): ChatCompletionResponse {
        val response = httpClient.post("${config.baseUrl}/chat/completions") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request.copy(stream = false))
        }

        return response.decodeBodyOrThrow<ChatCompletionResponse>().checkInBandError()
    }

    fun stream(request: ChatCompletionRequest): Flow<ChatCompletionChunk> = flow {
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
