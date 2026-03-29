package dev.toliner.openrouter.l2.routing

import dev.toliner.openrouter.l1.chat.ProviderPreferences
import dev.toliner.openrouter.l2.OpenRouterDslMarker

/**
 * Fluent DSL builder for configuring provider routing preferences.
 *
 * This builder allows you to control how requests are routed to different model providers,
 * including provider ordering, fallback behavior, filtering, and performance requirements.
 * These preferences affect which provider services the request and how failures are handled.
 *
 * Example usage:
 * ```kotlin
 * chatRequest {
 *     model = "openai/gpt-4"
 *     userMessage("Hello!")
 *     provider {
 *         order = listOf("OpenAI", "Azure")
 *         allowFallbacks = true
 *         requireParameters = true
 *         ignore = listOf("Together")
 *     }
 * }
 * ```
 *
 * @see ProviderPreferences for the underlying l1 data model
 * @see ChatRequestBuilder.provider for usage in context
 */
@OpenRouterDslMarker
public class ProviderRoutingBuilder {
    /**
     * Ordered list of provider names to prefer when routing the request.
     *
     * Providers will be tried in the order specified. If the first provider is unavailable
     * or returns an error, the next provider in the list will be tried (if [allowFallbacks]
     * is enabled).
     *
     * Example: `listOf("OpenAI", "Azure", "Together")`
     */
    public var order: List<String>? = null
    
    /**
     * Whether to allow fallback to other providers if the preferred provider fails.
     *
     * When `true`, the request will automatically fall back to other providers in the
     * [order] list if the current provider is unavailable or returns an error.
     *
     * When `false`, the request will fail immediately if the preferred provider cannot
     * service the request.
     *
     * Default behavior (when null) depends on the OpenRouter API settings.
     */
    public var allowFallbacks: Boolean? = null
    
    /**
     * Whether to require that providers support all specified request parameters.
     *
     * When `true`, only providers that support all the parameters in the request
     * (e.g., `temperature`, `topP`, specific tool features) will be considered.
     *
     * When `false`, providers that don't support some parameters may still be used,
     * and unsupported parameters will be ignored.
     *
     * Default behavior (when null) depends on the OpenRouter API settings.
     */
    public var requireParameters: Boolean? = null
    
    /**
     * Data collection preference for privacy control.
     *
     * Controls whether request/response data can be collected by providers for model
     * improvement or other purposes. Valid values depend on the provider and may include
     * "allow", "deny", or other provider-specific options.
     *
     * Example: `"deny"` to opt out of data collection
     */
    public var dataCollection: String? = null
    
    /**
     * Minimum throughput requirement in tokens per second.
     *
     * Only providers that can meet this minimum throughput threshold will be considered
     * for routing. Useful for performance-critical applications that need low latency.
     *
     * Example: `100` requires at least 100 tokens/second throughput
     */
    public var preferredMinThroughput: Int? = null
    
    /**
     * List of provider names to exclude from routing consideration.
     *
     * These providers will not be used to service the request, even if they would
     * otherwise be selected based on other routing preferences.
     *
     * Example: `listOf("Together", "Anthropic")` to exclude specific providers
     */
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
