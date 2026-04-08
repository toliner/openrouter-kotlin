package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maximum price constraints for provider selection.
 *
 * All prices are in USD per million tokens (or per unit for images/audio/requests).
 * Requests are **hard-blocked** (not just deprioritized) if no provider meets
 * all specified price constraints.
 *
 * Price values are strings representing decimal numbers (BigNumber format).
 *
 * @property prompt Maximum price per million prompt tokens.
 * @property completion Maximum price per million completion tokens.
 * @property image Maximum price per image.
 * @property audio Maximum price per audio unit.
 * @property request Maximum price per request.
 *
 * @see ProviderPreferences.maxPrice
 */
@Serializable
public data class MaxPrice(
    @SerialName("prompt")
    val prompt: String? = null,
    @SerialName("completion")
    val completion: String? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("audio")
    val audio: String? = null,
    @SerialName("request")
    val request: String? = null
)
