package dev.toliner.openrouter.l1.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Credits(
    @SerialName("total_credits")
    val totalCredits: Double,
    @SerialName("total_usage")
    val totalUsage: Double
)
