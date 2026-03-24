package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OpenRouterDslMarker
class PropertyBuilder(private val type: String) {
    var description: String? = null
    
    fun build(): JsonObject {
        return buildJsonObject {
            put("type", type)
            description?.let { put("description", it) }
        }
    }
}
