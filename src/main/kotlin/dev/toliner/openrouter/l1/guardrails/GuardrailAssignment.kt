package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a key assignment to a guardrail.
 *
 * @see Guardrail
 */
@Serializable
public data class GuardrailAssignment(
    @SerialName("id")
    val id: String,
    @SerialName("key_hash")
    val keyHash: String,
    @SerialName("guardrail_id")
    val guardrailId: String,
    @SerialName("key_name")
    val keyName: String,
    @SerialName("key_label")
    val keyLabel: String,
    @SerialName("assigned_by")
    val assignedBy: String?,
    @SerialName("created_at")
    val createdAt: String
)
