package dev.toliner.openrouter.client

/**
 * Configuration for the OpenRouter API client.
 *
 * This data class holds all configuration parameters needed to communicate with the OpenRouter API,
 * including authentication credentials and optional metadata headers.
 *
 * @property apiKey The OpenRouter API key for authentication. Required for all API requests.
 * @property baseUrl The base URL of the OpenRouter API. Defaults to `https://openrouter.ai/api/v1`.
 * @property httpReferer Optional HTTP-Referer header value. Used for request attribution and tracking.
 * @property xTitle Optional X-Title header value. Used to identify your application in OpenRouter's dashboard.
 */
public data class OpenRouterConfig(
    val apiKey: String,
    val baseUrl: String = "https://openrouter.ai/api/v1",
    val httpReferer: String? = null,
    val xTitle: String? = null
)
