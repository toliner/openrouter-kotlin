# streaming/ — SSE Parsing & Error Detection

Server-Sent Events parsing and in-band error detection for streaming chat completions.

## Structure

- **SseParser.kt** — Converts `Flow<ServerSentEvent>` to `Flow<ChatCompletionChunk>`.
- **InBandErrorDetector.kt** — Checks non-streaming responses for errors embedded inside successful HTTP responses.

## SseParser

Extension function on `Flow<ServerSentEvent>`:

```kotlin
fun Flow<ServerSentEvent>.toChatCompletionChunks(json: Json): Flow<ChatCompletionChunk>
```

### Behavior
1. `takeWhile` — stops on `[DONE]` sentinel.
2. Filters out empty data and comment lines.
3. `normalizedData()` — handles double-prefixed `data: data:` lines and trims whitespace.
4. Deserializes each line to `ChatCompletionChunk`.
5. Detects stream errors: if `finishReason == "error"`, throws `OpenRouterException.StreamError`.
6. Skips debug chunks: events with a `"debug"` key and empty choices array.

## InBandErrorDetector

Extension function on `ChatCompletionResponse`:

```kotlin
fun ChatCompletionResponse.checkInBandError(): ChatCompletionResponse
```

Iterates over `choices`, checks for `finishReason == "error"` combined with non-null `error` field. Throws `OpenRouterException.InBandError` if found. Otherwise returns `this` unchanged.

Called by `ChatApi.complete()` after decoding the response.

## Conventions

- Stream errors and in-band errors are always surfaced as typed `OpenRouterException` subtypes — never swallowed.
- The `normalizedData()` helper is private and handles an OpenRouter-specific quirk (double `data:` prefix).
- These utilities are consumed only by `client/ChatApi` — they are not part of the public API surface.
