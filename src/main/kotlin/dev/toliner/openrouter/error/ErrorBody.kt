package dev.toliner.openrouter.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Detailed error information returned by the OpenRouter API.
 *
 * Contains the error message, optional error code, and optional upstream provider error details.
 * This structure is embedded in all [OpenRouterException] subtypes.
 *
 * @property message Human-readable error message describing what went wrong
 * @property code Optional numeric error code for programmatic error handling
 * @property providerError Optional upstream provider error details, present when the error originated from an LLM provider
 * @see OpenRouterException
 * @see ProviderError
 */
@Serializable
public data class ErrorBody(
    val message: String,
    val code: Int? = null,
    @SerialName("provider_error") val providerError: ProviderError? = null,
)

/**
 * Error details from an upstream LLM provider.
 *
 * Present in [ErrorBody.providerError] when the error originated from a specific LLM provider
 * (e.g., OpenAI, Anthropic, Google) rather than from OpenRouter itself.
 *
 * @property message Human-readable error message from the upstream provider
 * @property code Optional numeric error code from the upstream provider
 * @see ErrorBody
 */
@Serializable
public data class ProviderError(
    val message: String,
    val code: Int? = null,
)

@Serializable
internal data class ErrorResponse(
    val error: ErrorBody,
)
