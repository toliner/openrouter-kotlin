package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProviderPreferences(
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
