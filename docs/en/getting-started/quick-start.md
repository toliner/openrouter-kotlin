# Quick Start

Get up and running with openrouter-kotlin in just a few steps.

## Initial Setup

First, configure your OpenRouter API key.

```kotlin
import dev.toliner.openrouter.client.OpenRouterConfig

val config = OpenRouterConfig(apiKey = "sk-or-...")
```

Next, initialize the `OpenRouterClient` with an HTTP engine. We recommend using the **CIO** (Coroutine-based I/O) engine, which is included by default.

```kotlin
import dev.toliner.openrouter.client.OpenRouterClient
import io.ktor.client.engine.cio.CIO

val client = OpenRouterClient(CIO.create(), config)
```

## Making a Chat Completion

The library provides a convenient DSL to make requests easily.

```kotlin
import dev.toliner.openrouter.l2.chat.chat

suspend fun main() {
    client.use { client ->
        val response = client.chat {
            model = "openai/gpt-4o-mini"
            systemMessage("You are a helpful assistant")
            userMessage("Hello!")
            maxTokens = 100
        }
        
        val content = response.choices.first().message?.content
        println(content)
    }
}
```

## Using Lower-Level Types

If you prefer to work with data classes directly without the DSL:

```kotlin
import dev.toliner.openrouter.l1.chat.*

val request = ChatCompletionRequest(
    model = "openai/gpt-4o-mini",
    messages = listOf(
        ChatMessage.User(content = MessageContent.Text("Hello!"))
    )
)

val response = client.chat.complete(request)
```

## Resource Management

The `OpenRouterClient` implements `AutoCloseable`, so you can use the `.use { }` extension to ensure resources are properly cleaned up.

```kotlin
client.use {
    // client is closed automatically when this block ends
}
```
