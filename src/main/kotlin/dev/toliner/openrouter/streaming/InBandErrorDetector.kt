package dev.toliner.openrouter.streaming

import dev.toliner.openrouter.error.OpenRouterException
import dev.toliner.openrouter.l1.chat.ChatCompletionResponse

fun ChatCompletionResponse.checkInBandError(): ChatCompletionResponse {
    val inBandError = choices.firstOrNull { it.error != null }?.error
    if (inBandError != null) {
        throw OpenRouterException.InBandError(inBandError)
    }
    return this
}
