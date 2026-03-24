package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OpenRouterDslMarker
class JsonSchemaBuilder {
    private val properties = mutableMapOf<String, JsonObject>()
    private val requiredFields = mutableListOf<String>()
    
    fun property(name: String, type: String, block: PropertyBuilder.() -> Unit = {}) {
        val prop = PropertyBuilder(type).apply(block).build()
        properties[name] = prop
    }
    
    fun required(vararg names: String) {
        requiredFields.addAll(names)
    }
    
    fun build(): JsonObject {
        return buildJsonObject {
            put("type", "object")
            put("properties", buildJsonObject {
                properties.forEach { (name, schema) ->
                    put(name, schema)
                }
            })
            if (requiredFields.isNotEmpty()) {
                put("required", JsonArray(requiredFields.map { JsonPrimitive(it) }))
            }
        }
    }
}
