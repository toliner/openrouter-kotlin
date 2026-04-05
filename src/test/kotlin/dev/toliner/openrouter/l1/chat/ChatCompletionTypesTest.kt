package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ChatCompletionTypesTest : FunSpec({
    test("Message.System serialization round-trip") {
        val message = Message.System(content = "You are a helpful assistant.")
        val json = OpenRouterJson.encodeToString(Message.serializer(), message)
        val decoded = OpenRouterJson.decodeFromString(Message.serializer(), json)
        
        decoded shouldBe message
        json shouldBe """{"role":"system","content":"You are a helpful assistant."}"""
    }
    
    test("Message.User with text content serialization round-trip") {
        val message = Message.User(content = dev.toliner.openrouter.serialization.Content.Text("Hello"))
        val json = OpenRouterJson.encodeToString(Message.serializer(), message)
        val decoded = OpenRouterJson.decodeFromString(Message.serializer(), json)
        
        decoded shouldBe message
        json shouldBe """{"role":"user","content":"Hello"}"""
    }
    
    test("Message.User with multipart content serialization round-trip") {
        val message = Message.User(
            content = dev.toliner.openrouter.serialization.Content.Parts(
                listOf(
                    dev.toliner.openrouter.serialization.ContentPart.TextPart(text = "Hello")
                )
            )
        )
        val json = OpenRouterJson.encodeToString(Message.serializer(), message)
        val decoded = OpenRouterJson.decodeFromString(Message.serializer(), json)
        
        decoded shouldBe message
    }
    
    test("Message.Assistant serialization round-trip") {
        val message = Message.Assistant(
            content = "Hello! How can I help?",
            toolCalls = null
        )
        val json = OpenRouterJson.encodeToString(Message.serializer(), message)
        val decoded = OpenRouterJson.decodeFromString(Message.serializer(), json)
        
        decoded shouldBe message
        json shouldBe """{"role":"assistant","content":"Hello! How can I help?"}"""
    }
    
    test("Message.Tool serialization round-trip") {
        val message = Message.Tool(
            toolCallId = "call_123",
            content = "Result data"
        )
        val json = OpenRouterJson.encodeToString(Message.serializer(), message)
        val decoded = OpenRouterJson.decodeFromString(Message.serializer(), json)
        
        decoded shouldBe message
        json shouldBe """{"role":"tool","tool_call_id":"call_123","content":"Result data"}"""
    }
    
    test("FunctionTool serialization round-trip") {
        val tool = FunctionTool(
            type = "function",
            function = FunctionDefinition(
                name = "get_weather",
                description = "Get the current weather",
                parameters = kotlinx.serialization.json.buildJsonObject {
                    put("type", kotlinx.serialization.json.JsonPrimitive("object"))
                    put("properties", kotlinx.serialization.json.buildJsonObject {
                        put("location", kotlinx.serialization.json.buildJsonObject {
                            put("type", kotlinx.serialization.json.JsonPrimitive("string"))
                        })
                    })
                }
            )
        )
        val json = OpenRouterJson.encodeToString(FunctionTool.serializer(), tool)
        val decoded = OpenRouterJson.decodeFromString(FunctionTool.serializer(), json)
        
        decoded shouldBe tool
    }
    
    test("ToolCall serialization round-trip") {
        val toolCall = ToolCall(
            id = "call_123",
            type = "function",
            function = FunctionCall(
                name = "get_weather",
                arguments = """{"location":"Tokyo"}"""
            )
        )
        val json = OpenRouterJson.encodeToString(ToolCall.serializer(), toolCall)
        val decoded = OpenRouterJson.decodeFromString(ToolCall.serializer(), json)
        
        decoded shouldBe toolCall
    }
    
    test("ResponseFormat serialization round-trip") {
        val format = ResponseFormat.JsonObject
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), format)
        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
        
        decoded shouldBe format
        json shouldBe """{"type":"json_object"}"""
    }
    
    test("ProviderPreferences serialization round-trip") {
        val prefs = ProviderPreferences(
            order = listOf("OpenAI", "Anthropic"),
            allowFallbacks = true,
            requireParameters = true,
            dataCollection = "deny"
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)
        
        decoded shouldBe prefs
    }
    
    test("Usage serialization round-trip") {
        val usage = Usage(
            promptTokens = 10,
            completionTokens = 20,
            totalTokens = 30
        )
        val json = OpenRouterJson.encodeToString(Usage.serializer(), usage)
        val decoded = OpenRouterJson.decodeFromString(Usage.serializer(), json)
        
        decoded shouldBe usage
    }
    
    test("Trace serialization round-trip") {
        val trace = Trace(
            traceId = "trace_123",
            spanName = "completion"
        )
        val json = OpenRouterJson.encodeToString(Trace.serializer(), trace)
        val decoded = OpenRouterJson.decodeFromString(Trace.serializer(), json)
        
        decoded shouldBe trace
    }
    
    test("ChatCompletionRequest minimal serialization") {
        val request = ChatCompletionRequest(
            model = "openai/gpt-3.5-turbo",
            messages = listOf(
                Message.User(content = dev.toliner.openrouter.serialization.Content.Text("Hello"))
            )
        )
        val json = OpenRouterJson.encodeToString(ChatCompletionRequest.serializer(), request)
        val decoded = OpenRouterJson.decodeFromString(ChatCompletionRequest.serializer(), json)
        
        decoded shouldBe request
        json shouldBe """{"model":"openai/gpt-3.5-turbo","messages":[{"role":"user","content":"Hello"}]}"""
    }
    
    test("ChatCompletionRequest full serialization") {
        val request = ChatCompletionRequest(
            model = "openai/gpt-4",
            messages = listOf(
                Message.System(content = "You are helpful"),
                Message.User(content = dev.toliner.openrouter.serialization.Content.Text("Hello"))
            ),
            temperature = 0.7,
            maxTokens = 100,
            topP = 0.9,
            topK = 50,
            frequencyPenalty = 0.5,
            presencePenalty = 0.5,
            repetitionPenalty = 1.0,
            seed = 42,
            stop = dev.toliner.openrouter.serialization.StringOrArray.Single("STOP"),
            stream = false,
            tools = listOf(
                FunctionTool(
                    type = "function",
                    function = FunctionDefinition(name = "test")
                )
            ),
            toolChoice = dev.toliner.openrouter.serialization.ToolChoice.Mode("auto"),
            responseFormat = ResponseFormat.JsonObject,
            provider = ProviderPreferences(order = listOf("OpenAI")),
            trace = Trace(traceId = "trace_1"),
            transforms = listOf("middle-out")
        )
        val json = OpenRouterJson.encodeToString(ChatCompletionRequest.serializer(), request)
        val decoded = OpenRouterJson.decodeFromString(ChatCompletionRequest.serializer(), json)
        
        decoded shouldBe request
    }
    
    test("ChatCompletionResponse normal deserialization") {
        val jsonStr = """
            {
                "id": "gen-123",
                "model": "openai/gpt-3.5-turbo",
                "object": "chat.completion",
                "created": 1234567890,
                "choices": [
                    {
                        "index": 0,
                        "message": {
                            "role": "assistant",
                            "content": "Hello!"
                        },
                        "finish_reason": "stop"
                    }
                ],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 5,
                    "total_tokens": 15
                }
            }
        """.trimIndent()
        
        val response = OpenRouterJson.decodeFromString(ChatCompletionResponse.serializer(), jsonStr)
        
        response.id shouldBe "gen-123"
        response.model shouldBe "openai/gpt-3.5-turbo"
        response.choices.size shouldBe 1
        response.choices[0].message shouldBe Message.Assistant(content = "Hello!", toolCalls = null)
        response.usage?.totalTokens shouldBe 15
    }
    
    test("ChatCompletionResponse with tool_calls deserialization") {
        val jsonStr = """
            {
                "id": "gen-456",
                "model": "openai/gpt-4",
                "object": "chat.completion",
                "created": 1234567890,
                "choices": [
                    {
                        "index": 0,
                        "message": {
                            "role": "assistant",
                            "content": null,
                            "tool_calls": [
                                {
                                    "id": "call_1",
                                    "type": "function",
                                    "function": {
                                        "name": "get_weather",
                                        "arguments": "{\"location\":\"Tokyo\"}"
                                    }
                                }
                            ]
                        },
                        "finish_reason": "tool_calls"
                    }
                ]
            }
        """.trimIndent()
        
        val response = OpenRouterJson.decodeFromString(ChatCompletionResponse.serializer(), jsonStr)
        
        response.id shouldBe "gen-456"
        response.choices[0].message shouldBe Message.Assistant(
            content = null,
            toolCalls = listOf(
                ToolCall(
                    id = "call_1",
                    type = "function",
                    function = FunctionCall(name = "get_weather", arguments = """{"location":"Tokyo"}""")
                )
            )
        )
        response.choices[0].finishReason shouldBe "tool_calls"
    }
    
    test("ChatCompletionChunk delta content deserialization") {
        val jsonStr = """
            {
                "id": "gen-stream-1",
                "model": "openai/gpt-3.5-turbo",
                "object": "chat.completion.chunk",
                "created": 1234567890,
                "choices": [
                    {
                        "index": 0,
                        "delta": {
                            "content": "Hello"
                        }
                    }
                ]
            }
        """.trimIndent()
        
        val chunk = OpenRouterJson.decodeFromString(ChatCompletionChunk.serializer(), jsonStr)
        
        chunk.id shouldBe "gen-stream-1"
        chunk.choices[0].delta.content shouldBe "Hello"
    }
    
    test("ChatCompletionChunk delta tool_calls deserialization") {
        val jsonStr = """
            {
                "id": "gen-stream-2",
                "model": "openai/gpt-4",
                "object": "chat.completion.chunk",
                "created": 1234567890,
                "choices": [
                    {
                        "index": 0,
                        "delta": {
                            "tool_calls": [
                                {
                                    "index": 0,
                                    "id": "call_1",
                                    "type": "function",
                                    "function": {
                                        "name": "get_weather",
                                        "arguments": "{\"location\""
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        """.trimIndent()
        
        val chunk = OpenRouterJson.decodeFromString(ChatCompletionChunk.serializer(), jsonStr)
        
        chunk.choices[0].delta.toolCalls?.get(0)?.id shouldBe "call_1"
    }
    
    test("ChatCompletionChunk finish_reason and usage deserialization") {
        val jsonStr = """
            {
                "id": "gen-stream-3",
                "model": "openai/gpt-3.5-turbo",
                "object": "chat.completion.chunk",
                "created": 1234567890,
                "choices": [
                    {
                        "index": 0,
                        "delta": {},
                        "finish_reason": "stop"
                    }
                ],
                "usage": {
                    "prompt_tokens": 10,
                    "completion_tokens": 20,
                    "total_tokens": 30
                }
            }
        """.trimIndent()
        
        val chunk = OpenRouterJson.decodeFromString(ChatCompletionChunk.serializer(), jsonStr)
        
        chunk.choices[0].finishReason shouldBe "stop"
        chunk.usage?.totalTokens shouldBe 30
    }
})
