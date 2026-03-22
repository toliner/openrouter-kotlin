package dev.toliner.openrouter.l1.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeyInfo(
    val label: String,
    val usage: Double,
    val limit: Double? = null,
    @SerialName("is_free_tier")
    val isFreeTier: Boolean,
    @SerialName("rate_limit")
    val rateLimit: RateLimit
)

@Serializable
data class RateLimit(
    val requests: Int,
    val interval: String
)
