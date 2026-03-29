package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l1.chat.FunctionTool
import dev.toliner.openrouter.l2.OpenRouterDslMarker

@OpenRouterDslMarker
public class ToolsBuilder {
    private val tools = mutableListOf<FunctionTool>()
    
    public fun function(name: String, block: FunctionToolBuilder.() -> Unit) {
        tools.add(FunctionToolBuilder(name).apply(block).build())
    }
    
    internal fun build(): List<FunctionTool> = tools.toList()
}
