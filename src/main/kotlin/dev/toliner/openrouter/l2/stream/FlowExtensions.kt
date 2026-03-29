package dev.toliner.openrouter.l2.stream

import dev.toliner.openrouter.l1.chat.ChatCompletionChunk
import dev.toliner.openrouter.l1.chat.Usage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold

/**
 * Collects all content from a streaming chat completion and returns it as a single string.
 *
 * This is a convenience extension that accumulates content from all chunks in the flow
 * by extracting `chunk.choices[0].delta.content` and concatenating them in order.
 * Chunks without content (e.g., metadata-only chunks) are ignored.
 *
 * Example usage:
 * ```kotlin
 * val fullResponse = client.chatStream {
 *     model = "openai/gpt-4"
 *     userMessage("Explain Kotlin coroutines in 3 sentences")
 * }.collectContent()
 * println(fullResponse) // Prints the complete response text
 * ```
 *
 * For applications that also need token usage statistics, use [collectContentAndUsage] instead.
 *
 * @return The complete concatenated content as a single string
 * @see collectContentAndUsage for also collecting usage information
 * @see chatStream for creating streaming requests
 */
public suspend fun Flow<ChatCompletionChunk>.collectContent(): String {
    return fold("") { acc, chunk ->
        acc + (chunk.choices.firstOrNull()?.delta?.content ?: "")
    }
}

/**
 * Collects content and usage information from a streaming chat completion.
 *
 * This extension accumulates all content chunks into a single string (like [collectContent])
 * and also captures the final usage statistics from the last chunk that contains usage data.
 *
 * The OpenRouter API sends usage information in the final chunk(s) of the stream. If no
 * usage data is found in the stream, a zero-initialized [Usage] object is returned.
 *
 * Example usage:
 * ```kotlin
 * val (content, usage) = client.chatStream {
 *     model = "openai/gpt-4"
 *     userMessage("What is Kotlin?")
 * }.collectContentAndUsage()
 *
 * println("Response: $content")
 * println("Tokens used: ${usage.totalTokens}")
 * println("Prompt tokens: ${usage.promptTokens}")
 * println("Completion tokens: ${usage.completionTokens}")
 * ```
 *
 * @return A [Pair] of the complete content string and the [Usage] statistics
 * @see collectContent for collecting only the content
 * @see Usage for the structure of usage statistics
 * @see chatStream for creating streaming requests
 */
public suspend fun Flow<ChatCompletionChunk>.collectContentAndUsage(): Pair<String, Usage> {
    var lastUsage: Usage? = null
    val content = fold("") { acc, chunk ->
        chunk.usage?.let { lastUsage = it }
        acc + (chunk.choices.firstOrNull()?.delta?.content ?: "")
    }
    return content to (lastUsage ?: Usage(promptTokens = 0, completionTokens = 0, totalTokens = 0))
}
