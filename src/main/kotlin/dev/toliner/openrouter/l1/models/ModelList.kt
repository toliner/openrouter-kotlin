package dev.toliner.openrouter.l1.models

import kotlinx.serialization.Serializable

/**
 * Response wrapper containing a list of available models.
 *
 * Returned by the OpenRouter `/models` endpoint. Contains the collection of all models
 * available for use through the OpenRouter API.
 *
 * @property data List of model metadata objects.
 *
 * @see Model
 */
@Serializable
public data class ModelList(
    val data: List<Model>
)
