package dev.toliner.openrouter.l2.routing

import dev.toliner.openrouter.l1.chat.DataCollection
import dev.toliner.openrouter.l1.chat.MaxPrice
import dev.toliner.openrouter.l1.chat.PercentileCutoffs
import dev.toliner.openrouter.l1.chat.PreferredLatency
import dev.toliner.openrouter.l1.chat.PreferredThroughput
import dev.toliner.openrouter.l1.chat.ProviderPreferences
import dev.toliner.openrouter.l1.chat.ProviderSort
import dev.toliner.openrouter.l1.chat.Quantization
import dev.toliner.openrouter.l2.OpenRouterDslMarker

@OpenRouterDslMarker
public class ProviderRoutingBuilder {
    public var order: List<String>? = null
    public var only: List<String>? = null
    public var ignore: List<String>? = null
    public var allowFallbacks: Boolean? = null
    public var requireParameters: Boolean? = null
    public var dataCollection: DataCollection? = null
    public var zdr: Boolean? = null
    public var enforceDistillableText: Boolean? = null
    public var quantizations: List<Quantization>? = null
    public var sort: ProviderSort? = null
    public var maxPrice: MaxPrice? = null
    public var preferredMinThroughput: PreferredThroughput? = null
    public var preferredMaxLatency: PreferredLatency? = null

    public fun sortBy(strategy: String) {
        sort = ProviderSort.Simple(strategy)
    }

    public fun sort(block: ProviderSortBuilder.() -> Unit) {
        sort = ProviderSortBuilder().apply(block).build()
    }

    public fun maxPrice(block: MaxPriceBuilder.() -> Unit) {
        maxPrice = MaxPriceBuilder().apply(block).build()
    }

    public fun preferredMinThroughput(tokensPerSecond: Double) {
        preferredMinThroughput = PreferredThroughput.Value(tokensPerSecond)
    }

    public fun preferredMinThroughput(block: PercentileCutoffsBuilder.() -> Unit) {
        preferredMinThroughput = PreferredThroughput.Percentile(
            PercentileCutoffsBuilder().apply(block).build()
        )
    }

    public fun preferredMaxLatency(seconds: Double) {
        preferredMaxLatency = PreferredLatency.Value(seconds)
    }

    public fun preferredMaxLatency(block: PercentileCutoffsBuilder.() -> Unit) {
        preferredMaxLatency = PreferredLatency.Percentile(
            PercentileCutoffsBuilder().apply(block).build()
        )
    }

    internal fun build(): ProviderPreferences {
        return ProviderPreferences(
            order = order,
            only = only,
            ignore = ignore,
            allowFallbacks = allowFallbacks,
            requireParameters = requireParameters,
            dataCollection = dataCollection,
            zdr = zdr,
            enforceDistillableText = enforceDistillableText,
            quantizations = quantizations,
            sort = sort,
            maxPrice = maxPrice,
            preferredMinThroughput = preferredMinThroughput,
            preferredMaxLatency = preferredMaxLatency
        )
    }
}

@OpenRouterDslMarker
public class ProviderSortBuilder {
    public var by: String? = null
    public var partition: String? = null

    internal fun build(): ProviderSort.Advanced {
        return ProviderSort.Advanced(
            by = requireNotNull(by) { "sort 'by' is required" },
            partition = partition
        )
    }
}

@OpenRouterDslMarker
public class MaxPriceBuilder {
    public var prompt: String? = null
    public var completion: String? = null
    public var image: String? = null
    public var audio: String? = null
    public var request: String? = null

    internal fun build(): MaxPrice {
        return MaxPrice(
            prompt = prompt,
            completion = completion,
            image = image,
            audio = audio,
            request = request
        )
    }
}

@OpenRouterDslMarker
public class PercentileCutoffsBuilder {
    public var p50: Double? = null
    public var p75: Double? = null
    public var p90: Double? = null
    public var p99: Double? = null

    internal fun build(): PercentileCutoffs {
        return PercentileCutoffs(
            p50 = p50,
            p75 = p75,
            p90 = p90,
            p99 = p99
        )
    }
}
