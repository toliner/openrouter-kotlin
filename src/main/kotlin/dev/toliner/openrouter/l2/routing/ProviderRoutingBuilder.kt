package dev.toliner.openrouter.l2.routing

import dev.toliner.openrouter.l1.chat.ProviderPreferences
import dev.toliner.openrouter.l2.OpenRouterDslMarker

@OpenRouterDslMarker
class ProviderRoutingBuilder {
    var order: List<String>? = null
    var allowFallbacks: Boolean? = null
    var requireParameters: Boolean? = null
    var dataCollection: String? = null
    var preferredMinThroughput: Int? = null
    var ignore: List<String>? = null

    fun build(): ProviderPreferences {
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
