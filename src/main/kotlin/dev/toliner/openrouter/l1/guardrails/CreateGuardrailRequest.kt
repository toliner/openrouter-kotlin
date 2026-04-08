package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for creating a new guardrail.
 *
 * @see Guardrail
 */
@Serializable
public data class CreateGuardrailRequest(
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("limit_usd")
    val limitUsd: Double? = null,
    @SerialName("reset_interval")
    val resetInterval: String? = null,
    @SerialName("allowed_providers")
    val allowedProviders: List<String>? = null,
    @SerialName("ignored_providers")
    val ignoredProviders: List<String>? = null,
    @SerialName("allowed_models")
    val allowedModels: List<String>? = null,
    @SerialName("enforce_zdr")
    val enforceZdr: Boolean? = null
)
