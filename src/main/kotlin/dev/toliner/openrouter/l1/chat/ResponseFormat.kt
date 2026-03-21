package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseFormat(
    @SerialName("type")
    val type: String
)
