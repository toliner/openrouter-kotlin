package dev.toliner.openrouter.l1.models

import kotlinx.serialization.Serializable

@Serializable
data class ModelList(
    val data: List<Model>
)
