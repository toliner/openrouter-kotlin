package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.ProviderSortSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sorting strategy for provider selection. Can be a simple string or an advanced object form.
 *
 * - Simple: `"price"`, `"throughput"`, or `"latency"`
 * - Advanced: specify [ProviderSort.Advanced.by] and [ProviderSort.Advanced.partition]
 *
 * @see ProviderPreferences.sort
 */
@Serializable(with = ProviderSortSerializer::class)
public sealed interface ProviderSort {
    @JvmInline
    public value class Simple(public val value: String) : ProviderSort

    @Serializable
    public data class Advanced(
        @SerialName("by")
        val by: String,
        @SerialName("partition")
        val partition: String? = null
    ) : ProviderSort
}
