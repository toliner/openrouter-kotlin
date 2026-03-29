package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents distributed tracing information for observability.
 *
 * Trace information can be attached to requests to enable correlation across
 * services and detailed performance monitoring. Useful for debugging and
 * monitoring in production environments.
 *
 * @property traceId The unique identifier for this trace. Should follow distributed tracing standards (e.g., W3C Trace Context).
 * @property spanName Optional name for the span within the trace. Helps identify this specific operation.
 *
 * @see ChatCompletionRequest.trace
 */
@Serializable
public data class Trace(
    @SerialName("trace_id")
    val traceId: String,
    @SerialName("span_name")
    val spanName: String? = null
)
