package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request body for creating a new guardrail.
 *
 * Used to define a new content filtering guardrail with a name, optional description, and
 * configuration parameters.
 *
 * @property name Human-readable name for the new guardrail (required)
 * @property description Optional detailed description of the guardrail's purpose and behavior
 * @property config Optional JSON configuration object containing guardrail-specific settings
 *
 * @see Guardrail
 * @see UpdateGuardrailRequest
 */
@Serializable
public data class CreateGuardrailRequest(
    val name: String,
    val description: String? = null,
    val config: JsonObject? = null
)
