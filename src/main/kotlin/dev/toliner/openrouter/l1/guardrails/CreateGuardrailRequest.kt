package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CreateGuardrailRequest(
    val name: String,
    val description: String? = null,
    val config: JsonObject? = null
)
