package dev.toliner.openrouter.l1.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Configuration for a model endpoint.
 *
 * Represents the URL and optional HTTP headers required to access a specific model endpoint.
 * Used when models are accessed through custom or provider-specific endpoints.
 *
 * @property url The endpoint URL for accessing the model.
 * @property headers Optional JSON object containing HTTP headers to include in requests to this endpoint.
 */
@Serializable
public data class ModelEndpoint(
    val url: String,
    val headers: JsonElement? = null
)
