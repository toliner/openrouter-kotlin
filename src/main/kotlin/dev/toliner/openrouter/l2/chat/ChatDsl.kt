package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.l1.chat.ChatCompletionRequest
import dev.toliner.openrouter.l1.chat.ChatCompletionResponse

/**
 * Creates a chat completion request using a fluent DSL builder.
 *
 * This function provides a type-safe Kotlin DSL for constructing [ChatCompletionRequest] instances.
 * The builder pattern allows for concise and readable request construction with automatic validation.
 *
 * Example usage:
 * ```kotlin
 * val request = chatRequest {
 *     model = "openai/gpt-4"
 *     systemMessage("You are a helpful assistant")
 *     userMessage("What is the weather today?")
 *     temperature = 0.7
 *     maxTokens = 500
 * }
 * ```
 *
 * @param block The configuration block executed in the context of [ChatRequestBuilder].
 * @return A fully constructed [ChatCompletionRequest] ready to be sent to the API.
 * @see ChatRequestBuilder for available configuration options
 * @see ChatCompletionRequest for the underlying l1 data model
 */
public fun chatRequest(block: ChatRequestBuilder.() -> Unit): ChatCompletionRequest {
    return ChatRequestBuilder().apply(block).build()
}

/**
 * Executes a chat completion request using a fluent DSL builder and returns the response.
 *
 * This extension function combines request building and API invocation into a single operation.
 * It constructs the request using the DSL, sends it to the OpenRouter API, and returns the
 * complete response.
 *
 * Example usage:
 * ```kotlin
 * val response = client.chat {
 *     model = "anthropic/claude-3-opus"
 *     systemMessage("You are a code reviewer")
 *     userMessage("Review this function: fun add(a: Int, b: Int) = a + b")
 *     maxTokens = 1000
 * }
 * println(response.choices.first().message.content)
 * ```
 *
 * @param block The configuration block executed in the context of [ChatRequestBuilder].
 * @return The [ChatCompletionResponse] from the OpenRouter API.
 * @throws dev.toliner.openrouter.error.OpenRouterException if the API call fails
 * @see chatRequest for building requests without executing them
 * @see ChatRequestBuilder for available configuration options
 * @see ChatCompletionResponse for the response structure
 */
public suspend fun OpenRouterClient.chat(block: ChatRequestBuilder.() -> Unit): ChatCompletionResponse {
    val request = chatRequest(block)
    return chat.complete(request)
}
