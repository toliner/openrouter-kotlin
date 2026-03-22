package dev.toliner.openrouter.l1.keys

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiKey(
    val hash: String,
    val name: String,
    val label: String? = null,
    val usage: Double,
    val limit: Double? = null,
    @SerialName("limit_remaining") val limitRemaining: Double? = null,
    @SerialName("is_free_tier") val isFreeTier: Boolean,
    @SerialName("rate_limit") val rateLimit: RateLimit? = null,
    val disabled: Boolean,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long? = null
)

@Serializable
data class RateLimit(
    val requests: Int,
    val interval: String
)
