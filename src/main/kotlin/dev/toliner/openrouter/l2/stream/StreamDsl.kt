package dev.toliner.openrouter.l2.stream

import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.l1.chat.ChatCompletionChunk
import dev.toliner.openrouter.l2.chat.ChatRequestBuilder
import kotlinx.coroutines.flow.Flow

suspend fun OpenRouterClient.chatStream(
    block: ChatRequestBuilder.() -> Unit
): Flow<ChatCompletionChunk> {
    val request = ChatRequestBuilder().apply(block).build().copy(stream = true)
    return chat.stream(request)
}
