package dev.toliner.openrouter.l1.account

import kotlinx.serialization.Serializable

/**
 * Response containing daily usage activity for an OpenRouter API key.
 *
 * This data is retrieved from the `/api/v1/activity` endpoint and provides
 * a breakdown of API usage by day, including request counts and costs.
 *
 * @property data List of daily activity records, ordered by date
 * @see DailyActivity
 */
@Serializable
public data class Activity(
    val data: List<DailyActivity>
)

/**
 * Daily usage activity for a single day.
 *
 * Represents the aggregated API usage metrics for a specific date,
 * including the number of requests made and the total cost incurred.
 *
 * @property date Date in ISO 8601 format (e.g., "2024-03-30")
 * @property requests Total number of API requests made on this date
 * @property cost Total cost in USD for all requests on this date
 * @see Activity
 */
@Serializable
public data class DailyActivity(
    val date: String,
    val requests: Int,
    val cost: Double
)
