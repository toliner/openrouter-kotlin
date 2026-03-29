package dev.toliner.openrouter.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ErrorBody(
    val message: String,
    val code: Int? = null,
    @SerialName("provider_error") val providerError: ProviderError? = null,
)

@Serializable
public data class ProviderError(
    val message: String,
    val code: Int? = null,
)

@Serializable
internal data class ErrorResponse(
    val error: ErrorBody,
)
