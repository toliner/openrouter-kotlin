package dev.toliner.openrouter.l1.keys

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a provisioned API key in the OpenRouter Keys API.
 *
 * API keys are used to authenticate requests to OpenRouter and can be managed through the Keys API
 * with usage limits, rate limiting, and enable/disable capabilities.
 *
 * @property hash Unique hash identifier for this API key
 * @property name Human-readable name for the API key
 * @property label Optional label for additional categorization or description
 * @property usage Current usage amount for this API key (in credits or requests)
 * @property limit Optional maximum usage limit; null indicates no limit
 * @property limitRemaining Optional remaining usage before hitting the limit
 * @property isFreeTier Indicates whether this key is on the free tier
 * @property rateLimit Optional rate limiting configuration for this key
 * @property disabled Whether this API key is currently disabled
 * @property createdAt Unix timestamp (seconds) when this key was created
 * @property updatedAt Unix timestamp (seconds) when this key was last updated; null if never updated
 *
 * @see CreateKeyRequest
 * @see UpdateKeyRequest
 * @see RateLimit
 */
@Serializable
public data class ApiKey(
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

/**
 * Rate limiting configuration for an API key.
 *
 * Defines the maximum number of requests allowed within a specific time interval.
 *
 * @property requests Maximum number of requests allowed per interval
 * @property interval Time interval for the rate limit (e.g., "1m", "1h", "1d")
 *
 * @see ApiKey
 */
@Serializable
public data class RateLimit(
    val requests: Int,
    val interval: String
)
