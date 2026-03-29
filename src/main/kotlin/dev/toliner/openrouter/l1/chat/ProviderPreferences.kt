package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents provider-specific preferences and routing options for chat completions.
 *
 * OpenRouter routes requests to multiple providers. This class allows fine-grained
 * control over provider selection, fallback behavior, and routing strategies.
 *
 * @property order Preferred order of providers to try. List of provider IDs (e.g., ["OpenAI", "Anthropic"]).
 * @property allowFallbacks Whether to allow fallback to other providers if preferred providers fail.
 * @property requireParameters Whether to require all request parameters to be supported by the selected provider.
 * @property dataCollection Data collection preference ("deny", "allow"). Controls whether provider can train on data.
 * @property sort Sorting strategy for provider selection ("cost", "latency", "quality", etc.).
 * @property preferredMinThroughput Minimum preferred throughput in tokens/second. Filters providers by speed.
 * @property ignore List of provider IDs to exclude from selection.
 *
 * @see ChatCompletionRequest.provider
 */
@Serializable
public data class ProviderPreferences(
    @SerialName("order")
    val order: List<String>? = null,
    @SerialName("allow_fallbacks")
    val allowFallbacks: Boolean? = null,
    @SerialName("require_parameters")
    val requireParameters: Boolean? = null,
    @SerialName("data_collection")
    val dataCollection: String? = null,
    @SerialName("sort")
    val sort: String? = null,
    @SerialName("preferred_min_throughput")
    val preferredMinThroughput: Int? = null,
    @SerialName("ignore")
    val ignore: List<String>? = null
)
