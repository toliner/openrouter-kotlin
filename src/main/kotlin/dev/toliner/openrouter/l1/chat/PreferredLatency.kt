package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.PreferredLatencySerializer
import kotlinx.serialization.Serializable

/**
 * Preferred maximum latency constraint (seconds). Endpoints above threshold
 * are deprioritized but not excluded.
 *
 * - [Value]: a single number applied as the p50 threshold.
 * - [Percentile]: percentile-based cutoffs for fine-grained control.
 *
 * @see ProviderPreferences.preferredMaxLatency
 */
@Serializable(with = PreferredLatencySerializer::class)
public sealed interface PreferredLatency {
    @JvmInline
    public value class Value(public val seconds: Double) : PreferredLatency

    @JvmInline
    public value class Percentile(public val cutoffs: PercentileCutoffs) : PreferredLatency
}
