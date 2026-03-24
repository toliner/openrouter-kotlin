package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.l1.chat.*
import dev.toliner.openrouter.l2.OpenRouterDslMarker
import dev.toliner.openrouter.serialization.Content
import dev.toliner.openrouter.serialization.StringOrArray
import dev.toliner.openrouter.serialization.ToolChoice

@OpenRouterDslMarker
class ChatRequestBuilder {
    var model: String? = null
    var temperature: Double? = null
    var maxTokens: Int? = null
    var topP: Double? = null
    var topK: Int? = null
    var frequencyPenalty: Double? = null
    var presencePenalty: Double? = null
    var repetitionPenalty: Double? = null
    var seed: Int? = null
    var stop: StringOrArray? = null
    var tools: List<FunctionTool>? = null
    var toolChoice: ToolChoice? = null
    var responseFormat: ResponseFormat? = null
    var provider: ProviderPreferences? = null
    var trace: Trace? = null
    var transforms: List<String>? = null
    var route: String? = null
    var models: List<String>? = null
    
    private val messages = mutableListOf<Message>()
    
    fun systemMessage(content: String) {
        messages.add(Message.System(content))
    }
    
    fun userMessage(content: String) {
        messages.add(Message.User(Content.Text(content)))
    }
    
    fun userMessage(block: () -> Content) {
        messages.add(Message.User(block()))
    }
    
    fun assistantMessage(content: String, toolCalls: List<ToolCall>? = null) {
        messages.add(Message.Assistant(content = content, toolCalls = toolCalls))
    }
    
    fun toolMessage(toolCallId: String, content: String) {
        messages.add(Message.Tool(toolCallId = toolCallId, content = content))
    }
    
    fun build(): ChatCompletionRequest {
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
