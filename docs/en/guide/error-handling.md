# Error Handling Guide

openrouter-kotlin uses a sealed exception hierarchy to handle all possible error states from the OpenRouter API. This ensures that you can catch and handle errors in a type-safe way.

## OpenRouterException Hierarchy

All exceptions thrown by the library inherit from `OpenRouterException`.

### Subtypes and HTTP Status Codes

- `BadRequest` (400): The request was invalid (e.g., missing required fields).
- `Unauthorized` (401): Invalid or missing API key.
- `PaymentRequired` (402): Insufficient credits for the request.
- `Forbidden` (403): Access to the requested model is restricted.
- `RequestTimeout` (408): The request took too long.
- `TooManyRequests` (429): You have exceeded your rate limit.
- `BadGateway` (502): The upstream provider returned an error.
- `ServiceUnavailable` (503): The service is temporarily unavailable.
- `UnknownError`: For status codes that don't have a specific mapping.
- `InBandError`: An error occurred inside a successful HTTP response.
- `StreamError`: An error occurred during a streaming response.

## Handling Errors

You can use a `try-catch` block to handle different error types.

```kotlin
import dev.toliner.openrouter.error.OpenRouterException
import kotlinx.coroutines.delay

try {
    val response = client.chat {
        model = "openai/gpt-4o-mini"
        userMessage("Hello!")
    }
} catch (e: OpenRouterException.TooManyRequests) {
    // Wait for the duration suggested by the server, or default to 60s
    val waitSeconds = e.retryAfter ?: 60
    delay(waitSeconds * 1000L)
    // Attempt retry
} catch (e: OpenRouterException.PaymentRequired) {
    println("Error: Insufficient credits.")
} catch (e: OpenRouterException) {
    if (e.isRetryable) {
        println("A temporary error occurred: ${e.message}. Retrying...")
    } else {
        println("A permanent error occurred: ${e.message}")
    }
}
```

## Useful Properties

- `isRetryable`: An extension property on `OpenRouterException` that returns `true` if the error is temporary (e.g., 429, 502, 503).
- `retryAfter`: Available on `TooManyRequests`, this is the value of the `Retry-After` header in seconds.
- `error`: Contains the `ErrorBody` returned by the server, which may include a detailed message and a code.

## Provider Errors

When an error is returned by an upstream provider, the `ErrorBody` may include a `ProviderError` object with more details from the specific model provider.

```kotlin
catch (e: OpenRouterException) {
    val providerMessage = e.error?.metadata?.providerName
    println("Error from provider $providerMessage: ${e.error?.message}")
}
```
!!! warning
    Always ensure you handle `OpenRouterException` in production code to avoid unhandled crashes during API calls.
