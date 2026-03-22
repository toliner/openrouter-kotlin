package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuardrailAssignment(
    val id: String,
    @SerialName("guardrail_id") val guardrailId: String,
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("created_at") val createdAt: Long
)
