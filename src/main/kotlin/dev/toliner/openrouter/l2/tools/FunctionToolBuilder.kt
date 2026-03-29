package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l1.chat.FunctionDefinition
import dev.toliner.openrouter.l1.chat.FunctionTool
import dev.toliner.openrouter.l2.OpenRouterDslMarker
import kotlinx.serialization.json.JsonObject

@OpenRouterDslMarker
public class FunctionToolBuilder(private val name: String) {
    public var description: String? = null
    private var parametersSchema: JsonObject? = null
    
    public fun parameters(block: JsonSchemaBuilder.() -> Unit) {
        parametersSchema = JsonSchemaBuilder().apply(block).build()
    }
    
    internal fun build(): FunctionTool {
        return FunctionTool(
            type = "function",
            function = FunctionDefinition(
                name = name,
                description = description,
                parameters = parametersSchema
            )
        )
    }
}
