package dev.toliner.openrouter.l1.responses

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@ExperimentalOpenRouterApi
@Serializable
data class ResponseTool(
    @SerialName("type")
    val type: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("parameters")
    val parameters: JsonObject? = null
)
