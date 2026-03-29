package dev.toliner.openrouter.client

import dev.toliner.openrouter.ExperimentalOpenRouterApi
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

/**
 * Main entry point for the OpenRouter API client.
 *
 * This class provides access to all OpenRouter API endpoints through dedicated API modules.
 * It implements [Closeable] to manage the underlying HTTP client lifecycle. Always call [close]
 * when done to release resources, or use the client within a `use` block.
 *
 * Example usage:
 * ```kotlin
 * val config = OpenRouterConfig(apiKey = "your-api-key")
 * OpenRouterClient(CIO.create(), config).use { client ->
 *     val response = client.chat.complete(chatRequest)
 *     // ... use response
 * }
 * ```
 *
 * @property config The configuration for this client instance.
 * @property chat API for chat completions (text generation with conversational context).
 * @property models API for listing and querying available models.
 * @property embeddings API for creating text embeddings.
 * @property generation API for retrieving generation statistics.
 * @property account API for account information (credits, key info, activity).
 * @property keys API for managing API keys.
 * @property guardrails API for managing content guardrails.
 * @property providers API for listing available providers.
 * @property auth API for authentication and key validation.
 * @property responses API for the experimental Responses endpoint (requires [ExperimentalOpenRouterApi]).
 */
public class OpenRouterClient(
    engine: HttpClientEngine,
    internal val config: OpenRouterConfig
) : Closeable {
    internal val httpClient = HttpClient(engine) {
        install(ContentNegotiation) { json(OpenRouterJson) }
        expectSuccess = false
    }

    public val chat: ChatApi = ChatApi(httpClient, config)
    public val models: ModelsApi = ModelsApi(httpClient, config)
    public val embeddings: EmbeddingsApi = EmbeddingsApi(httpClient, config)
    public val generation: GenerationApi = GenerationApi(httpClient, config)
    public val account: AccountApi = AccountApi(httpClient, config)
    public val keys: KeysApi = KeysApi(httpClient, config)
    public val guardrails: GuardrailsApi = GuardrailsApi(httpClient, config)
    public val providers: ProvidersApi = ProvidersApi(httpClient, config)
    public val auth: AuthApi = AuthApi(httpClient, config)
    
    @ExperimentalOpenRouterApi
    public val responses: ResponsesApi = ResponsesApi(httpClient, config)

    /**
     * Closes the underlying HTTP client and releases all resources.
     *
     * After calling this method, the client should not be used for any further API calls.
     */
    public override fun close() {
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
