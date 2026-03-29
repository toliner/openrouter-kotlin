package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a content guardrail in the OpenRouter Guardrails API.
 *
 * Guardrails are content filtering rules that can be applied to model responses to ensure outputs
 * meet specified criteria. They can be configured with custom parameters and assigned to specific
 * targets (e.g., models or API keys).
 *
 * @property id Unique identifier for this guardrail
 * @property name Human-readable name for the guardrail
 * @property description Optional detailed description of the guardrail's purpose and behavior
 * @property config Optional JSON configuration object containing guardrail-specific settings
 * @property createdAt Unix timestamp (seconds) when this guardrail was created
 * @property updatedAt Unix timestamp (seconds) when this guardrail was last updated; null if never updated
 *
 * @see CreateGuardrailRequest
 * @see UpdateGuardrailRequest
 * @see GuardrailAssignment
 */
@Serializable
public data class Guardrail(
    val id: String,
    val name: String,
    val description: String? = null,
    val config: JsonObject? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long? = null
)
