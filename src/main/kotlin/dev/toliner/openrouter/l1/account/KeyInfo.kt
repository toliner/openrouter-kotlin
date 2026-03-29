package dev.toliner.openrouter.l1.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API key metadata and usage limits.
 *
 * This data is retrieved from the `/api/v1/auth/key` endpoint and provides
 * detailed information about the API key including its label, usage tracking,
 * credit limits, and rate limiting configuration.
 *
 * @property label User-assigned label for the API key
 * @property usage Total credits consumed by this key to date (in USD)
 * @property limit Optional credit limit for the key (in USD), null if unlimited
 * @property isFreeTier Whether this key is on the free tier plan
 * @property rateLimit Rate limiting configuration for this key
 * @see RateLimit
 */
@Serializable
public data class KeyInfo(
    val label: String,
    val usage: Double,
    val limit: Double? = null,
    @SerialName("is_free_tier")
    val isFreeTier: Boolean,
    @SerialName("rate_limit")
    val rateLimit: RateLimit
)

/**
 * Rate limiting configuration for an API key.
 *
 * Defines the maximum number of requests allowed within a time interval.
 *
 * @property requests Maximum number of requests allowed per interval
 * @property interval Time interval specification (e.g., "10s", "1m", "1h")
 * @see KeyInfo
 */
@Serializable
public data class RateLimit(
    val requests: Int,
    val interval: String
)
