# Streaming Guide

Streaming allows you to receive model responses progressively as they are being generated. openrouter-kotlin leverages Kotlin Coroutines `Flow` for idiomatic streaming.

## Using the Streaming DSL

Use `client.chatStream { }` to initiate a streaming request.

```kotlin
import dev.toliner.openrouter.l2.chat.chatStream
import kotlinx.coroutines.flow.collect

client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Write a long story about space.")
}.collect { chunk ->
    val content = chunk.choices.firstOrNull()?.delta?.content ?: ""
    print(content)
}
```

### Response Chunks

Each element in the flow is a `ChatCompletionChunk`. Unlike full responses, chunks contain a `delta` instead of a full `message`.

## Collection Helpers

Processing raw chunks manually can be repetitive. The library provides extension methods to collect only what you need.

### collectContent()

Collects all content deltas and provides them as a single string.

```kotlin
import dev.toliner.openrouter.streaming.collectContent

val fullContent = client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Tell me a joke.")
}.collectContent()

println(fullContent)
```

### collectContentAndUsage()

If you need both the generated content and the token usage metadata (which usually comes in the final chunk), use `collectContentAndUsage()`.

```kotlin
import dev.toliner.openrouter.streaming.collectContentAndUsage

val (content, usage) = client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Explain the theory of relativity.")
}.collectContentAndUsage()

println("Content: $content")
println("Usage: ${usage?.totalTokens} tokens")
```

## Progressive UI Output

For building a progressive user interface, you can collect the flow and update your state in real-time.

```kotlin
val output = StringBuilder()
client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Explain quantum computing.")
}.collect { chunk ->
    chunk.choices.firstOrNull()?.delta?.content?.let { delta ->
        output.append(delta)
        updateUI(output.toString())
    }
}
```

!!! note
    Streaming requests automatically set `stream = true` in the underlying request, regardless of what you specify in the builder.
