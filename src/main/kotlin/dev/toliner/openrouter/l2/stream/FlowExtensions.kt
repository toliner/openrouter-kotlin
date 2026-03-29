package dev.toliner.openrouter.l2.stream

import dev.toliner.openrouter.l1.chat.ChatCompletionChunk
import dev.toliner.openrouter.l1.chat.Usage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold

public suspend fun Flow<ChatCompletionChunk>.collectContent(): String {
    return fold("") { acc, chunk ->
        acc + (chunk.choices.firstOrNull()?.delta?.content ?: "")
    }
}

public suspend fun Flow<ChatCompletionChunk>.collectContentAndUsage(): Pair<String, Usage> {
    var lastUsage: Usage? = null
    val content = fold("") { acc, chunk ->
        chunk.usage?.let { lastUsage = it }
        acc + (chunk.choices.firstOrNull()?.delta?.content ?: "")
    }
    return content to (lastUsage ?: Usage(promptTokens = 0, completionTokens = 0, totalTokens = 0))
}
