package dev.toliner.openrouter.serialization

import kotlinx.serialization.json.Json

val OpenRouterJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    isLenient = false
    encodeDefaults = false
}
