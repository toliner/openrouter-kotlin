package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddAssignmentRequest(
    @SerialName("target_type") val targetType: String,
    @SerialName("target_id") val targetId: String
)
