package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an assignment of a guardrail to a specific target.
 *
 * Guardrail assignments link a guardrail to a target resource (such as a model or API key),
 * enabling the guardrail's filtering rules to be applied to that target.
 *
 * @property id Unique identifier for this assignment
 * @property guardrailId Identifier of the guardrail being assigned
 * @property targetType Type of the target resource (e.g., "model", "key")
 * @property targetId Identifier of the target resource
 * @property createdAt Unix timestamp (seconds) when this assignment was created
 *
 * @see Guardrail
 * @see AddAssignmentRequest
 */
@Serializable
public data class GuardrailAssignment(
    val id: String,
    @SerialName("guardrail_id") val guardrailId: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("created_at") val createdAt: Long
)
