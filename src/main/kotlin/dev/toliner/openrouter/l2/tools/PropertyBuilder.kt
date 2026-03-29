package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OpenRouterDslMarker
public class PropertyBuilder(private val type: String) {
    public var description: String? = null
    
    internal fun build(): JsonObject {
        return buildJsonObject {
            put("type", type)
            description?.let { put("description", it) }
        }
    }
}
