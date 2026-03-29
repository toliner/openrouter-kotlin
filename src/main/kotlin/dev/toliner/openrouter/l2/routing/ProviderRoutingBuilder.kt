package dev.toliner.openrouter.l2.routing

import dev.toliner.openrouter.l1.chat.ProviderPreferences
import dev.toliner.openrouter.l2.OpenRouterDslMarker

@OpenRouterDslMarker
public class ProviderRoutingBuilder {
    public var order: List<String>? = null
    public var allowFallbacks: Boolean? = null
    public var requireParameters: Boolean? = null
    public var dataCollection: String? = null
    public var preferredMinThroughput: Int? = null
    public var ignore: List<String>? = null

    internal fun build(): ProviderPreferences {
        return ProviderPreferences(
            order = order,
            allowFallbacks = allowFallbacks,
            requireParameters = requireParameters,
            dataCollection = dataCollection,
            sort = null,
            preferredMinThroughput = preferredMinThroughput,
            ignore = ignore
        )
    }
}
