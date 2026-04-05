package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.PreferredThroughputSerializer
import kotlinx.serialization.Serializable

/**
 * Preferred minimum throughput constraint (tokens/sec). Endpoints below threshold
 * are deprioritized but not excluded.
 *
 * - [Value]: a single number applied as the p50 threshold.
 * - [Percentile]: percentile-based cutoffs for fine-grained control.
 *
 * @see ProviderPreferences.preferredMinThroughput
 */
@Serializable(with = PreferredThroughputSerializer::class)
public sealed interface PreferredThroughput {
    @JvmInline
    public value class Value(public val tokensPerSecond: Double) : PreferredThroughput

    @JvmInline
    public value class Percentile(public val cutoffs: PercentileCutoffs) : PreferredThroughput
}
