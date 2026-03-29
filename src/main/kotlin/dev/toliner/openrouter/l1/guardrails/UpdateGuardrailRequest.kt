package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Request body for updating an existing guardrail.
 *
 * Used to modify properties of an existing guardrail. All fields are optional; only provided
 * fields will be updated.
 *
 * @property name New name for the guardrail; null to leave unchanged
 * @property description New description; null to leave unchanged
 * @property config New configuration object; null to leave unchanged
 *
 * @see Guardrail
 * @see CreateGuardrailRequest
 */
@Serializable
public data class UpdateGuardrailRequest(
    val name: String? = null,
    val description: String? = null,
    val config: JsonObject? = null
)
