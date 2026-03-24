package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.l1.chat.ChatCompletionRequest
import dev.toliner.openrouter.l1.chat.ChatCompletionResponse

fun chatRequest(block: ChatRequestBuilder.() -> Unit): ChatCompletionRequest {
    return ChatRequestBuilder().apply(block).build()
}

suspend fun OpenRouterClient.chat(block: ChatRequestBuilder.() -> Unit): ChatCompletionResponse {
    val request = chatRequest(block)
    return chat.complete(request)
}
