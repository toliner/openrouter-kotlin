package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Controls whether to use providers that may store or log prompts.
 *
 * @see ProviderPreferences.dataCollection
 */
@Serializable
public enum class DataCollection {
    @SerialName("allow")
    ALLOW,

    @SerialName("deny")
    DENY
}
