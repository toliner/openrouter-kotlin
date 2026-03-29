package dev.toliner.openrouter.l1.models

import kotlinx.serialization.Serializable

/**
 * Response containing the total number of available models.
 *
 * Provides a lightweight way to query the total count of models available through
 * the OpenRouter API without retrieving full model metadata.
 *
 * @property count Total number of models available.
 *
 * @see ModelList
 */
@Serializable
public data class ModelsCount(
    val count: Int
)
