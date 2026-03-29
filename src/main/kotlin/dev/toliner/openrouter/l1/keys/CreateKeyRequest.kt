package dev.toliner.openrouter.l1.keys

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Request body for creating a new API key.
 *
 * Used to provision a new API key with optional usage limits and initial state.
 *
 * @property name Human-readable name for the new API key (required)
 * @property label Optional label for additional categorization or description
 * @property limit Optional maximum usage limit for this key; null indicates no limit
 * @property disabled Optional initial disabled state; defaults to false (enabled) if not specified
 *
 * @see ApiKey
 * @see UpdateKeyRequest
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class CreateKeyRequest(
    val name: String,
    val label: String? = null,
    val limit: Double? = null,
    val disabled: Boolean? = null
)
