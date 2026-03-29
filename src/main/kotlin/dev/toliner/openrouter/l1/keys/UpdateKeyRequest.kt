package dev.toliner.openrouter.l1.keys

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Request body for updating an existing API key.
 *
 * Used to modify properties of an existing API key. All fields are optional; only provided fields
 * will be updated.
 *
 * @property label New label value; null to leave unchanged
 * @property limit New usage limit; null to leave unchanged
 * @property disabled New disabled state; null to leave unchanged
 *
 * @see ApiKey
 * @see CreateKeyRequest
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class UpdateKeyRequest(
    val label: String? = null,
    val limit: Double? = null,
    val disabled: Boolean? = null
)
