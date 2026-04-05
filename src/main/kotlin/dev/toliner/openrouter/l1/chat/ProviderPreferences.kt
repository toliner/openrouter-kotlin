package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Provider-specific preferences and routing options for chat completions.
 *
 * @see ChatCompletionRequest.provider
 */
@Serializable
public data class ProviderPreferences(
    @SerialName("order")
    val order: List<String>? = null,
    @SerialName("only")
    val only: List<String>? = null,
    @SerialName("ignore")
    val ignore: List<String>? = null,
    @SerialName("allow_fallbacks")
    val allowFallbacks: Boolean? = null,
    @SerialName("require_parameters")
    val requireParameters: Boolean? = null,
    @SerialName("data_collection")
    val dataCollection: DataCollection? = null,
    @SerialName("zdr")
    val zdr: Boolean? = null,
    @SerialName("enforce_distillable_text")
    val enforceDistillableText: Boolean? = null,
    @SerialName("quantizations")
    val quantizations: List<Quantization>? = null,
    @SerialName("sort")
    val sort: ProviderSort? = null,
    @SerialName("max_price")
    val maxPrice: MaxPrice? = null,
    @SerialName("preferred_min_throughput")
    val preferredMinThroughput: PreferredThroughput? = null,
    @SerialName("preferred_max_latency")
    val preferredMaxLatency: PreferredLatency? = null
)
