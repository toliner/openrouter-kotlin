package dev.toliner.openrouter.l1.guardrails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for bulk assigning API keys to a guardrail.
 *
 * @see GuardrailAssignment
 * @see Guardrail
 */
@Serializable
public data class AddAssignmentRequest(
    @SerialName("key_hashes")
    val keyHashes: List<String>
)
