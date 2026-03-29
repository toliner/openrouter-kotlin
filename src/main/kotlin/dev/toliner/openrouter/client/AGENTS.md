# client/ — API Implementation Layer

HTTP client classes that call the OpenRouter API using Ktor.

## Structure

- **OpenRouterClient.kt** — Main entry point. Takes `HttpClientEngine` + `OpenRouterConfig`, creates the shared `HttpClient`, exposes API modules as `val` properties. Implements `Closeable`.
- **OpenRouterConfig.kt** — Configuration data class: `apiKey`, `baseUrl` (default: `https://openrouter.ai/api/v1`), optional `httpReferer`, `xTitle`.
- **XxxApi.kt** — One class per API endpoint (ChatApi, ModelsApi, EmbeddingsApi, etc.). Each takes `(HttpClient, OpenRouterConfig)` as constructor parameters (private fields).

## HTTP Patterns

### POST Endpoint (ChatApi, EmbeddingsApi, ResponsesApi, GuardrailsApi)
```kotlin
suspend fun methodName(request: L1RequestType): L1ResponseType {
    val response = httpClient.post("${config.baseUrl}/path") {
        applyOpenRouterHeaders(config)
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    return response.decodeBodyOrThrow()
}
```

### GET Endpoint (ModelsApi, AccountApi, KeysApi, ProvidersApi, AuthApi, GenerationApi)
```kotlin
private suspend inline fun <reified T> getAndDecode(url: String): T {
    val response = httpClient.get(url) {
        applyOpenRouterHeaders(config)
    }
    return response.decodeBodyOrThrow()
}
```

### ChatApi Special Cases
- `complete()` forces `stream = false` via `request.copy(stream = false)`, then calls `.checkInBandError()` on the decoded response.
- `stream()` forces `stream = true`, adds `Accept: text/event-stream` header, parses SSE from `bodyAsText()`, returns `Flow<ChatCompletionChunk>`.

## Internal Helpers (package-internal)

- `applyOpenRouterHeaders(config)` — Sets `Authorization: Bearer {apiKey}`, optional `HTTP-Referer`, `X-Title`.
- `throwIfErrorStatus(response)` — Checks status in 200-299 range, otherwise parses error body and throws typed `OpenRouterException`.
- `decodeBodyOrThrow<T>(response)` — Combines status check + body deserialization.
- `parseErrorBody(bodyText)` — Tries `ErrorResponse`, then `ErrorBody`, then constructs a fallback.

## Conventions

- `HttpClient` is created once in `OpenRouterClient` with `ContentNegotiation { json(OpenRouterJson) }` and `expectSuccess = false`.
- Never set `expectSuccess = true` — error handling is manual via `throwIfErrorStatus()`.
- All API methods are `suspend` functions.
- `@ExperimentalOpenRouterApi` is applied to `ResponsesApi` and its methods.
- When adding a new Api class, wire it into `OpenRouterClient` as a `val` property initialized in the constructor.

## Testing

Tests are in `src/test/kotlin/dev/toliner/openrouter/client/`. Use `mockEngineWithResponse()` from `testutil/MockEngineHelpers.kt`:

```kotlin
class XxxApiTest : FunSpec({
    test("description") {
        val (engine, requestValidator) = mockEngineWithResponse(responseJson)
        val client = OpenRouterClient(engine, config)
        // call API method
        // requestValidator.captured should contain assertions
    }
})
```
