# openrouter-kotlin

Kotlin client library for the [OpenRouter API](https://openrouter.ai/).

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue.svg?style=flat-square&logo=kotlin)
![JVM](https://img.shields.io/badge/JVM-25-orange.svg?style=flat-square)
![kotlinx-serialization](https://img.shields.io/badge/kotlinx--serialization-1.10.0-blueviolet.svg?style=flat-square)

openrouter-kotlin provides a clean, type-safe, and idiomatic way to interact with OpenRouter from Kotlin and other JVM languages.

## Key Features

- **Type-Safe DSL**: Build complex chat requests with an intuitive Kotlin DSL.
- **Full Streaming Support**: Easily handle streaming responses with Kotlin Coroutines Flow.
- **Robust Error Handling**: Exhaustive sealed exception hierarchy for all API and HTTP error states.
- **Complete API Coverage**: Support for Chat Completions, Models, Providers, and Account endpoints.
- **Custom Serialization**: Built-in support for OpenRouter's flexible JSON union types.
- **Ktor-Powered**: Built on the modern Ktor HTTP client with the efficient CIO engine.

## Quick Example

```kotlin
import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.client.OpenRouterConfig
import dev.toliner.openrouter.l2.chat.chat
import io.ktor.client.engine.cio.CIO

val config = OpenRouterConfig(apiKey = "sk-or-...")
val client = OpenRouterClient(CIO.create(), config)

val response = client.chat {
    model = "openai/gpt-4o-mini"
    userMessage("Hello! Tell me about Kotlin.")
}

println(response.choices.first().message?.content)
```

## Getting Started

- [Installation](getting-started/installation.md)
- [Quick Start](getting-started/quick-start.md)

## Guides

- [Chat Completions](guide/chat-completions.md)
- [Streaming](guide/streaming.md)
- [DSL Builders](guide/dsl-builders.md)
- [Error Handling](guide/error-handling.md)
- [Models & Providers](guide/models-providers.md)
