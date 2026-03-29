package dev.toliner.openrouter.l2.stream

import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.l1.chat.ChatCompletionChunk
import dev.toliner.openrouter.l2.chat.ChatRequestBuilder
import kotlinx.coroutines.flow.Flow

/**
 * Creates a streaming chat completion request and returns a Flow of response chunks.
 *
 * This extension function enables server-sent events (SSE) streaming mode, where the model's
 * response is delivered incrementally as a Flow of [ChatCompletionChunk] objects. This is useful
 * for real-time applications, progressive UI updates, and reducing time-to-first-token.
 *
 * The function automatically sets `stream = true` on the request. Each chunk contains a delta
 * with partial content that can be accumulated to build the full response.
 *
 * Example usage:
 * ```kotlin
 * client.chatStream {
 *     model = "openai/gpt-4"
 *     systemMessage("You are a helpful assistant")
 *     userMessage("Write a short story")
 *     maxTokens = 500
 * }.collect { chunk ->
 *     val delta = chunk.choices.firstOrNull()?.delta?.content
 *     if (delta != null) {
 *         print(delta) // Print each token as it arrives
 *     }
 * }
 * ```
 *
 * For simplified content collection, see [collectContent] and [collectContentAndUsage].
 *
 * @param block The configuration block executed in the context of [ChatRequestBuilder]
 * @return A [Flow] of [ChatCompletionChunk] objects representing the streaming response
 * @throws dev.toliner.openrouter.error.OpenRouterException if the API call fails
 * @see ChatRequestBuilder for available configuration options
 * @see ChatCompletionChunk for the chunk structure
 * @see collectContent for simple content accumulation
 * @see collectContentAndUsage for content and usage tracking
 */
public suspend fun OpenRouterClient.chatStream(
    block: ChatRequestBuilder.() -> Unit
): Flow<ChatCompletionChunk> {
    val request = ChatRequestBuilder().apply(block).build().copy(stream = true)
    return chat.stream(request)
}
