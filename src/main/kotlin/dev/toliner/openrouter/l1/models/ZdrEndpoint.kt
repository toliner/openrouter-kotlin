package dev.toliner.openrouter.l1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZdrEndpoint(
    val id: String,
    val name: String,
    @SerialName("zdr_affected")
    val zdrAffected: Boolean,
    val multiplier: Double? = null
)
