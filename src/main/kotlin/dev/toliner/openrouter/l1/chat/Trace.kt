package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trace(
    @SerialName("trace_id")
    val traceId: String,
    @SerialName("span_name")
    val spanName: String? = null
)
