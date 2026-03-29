package dev.toliner.openrouter.error

public sealed class OpenRouterException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    public data class BadRequest(val body: ErrorBody) : OpenRouterException(body.message)
    public data class Unauthorized(val body: ErrorBody) : OpenRouterException(body.message)
    public data class PaymentRequired(val body: ErrorBody) : OpenRouterException(body.message)
    public data class Forbidden(val body: ErrorBody) : OpenRouterException(body.message)
    public data class RequestTimeout(val body: ErrorBody) : OpenRouterException(body.message)
    public data class TooManyRequests(val body: ErrorBody, val retryAfter: Int?) : OpenRouterException(body.message)
    public data class BadGateway(val body: ErrorBody) : OpenRouterException(body.message)
    public data class ServiceUnavailable(val body: ErrorBody) : OpenRouterException(body.message)
    public data class UnknownError(val statusCode: Int, val body: ErrorBody) : OpenRouterException("HTTP $statusCode: ${body.message}")
    public data class StreamError(val body: ErrorBody) : OpenRouterException(body.message)
    public data class InBandError(val body: ErrorBody) : OpenRouterException(body.message)
}

internal fun errorFromStatus(statusCode: Int, body: ErrorBody, retryAfter: Int? = null): OpenRouterException = when (statusCode) {
    400 -> OpenRouterException.BadRequest(body)
    401 -> OpenRouterException.Unauthorized(body)
    402 -> OpenRouterException.PaymentRequired(body)
    403 -> OpenRouterException.Forbidden(body)
    408 -> OpenRouterException.RequestTimeout(body)
    429 -> OpenRouterException.TooManyRequests(body, retryAfter)
    502 -> OpenRouterException.BadGateway(body)
    503 -> OpenRouterException.ServiceUnavailable(body)
    else -> OpenRouterException.UnknownError(statusCode, body)
}

public val OpenRouterException.isRetryable: Boolean
    get() = when (this) {
        is OpenRouterException.TooManyRequests -> true
        is OpenRouterException.BadGateway -> true
        is OpenRouterException.ServiceUnavailable -> true
        is OpenRouterException.UnknownError -> statusCode in setOf(500, 504, 524, 529)
        else -> false
    }
