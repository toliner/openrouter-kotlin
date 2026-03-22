package dev.toliner.openrouter.l1.keys

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class UpdateKeyRequest(
    val label: String? = null,
    val limit: Double? = null,
    val disabled: Boolean? = null
)
