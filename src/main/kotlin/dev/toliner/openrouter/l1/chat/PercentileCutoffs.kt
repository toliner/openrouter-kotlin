package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Percentile-based cutoff thresholds for throughput or latency.
 *
 * All specified cutoffs must be met for an endpoint to be in the preferred group.
 * Endpoints below the threshold are still used as fallbacks (deprioritized, not excluded).
 *
 * @property p50 The p50 (median) cutoff value.
 * @property p75 The p75 cutoff value.
 * @property p90 The p90 cutoff value.
 * @property p99 The p99 cutoff value.
 */
@Serializable
public data class PercentileCutoffs(
    @SerialName("p50")
    val p50: Double? = null,
    @SerialName("p75")
    val p75: Double? = null,
    @SerialName("p90")
    val p90: Double? = null,
    @SerialName("p99")
    val p99: Double? = null
)
