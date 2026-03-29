package dev.toliner.openrouter.l1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about a model's Zero-Day Rate (ZDR) endpoint configuration.
 *
 * ZDR endpoints represent model instances that may be affected by pricing adjustments or rate limits
 * specific to newly released or high-demand models. The multiplier affects the base pricing for these endpoints.
 *
 * @property id Unique identifier for this ZDR endpoint configuration.
 * @property name Human-readable name of the endpoint.
 * @property zdrAffected Whether this endpoint is subject to zero-day rate pricing adjustments.
 * @property multiplier Optional pricing multiplier applied to base costs when ZDR is in effect.
 */
@Serializable
public data class ZdrEndpoint(
    val id: String,
    val name: String,
    @SerialName("zdr_affected")
    val zdrAffected: Boolean,
    val multiplier: Double? = null
)
