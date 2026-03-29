package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.l1.chat.*
import dev.toliner.openrouter.l2.OpenRouterDslMarker
import dev.toliner.openrouter.l2.routing.ProviderRoutingBuilder
import dev.toliner.openrouter.l2.tools.ToolsBuilder
import dev.toliner.openrouter.serialization.Content
import dev.toliner.openrouter.serialization.StringOrArray
import dev.toliner.openrouter.serialization.ToolChoice

@OpenRouterDslMarker
public class ChatRequestBuilder {
    public var model: String? = null
    public var temperature: Double? = null
    public var maxTokens: Int? = null
    public var topP: Double? = null
    public var topK: Int? = null
    public var frequencyPenalty: Double? = null
    public var presencePenalty: Double? = null
    public var repetitionPenalty: Double? = null
    public var seed: Int? = null
    public var stop: StringOrArray? = null
    public var tools: List<FunctionTool>? = null
    public var toolChoice: ToolChoice? = null
    public var responseFormat: ResponseFormat? = null
    public var provider: ProviderPreferences? = null
    public var trace: Trace? = null
    public var transforms: List<String>? = null
    public var route: String? = null
    public var models: List<String>? = null
    
    private val messages = mutableListOf<Message>()
    
    public fun systemMessage(content: String) {
        messages.add(Message.System(content))
    }
    
    public fun userMessage(content: String) {
        messages.add(Message.User(Content.Text(content)))
    }
    
    public fun userMessage(block: () -> Content) {
        messages.add(Message.User(block()))
    }
    
    public fun assistantMessage(content: String, toolCalls: List<ToolCall>? = null) {
        messages.add(Message.Assistant(content = content, toolCalls = toolCalls))
    }
    
    public fun toolMessage(toolCallId: String, content: String) {
        messages.add(Message.Tool(toolCallId = toolCallId, content = content))
    }
    
    public fun tools(block: ToolsBuilder.() -> Unit) {
        this.tools = ToolsBuilder().apply(block).build()
    }

    public fun provider(block: ProviderRoutingBuilder.() -> Unit) {
        this.provider = ProviderRoutingBuilder().apply(block).build()
    }

    internal fun build(): ChatCompletionRequest {
        requireNotNull(model) { "model is required" }
        require(messages.isNotEmpty()) { "at least one message is required" }
        
        return ChatCompletionRequest(
            model = model!!,
            messages = messages.toList(),
            temperature = temperature,
            maxTokens = maxTokens,
            topP = topP,
            topK = topK,
            frequencyPenalty = frequencyPenalty,
            presencePenalty = presencePenalty,
            repetitionPenalty = repetitionPenalty,
            seed = seed,
            stop = stop,
            stream = false,
            tools = tools,
            toolChoice = toolChoice,
            responseFormat = responseFormat,
            provider = provider,
            trace = trace,
            transforms = transforms,
            route = route,
            models = models
        )
    }
}
