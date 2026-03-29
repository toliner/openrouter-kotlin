package dev.toliner.openrouter.error

/**
 * Base sealed class for all OpenRouter API exceptions.
 *
 * This exception hierarchy maps HTTP status codes and API error responses to typed exceptions,
 * allowing clients to handle specific error conditions programmatically. Each subtype represents
 * a specific error condition returned by the OpenRouter API or detected during streaming.
 *
 * @see ErrorBody
 * @see isRetryable
 */
public sealed class OpenRouterException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /**
     * HTTP 400 Bad Request.
     *
     * Thrown when the request is malformed or contains invalid parameters.
     *
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     */
    public data class BadRequest(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * HTTP 401 Unauthorized.
     *
     * Thrown when authentication fails or API key is missing or invalid.
     *
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     */
    public data class Unauthorized(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * HTTP 402 Payment Required.
     *
     * Thrown when the account has insufficient credits or payment is required to continue.
     *
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     */
    public data class PaymentRequired(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * HTTP 403 Forbidden.
     *
     * Thrown when the request is understood but refused due to permissions or access restrictions.
     *
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     */
    public data class Forbidden(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * HTTP 408 Request Timeout.
     *
     * Thrown when the request times out before the server can respond.
     *
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     */
    public data class RequestTimeout(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * HTTP 429 Too Many Requests.
     *
     * Thrown when rate limits are exceeded. This exception is retryable.
     *
     * @property body The detailed error information returned by the API
     * @property retryAfter Optional number of seconds to wait before retrying, if provided by the server
     * @see ErrorBody
     * @see isRetryable
     */
    public data class TooManyRequests(val body: ErrorBody, val retryAfter: Int?) : OpenRouterException(body.message)

    /**
     * HTTP 502 Bad Gateway.
     *
     * Thrown when the upstream provider returns an error or is unreachable. This exception is retryable.
     *
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     * @see isRetryable
     */
    public data class BadGateway(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * HTTP 503 Service Unavailable.
     *
     * Thrown when the API service is temporarily unavailable. This exception is retryable.
     *
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     * @see isRetryable
     */
    public data class ServiceUnavailable(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * HTTP status code not explicitly mapped to a specific exception type.
     *
     * Thrown for any HTTP error status not covered by other exception subtypes.
     * May be retryable for specific status codes (500, 504, 524, 529).
     *
     * @property statusCode The HTTP status code returned by the server
     * @property body The detailed error information returned by the API
     * @see ErrorBody
     * @see isRetryable
     */
    public data class UnknownError(val statusCode: Int, val body: ErrorBody) : OpenRouterException("HTTP $statusCode: ${body.message}")

    /**
     * Error detected during Server-Sent Events (SSE) stream parsing.
     *
     * Thrown when an error event is received in a streaming response.
     *
     * @property body The detailed error information from the stream
     * @see ErrorBody
     */
    public data class StreamError(val body: ErrorBody) : OpenRouterException(body.message)

    /**
     * Error embedded in a successful HTTP response.
     *
     * Thrown when a 2xx HTTP response contains an error object instead of the expected data.
     *
     * @property body The detailed error information from the response
     * @see ErrorBody
     */
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

/**
 * Extension property to determine if an exception represents a retryable error.
 *
 * Returns `true` for transient errors that may succeed if retried after a delay:
 * - [OpenRouterException.TooManyRequests] (HTTP 429) — rate limit exceeded
 * - [OpenRouterException.BadGateway] (HTTP 502) — upstream provider error
 * - [OpenRouterException.ServiceUnavailable] (HTTP 503) — service temporarily unavailable
 * - [OpenRouterException.UnknownError] with status codes 500, 504, 524, 529 — server errors
 *
 * Returns `false` for all other exception types, indicating permanent errors that should not be retried.
 *
 * @see OpenRouterException.TooManyRequests.retryAfter
 */
public val OpenRouterException.isRetryable: Boolean
    get() = when (this) {
        is OpenRouterException.TooManyRequests -> true
        is OpenRouterException.BadGateway -> true
        is OpenRouterException.ServiceUnavailable -> true
        is OpenRouterException.UnknownError -> statusCode in setOf(500, 504, 524, 529)
        else -> false
    }
