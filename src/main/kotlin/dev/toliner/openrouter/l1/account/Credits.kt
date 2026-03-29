package dev.toliner.openrouter.l1.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Account balance and usage information.
 *
 * This data is retrieved from the `/api/v1/auth/key` endpoint and provides
 * the current credit balance and total usage for an API key.
 * Credits are denominated in USD.
 *
 * @property totalCredits Total credits available on the account (in USD)
 * @property totalUsage Total credits consumed to date (in USD)
 */
@Serializable
public data class Credits(
    @SerialName("total_credits")
    val totalCredits: Double,
    @SerialName("total_usage")
    val totalUsage: Double
)
