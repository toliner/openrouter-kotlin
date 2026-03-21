package dev.toliner.openrouter.l1.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ModelEndpoint(
    val url: String,
    val headers: JsonElement? = null
)
