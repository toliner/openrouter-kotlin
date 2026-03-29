package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for assigning a guardrail to a target resource.
 *
 * Used to create a new guardrail assignment linking a guardrail to a specific target such as
 * a model or API key.
 *
 * @property targetType Type of the target resource to assign the guardrail to (e.g., "model", "key")
 * @property targetId Identifier of the target resource
 *
 * @see GuardrailAssignment
 * @see Guardrail
 */
@Serializable
public data class AddAssignmentRequest(
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String
)
