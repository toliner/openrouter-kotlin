package dev.toliner.openrouter.l1.providers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Provider information.
 *
 * @property name Provider name (e.g., "OpenAI", "Anthropic")
 * @property slug Provider slug identifier (e.g., "openai", "anthropic")
 * @property privacyPolicyUrl URL to provider's privacy policy
 * @property termsOfServiceUrl URL to provider's terms of service
 * @property statusPageUrl URL to provider's status page
 */
@Serializable
data class Provider(
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("privacy_policy_url") val privacyPolicyUrl: String? = null,
    @SerialName("terms_of_service_url") val termsOfServiceUrl: String? = null,
    @SerialName("status_page_url") val statusPageUrl: String? = null
)
