package dev.toliner.openrouter.l1.providers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Provider information from the OpenRouter network.
 *
 * This data is retrieved from the `/api/v1/providers` endpoint and provides
 * information about LLM providers available through OpenRouter, including
 * links to their privacy policies, terms of service, and status pages.
 *
 * Providers are the underlying LLM vendors (e.g., OpenAI, Anthropic, Google)
 * whose models are accessible via OpenRouter's unified API.
 *
 * @property name Human-readable provider name (e.g., "OpenAI", "Anthropic", "Google")
 * @property slug URL-safe provider identifier (e.g., "openai", "anthropic", "google")
 * @property privacyPolicyUrl Optional URL to provider's privacy policy
 * @property termsOfServiceUrl Optional URL to provider's terms of service
 * @property statusPageUrl Optional URL to provider's status page for monitoring service availability
 */
@Serializable
public data class Provider(
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("privacy_policy_url") val privacyPolicyUrl: String? = null,
    @SerialName("terms_of_service_url") val termsOfServiceUrl: String? = null,
    @SerialName("status_page_url") val statusPageUrl: String? = null
)
