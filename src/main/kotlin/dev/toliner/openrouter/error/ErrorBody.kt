package dev.toliner.openrouter.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorBody(
    val message: String,
    val code: Int? = null,
    @SerialName("provider_error") val providerError: ProviderError? = null,
)

@Serializable
data class ProviderError(
    val message: String,
    val code: Int? = null,
)

@Serializable
data class ErrorResponse(
    val error: ErrorBody,
)
