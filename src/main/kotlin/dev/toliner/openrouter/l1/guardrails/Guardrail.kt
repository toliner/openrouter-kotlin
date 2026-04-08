package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a guardrail in the OpenRouter Guardrails API.
 *
 * @property id Unique identifier for this guardrail.
 * @property name Human-readable name for the guardrail.
 * @property description Optional description of the guardrail's purpose.
 * @property limitUsd Spending limit in USD.
 * @property resetInterval Interval at which the limit resets (daily, weekly, monthly).
 * @property allowedProviders List of allowed provider IDs.
 * @property ignoredProviders List of provider IDs to exclude from routing.
 * @property allowedModels Array of model canonical slugs (immutable identifiers).
 * @property enforceZdr Whether to enforce zero data retention.
 * @property createdAt ISO 8601 timestamp of when the guardrail was created.
 * @property updatedAt ISO 8601 timestamp of when the guardrail was last updated.
 *
 * @see CreateGuardrailRequest
 * @see UpdateGuardrailRequest
 */
@Serializable
public data class Guardrail(
    @SerialName("id")
    val id: String,
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
    val enforceZdr: Boolean? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
