package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.l1.chat.*
import dev.toliner.openrouter.l2.OpenRouterDslMarker
import dev.toliner.openrouter.l2.routing.ProviderRoutingBuilder
import dev.toliner.openrouter.l2.tools.ToolsBuilder
import dev.toliner.openrouter.serialization.Content
import dev.toliner.openrouter.serialization.StringOrArray
import dev.toliner.openrouter.serialization.ToolChoice

/**
 * Fluent DSL builder for constructing chat completion requests.
 *
 * This builder provides a type-safe, Kotlin-idiomatic way to construct [ChatCompletionRequest] instances.
 * All properties mirror the OpenRouter Chat Completions API parameters. The builder enforces required
 * fields (model, messages) at build time and provides convenience methods for adding messages.
 *
 * Example usage:
 * ```kotlin
 * val request = chatRequest {
 *     model = "openai/gpt-4"
 *     systemMessage("You are a helpful assistant")
 *     userMessage("Tell me about Kotlin")
 *     temperature = 0.7
 *     maxTokens = 500
 *     tools {
 *         function("get_weather") {
 *             description = "Get current weather for a location"
 *             parameters {
 *                 property("location", "string") {
 *                     description = "City name"
 *                 }
 *                 required("location")
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @see ChatCompletionRequest for the underlying l1 data model
 * @see chatRequest for the entry-point function
 */
@OpenRouterDslMarker
public class ChatRequestBuilder {
    /**
     * The ID of the model to use for completion (required).
     *
     * Example: "openai/gpt-4", "anthropic/claude-3-opus", "google/gemini-pro"
     */
    public var model: String? = null
    
    /**
     * Controls randomness in the output. Higher values (e.g., 1.0) make output more random,
     * lower values (e.g., 0.2) make it more focused and deterministic.
     *
     * Valid range: 0.0 to 2.0 (model-dependent)
     */
    public var temperature: Double? = null
    
    /**
     * Maximum number of tokens to generate in the completion.
     *
     * The total length of input tokens and generated tokens is limited by the model's context length.
     */
    public var maxTokens: Int? = null
    
    /**
     * Nucleus sampling parameter. Only tokens with cumulative probability up to topP are considered.
     *
     * Example: 0.1 means only the tokens comprising the top 10% probability mass are considered.
     * Valid range: 0.0 to 1.0. Recommended to alter this OR temperature, not both.
     */
    public var topP: Double? = null
    
    /**
     * Limits the number of highest probability vocabulary tokens to consider at each generation step.
     *
     * Example: 40 means only the top 40 tokens are considered.
     * Valid range: typically 1 to 100 (model-dependent).
     */
    public var topK: Int? = null
    
    /**
     * Penalizes tokens based on their frequency in the generated text so far.
     *
     * Positive values discourage repetition. Valid range: -2.0 to 2.0.
     */
    public var frequencyPenalty: Double? = null
    
    /**
     * Penalizes tokens based on whether they have appeared in the generated text so far.
     *
     * Positive values encourage the model to talk about new topics. Valid range: -2.0 to 2.0.
     */
    public var presencePenalty: Double? = null
    
    /**
     * Alternative repetition penalty (provider-specific).
     *
     * Penalizes repetition in the generated text. Valid range and behavior varies by provider.
     */
    public var repetitionPenalty: Double? = null
    
    /**
     * Seed for deterministic sampling.
     *
     * If specified, the model will make a best effort to sample deterministically for the same seed
     * and parameters. Determinism is not guaranteed across different model versions.
     */
    public var seed: Int? = null
    
    /**
     * Sequences where the model should stop generating further tokens.
     *
     * Can be a single string or an array of strings. Generation stops when any of these sequences
     * are encountered. Maximum 4 stop sequences.
     */
    public var stop: StringOrArray? = null
    
    /**
     * List of function tools available to the model during generation.
     *
     * Use the [tools] DSL method to configure tools instead of setting this directly.
     * @see tools for the DSL configuration method
     */
    public var tools: List<FunctionTool>? = null
    
    /**
     * Controls which (if any) tool is called by the model.
     *
     * Can be "none", "auto", "required", or an object specifying a particular function.
     * @see ToolChoice for available options
     */
    public var toolChoice: ToolChoice? = null
    
    /**
     * Format specification for the model's output.
     *
     * Use this to request JSON mode or other structured output formats.
     * @see ResponseFormat for available options
     */
    public var responseFormat: ResponseFormat? = null
    
    /**
     * Provider routing preferences for the request.
     *
     * Use the [provider] DSL method to configure routing instead of setting this directly.
     * @see provider for the DSL configuration method
     */
    public var provider: ProviderPreferences? = null
    
    /**
     * Trace configuration for debugging and observability.
     *
     * @see Trace for available options
     */
    public var trace: Trace? = null
    
    /**
     * List of transform operations to apply to the request or response.
     *
     * Provider-specific transformations for content moderation, formatting, etc.
     */
    public var transforms: List<String>? = null
    
    /**
     * Custom routing path for the request.
     *
     * Used for advanced routing scenarios and provider selection.
     */
    public var route: String? = null
    
    /**
     * List of model IDs to try in order (fallback routing).
     *
     * The API will try each model in sequence until one succeeds.
     */
    public var models: List<String>? = null
    
    private val messages = mutableListOf<Message>()
    
    /**
     * Adds a system message to the conversation.
     *
     * System messages typically contain instructions or context that guide the model's behavior
     * throughout the conversation.
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4"
     *     systemMessage("You are a helpful coding assistant specializing in Kotlin")
     * }
     * ```
     *
     * @param content The system message content
     * @see Message.System
     */
    public fun systemMessage(content: String) {
        messages.add(Message.System(content))
    }
    
    /**
     * Adds a user message with text content to the conversation.
     *
     * User messages represent input from the end user or application.
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4"
     *     userMessage("What is the capital of France?")
     * }
     * ```
     *
     * @param content The user message text content
     * @see Message.User
     * @see userMessage with structured content block
     */
    public fun userMessage(content: String) {
        messages.add(Message.User(Content.Text(content)))
    }
    
    /**
     * Adds a user message with structured content to the conversation.
     *
     * This overload allows for multi-modal content including text, images, and other content types.
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4-vision"
     *     userMessage {
     *         Content.MultiPart(listOf(
     *             ContentPart.Text("What's in this image?"),
     *             ContentPart.ImageUrl(ImageUrl("https://example.com/image.jpg"))
     *         ))
     *     }
     * }
     * ```
     *
     * @param block Lambda that returns the structured [Content]
     * @see Message.User
     * @see Content
     */
    public fun userMessage(block: () -> Content) {
        messages.add(Message.User(block()))
    }
    
    /**
     * Adds an assistant message to the conversation.
     *
     * Assistant messages represent previous responses from the model. These are typically used
     * when continuing a conversation or providing examples (few-shot learning).
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4"
     *     userMessage("What is 2+2?")
     *     assistantMessage("2+2 equals 4")
     *     userMessage("What about 3+3?")
     * }
     * ```
     *
     * @param content The assistant message content
     * @param toolCalls Optional list of tool calls made by the assistant (for function calling workflows)
     * @see Message.Assistant
     * @see ToolCall
     */
    public fun assistantMessage(content: String, toolCalls: List<ToolCall>? = null) {
        messages.add(Message.Assistant(content = content, toolCalls = toolCalls))
    }
    
    /**
     * Adds a tool result message to the conversation.
     *
     * Tool messages contain the results from function/tool calls. They must reference a specific
     * tool call ID from a previous assistant message.
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4"
     *     userMessage("What's the weather in Tokyo?")
     *     assistantMessage("", toolCalls = listOf(
     *         ToolCall(id = "call_123", type = "function", function = FunctionCall(
     *             name = "get_weather",
     *             arguments = """{"location": "Tokyo"}"""
     *         ))
     *     ))
     *     toolMessage("call_123", """{"temperature": 22, "condition": "sunny"}""")
     * }
     * ```
     *
     * @param toolCallId The ID of the tool call this message is responding to
     * @param content The tool's output/result as a string (typically JSON)
     * @see Message.Tool
     * @see ToolCall
     */
    public fun toolMessage(toolCallId: String, content: String) {
        messages.add(Message.Tool(toolCallId = toolCallId, content = content))
    }
    
    /**
     * Configures function tools using a DSL builder.
     *
     * Tools define functions that the model can call during generation. The model will provide
     * arguments in the response, and you can execute the function and return results via [toolMessage].
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4"
     *     userMessage("What's the weather in Paris?")
     *     tools {
     *         function("get_weather") {
     *             description = "Get the current weather in a location"
     *             parameters {
     *                 property("location", "string") {
     *                     description = "The city name"
     *                 }
     *                 property("unit", "string") {
     *                     description = "Temperature unit (celsius or fahrenheit)"
     *                 }
     *                 required("location")
     *             }
     *         }
     *     }
     * }
     * ```
     *
     * @param block Configuration block executed in the context of [ToolsBuilder]
     * @see ToolsBuilder
     * @see FunctionTool
     */
    public fun tools(block: ToolsBuilder.() -> Unit) {
        this.tools = ToolsBuilder().apply(block).build()
    }

    /**
     * Configures provider routing preferences using a DSL builder.
     *
     * Provider preferences control how the request is routed to different model providers,
     * including fallback behavior, provider ordering, and filtering.
     *
     * Example:
     * ```kotlin
     * chatRequest {
     *     model = "openai/gpt-4"
     *     userMessage("Hello!")
     *     provider {
     *         order = listOf("OpenAI", "Together")
     *         allowFallbacks = true
     *         requireParameters = true
     *     }
     * }
     * ```
     *
     * @param block Configuration block executed in the context of [ProviderRoutingBuilder]
     * @see ProviderRoutingBuilder
     * @see ProviderPreferences
     */
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
