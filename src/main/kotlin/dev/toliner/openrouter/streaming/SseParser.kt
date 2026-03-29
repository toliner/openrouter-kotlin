package dev.toliner.openrouter.streaming

import dev.toliner.openrouter.error.OpenRouterException
import dev.toliner.openrouter.l1.chat.ChatCompletionChunk
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

internal fun Flow<ServerSentEvent>.toChatCompletionChunks(json: Json): Flow<ChatCompletionChunk> =
    takeWhile { event ->
        val data = event.normalizedData() ?: return@takeWhile true
        data != "[DONE]"
    }
        .filter { event ->
            val data = event.normalizedData()
            !data.isNullOrEmpty() && !data.startsWith(":")
        }
        .map { event ->
            val data = event.normalizedData() ?: error("unreachable")
            val element = json.parseToJsonElement(data)
            if (element.shouldSkipDebugChunk()) {
                null
            } else {
                val chunk = json.decodeFromJsonElement<ChatCompletionChunk>(element)
                val streamError = chunk.choices.firstOrNull {
                    it.finishReason == "error" && it.error != null
                }?.error
                if (streamError != null) {
                    throw OpenRouterException.StreamError(streamError)
                }
                chunk
            }
        }
        .filter { it != null }
        .map { it ?: error("unreachable") }

private fun kotlinx.serialization.json.JsonElement.shouldSkipDebugChunk(): Boolean {
    val obj = this as? JsonObject ?: return false
    val hasDebug = "debug" in obj
    if (!hasDebug) return false
    val choices = obj["choices"] as? JsonArray ?: return false
    return choices.isEmpty()
}

private fun ServerSentEvent.normalizedData(): String? {
    val raw = data ?: return null
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null

    val payload = if (trimmed.startsWith("data:")) {
        trimmed.removePrefix("data:").trimStart()
    } else {
        trimmed
    }

    return payload
}
