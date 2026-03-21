package dev.toliner.openrouter.client

data class OpenRouterConfig(
    val apiKey: String,
    val baseUrl: String = "https://openrouter.ai/api/v1",
    val httpReferer: String? = null,
    val xTitle: String? = null
)
