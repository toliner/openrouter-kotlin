# Module openrouter-kotlin

Kotlin client library for the [OpenRouter API](https://openrouter.ai/docs/api-reference).

OpenRouter is an LLM API aggregator that provides a unified interface to hundreds of AI models
from providers such as OpenAI, Anthropic, Google, Meta, and more. This library offers a
type-safe, coroutine-based Kotlin client with a fluent DSL for building requests.

## Features

- **Type-safe API** — Every request and response is modeled as a Kotlin data class with
  kotlinx-serialization support.
- **Coroutine-first** — All API methods are `suspend` functions. Streaming responses use
  Kotlin `Flow`.
- **Fluent DSL** — Build chat requests with an idiomatic Kotlin DSL instead of constructing
  data classes manually.
- **Comprehensive error handling** — HTTP errors are mapped to a sealed exception hierarchy
  with retry guidance.
- **Full API coverage** — Chat completions, models, embeddings, account, keys, guardrails,
  providers, auth, generation stats, and the experimental Responses API.

## Quick Start

```kotlin
val client = OpenRouterClient(OpenRouterConfig(apiKey = "sk-or-..."))

// Using the DSL
val response = client.chat {
    model = "openai/gpt-4o"
    systemMessage("You are a helpful assistant.")
    userMessage("Hello!")
}
println(response.choices.first().message?.content)

// Streaming
client.chatStream {
    model = "anthropic/claude-sonnet-4"
    userMessage("Tell me a story.")
}.collectContent { print(it) }

client.close()
```

# Package dev.toliner.openrouter

Root package containing the [ExperimentalOpenRouterApi] opt-in annotation for unstable API surfaces.

# Package dev.toliner.openrouter.client

HTTP client classes for calling OpenRouter API endpoints.

The main entry point is [OpenRouterClient][dev.toliner.openrouter.client.OpenRouterClient],
which provides access to all API endpoints via typed properties (`chat`, `models`,
`embeddings`, `account`, `keys`, `guardrails`, `providers`, `auth`, `generation`, `responses`).

Configuration is handled by [OpenRouterConfig][dev.toliner.openrouter.client.OpenRouterConfig].

# Package dev.toliner.openrouter.l1.chat

Low-level data models for the Chat Completions API (`/chat/completions`).

Contains request/response types ([ChatCompletionRequest][dev.toliner.openrouter.l1.chat.ChatCompletionRequest],
[ChatCompletionResponse][dev.toliner.openrouter.l1.chat.ChatCompletionResponse]),
message types ([Message][dev.toliner.openrouter.l1.chat.Message]), tool definitions
([FunctionTool][dev.toliner.openrouter.l1.chat.FunctionTool]), and streaming chunks
([ChatCompletionChunk][dev.toliner.openrouter.l1.chat.ChatCompletionChunk]).

# Package dev.toliner.openrouter.l1.models

Data models for the Models API (`/models`).

Includes [Model][dev.toliner.openrouter.l1.models.Model] metadata (pricing, context length,
supported features), [ModelList][dev.toliner.openrouter.l1.models.ModelList],
and endpoint information.

# Package dev.toliner.openrouter.l1.embeddings

Data models for the Embeddings API (`/embeddings`).

# Package dev.toliner.openrouter.l1.account

Data models for account-related endpoints: credit balance, usage activity, and key information.

# Package dev.toliner.openrouter.l1.auth

Data model for the auth key validation endpoint (`/auth/key`).

# Package dev.toliner.openrouter.l1.keys

Data models for the API Keys management endpoints (`/keys`).

Supports creating, updating, and listing provisioned API keys.

# Package dev.toliner.openrouter.l1.guardrails

Data models for the Guardrails API (`/guardrails`).

Guardrails allow configuring content filtering rules applied to model responses.

# Package dev.toliner.openrouter.l1.generation

Data model for the Generation Stats endpoint (`/generation`).

# Package dev.toliner.openrouter.l1.providers

Data model for the Providers endpoint (`/providers`).

# Package dev.toliner.openrouter.l1.responses

Data models for the experimental Responses API.

This API is marked with [ExperimentalOpenRouterApi][dev.toliner.openrouter.ExperimentalOpenRouterApi]
and may change without notice.

# Package dev.toliner.openrouter.l2

High-level DSL builders for constructing API request types.

# Package dev.toliner.openrouter.l2.chat

DSL entry points for chat completions: [chatRequest][dev.toliner.openrouter.l2.chat.chatRequest],
[OpenRouterClient.chat][dev.toliner.openrouter.l2.chat.chat], and the
[ChatRequestBuilder][dev.toliner.openrouter.l2.chat.ChatRequestBuilder].

# Package dev.toliner.openrouter.l2.stream

Streaming DSL and Flow extensions for processing chat completion streams.

# Package dev.toliner.openrouter.l2.tools

DSL builders for defining function tools and JSON schemas in chat requests.

# Package dev.toliner.openrouter.l2.routing

DSL builder for configuring provider routing preferences.

# Package dev.toliner.openrouter.error

Sealed exception hierarchy for OpenRouter API errors.

All API errors are represented as subtypes of
[OpenRouterException][dev.toliner.openrouter.error.OpenRouterException], mapped from HTTP
status codes. The [isRetryable][dev.toliner.openrouter.error.isRetryable] extension property
indicates whether a failed request can be safely retried.

# Package dev.toliner.openrouter.serialization

Sealed types for JSON union patterns used in the OpenRouter API.

Includes [Content][dev.toliner.openrouter.serialization.Content] (string or content parts),
[StringOrArray][dev.toliner.openrouter.serialization.StringOrArray], and
[ToolChoice][dev.toliner.openrouter.serialization.ToolChoice].
