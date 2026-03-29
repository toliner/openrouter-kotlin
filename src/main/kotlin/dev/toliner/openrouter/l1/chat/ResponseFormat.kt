package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Specifies the format of the model's output.
 *
 * Response format constraints allow requesting specific output structures.
 * For example, "json_object" mode instructs the model to return valid JSON.
 *
 * Common values:
 * - "text": Standard text output (default)
 * - "json_object": Model will return a valid JSON object
 *
 * Note: Not all models support all response format types.
 *
 * @property type The response format type.
 *
 * @see ChatCompletionRequest.responseFormat
 */
@Serializable
public data class ResponseFormat(
    @SerialName("type")
    val type: String
)
