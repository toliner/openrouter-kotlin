package dev.toliner.openrouter.l1.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing usage activity for an OpenRouter API key.
 *
 * @property data List of activity records.
 * @see ActivityItem
 */
@Serializable
public data class Activity(
    @SerialName("data")
    val data: List<ActivityItem>
)

/**
 * Usage activity for a single model/endpoint/day combination.
 *
 * @property date Date in ISO 8601 format (e.g., "2024-03-30").
 * @property model Model slug (e.g., "openai/gpt-4.1").
 * @property modelPermaslug Model permaslug (e.g., "openai/gpt-4.1-2025-04-14").
 * @property endpointId Unique identifier for the endpoint.
 * @property providerName Name of the provider serving this endpoint.
 * @property usage Total cost in USD (OpenRouter credits spent).
 * @property byokUsageInference BYOK inference cost in USD (external credits spent).
 * @property requests Number of requests made.
 * @property promptTokens Total prompt tokens used.
 * @property completionTokens Total completion tokens generated.
 * @property reasoningTokens Total reasoning tokens used.
 * @see Activity
 */
@Serializable
public data class ActivityItem(
    @SerialName("date")
    val date: String,
    @SerialName("model")
    val model: String,
    @SerialName("model_permaslug")
    val modelPermaslug: String,
    @SerialName("endpoint_id")
    val endpointId: String,
    @SerialName("provider_name")
    val providerName: String,
    @SerialName("usage")
    val usage: Double,
    @SerialName("byok_usage_inference")
    val byokUsageInference: Double,
    @SerialName("requests")
    val requests: Int,
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("reasoning_tokens")
    val reasoningTokens: Int
)
