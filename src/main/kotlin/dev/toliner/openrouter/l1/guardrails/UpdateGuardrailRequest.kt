package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UpdateGuardrailRequest(
    val name: String? = null,
    val description: String? = null,
    val config: JsonObject? = null
)
