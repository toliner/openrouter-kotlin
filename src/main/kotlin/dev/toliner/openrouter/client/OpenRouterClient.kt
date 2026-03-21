package dev.toliner.openrouter.client

import dev.toliner.openrouter.error.ErrorBody
import dev.toliner.openrouter.error.ErrorResponse
import dev.toliner.openrouter.error.errorFromStatus
import dev.toliner.openrouter.serialization.OpenRouterJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import java.io.Closeable

class OpenRouterClient(
    engine: HttpClientEngine,
    internal val config: OpenRouterConfig
) : Closeable {
    internal val httpClient = HttpClient(engine) {
        install(ContentNegotiation) { json(OpenRouterJson) }
        expectSuccess = false
    }

    val chat: ChatApi = ChatApi(httpClient, config)
    val models: ModelsApi = ModelsApi(httpClient, config)
    val embeddings: EmbeddingsApi = EmbeddingsApi(httpClient, config)

    override fun close() {
        httpClient.close()
    }
}

internal fun HttpRequestBuilder.applyOpenRouterHeaders(config: OpenRouterConfig) {
    header(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
    config.httpReferer?.let { header("HTTP-Referer", it) }
    config.xTitle?.let { header("X-Title", it) }
}

internal suspend fun HttpResponse.throwIfErrorStatus() {
    if (status.value in 200..299) return

    val text = bodyAsText()
    val body = parseErrorBody(text)
    val retryAfter = headers[HttpHeaders.RetryAfter]?.toIntOrNull()
    throw errorFromStatus(status.value, body, retryAfter)
}

internal suspend inline fun <reified T> HttpResponse.decodeBodyOrThrow(): T {
    throwIfErrorStatus()
    return body()
}

private fun parseErrorBody(raw: String): ErrorBody {
    return runCatching { OpenRouterJson.decodeFromString<ErrorResponse>(raw).error }
        .getOrElse {
            runCatching { OpenRouterJson.decodeFromString<ErrorBody>(raw) }
                .getOrElse { ErrorBody(message = "HTTP error", providerError = null) }
        }
}
