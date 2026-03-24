package dev.toliner.openrouter.l1.responses

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ResponsesTypesTest : FunSpec({
    test("InputItem.Message serialization round-trip") {
        val item = InputItem.Message(
            role = "user",
            content = "Hello, world!"
        )
        val json = OpenRouterJson.encodeToString(InputItem.serializer(), item)
        val decoded = OpenRouterJson.decodeFromString(InputItem.serializer(), json)
        
        decoded shouldBe item
        json shouldBe """{"type":"message","role":"user","content":"Hello, world!"}"""
    }
    
    test("InputItem.FunctionCallOutput serialization round-trip") {
        val item = InputItem.FunctionCallOutput(
            callId = "call_123",
            output = """{"result":"success"}"""
        )
        val json = OpenRouterJson.encodeToString(InputItem.serializer(), item)
        val decoded = OpenRouterJson.decodeFromString(InputItem.serializer(), json)
        
        decoded shouldBe item
        json shouldBe """{"type":"function_call_output","call_id":"call_123","output":"{\"result\":\"success\"}"}"""
    }
    
    test("ResponseInput.Text serialization") {
        val input = ResponseInput.Text("What is the weather?")
        val json = OpenRouterJson.encodeToString(ResponseInput.serializer(), input)
        
        json shouldBe """"What is the weather?""""
    }
    
    test("ResponseInput.Text deserialization") {
        val json = """"What is the weather?""""
        val decoded = OpenRouterJson.decodeFromString(ResponseInput.serializer(), json)
        
        decoded shouldBe ResponseInput.Text("What is the weather?")
    }
    
    test("ResponseInput.Items serialization") {
        val input = ResponseInput.Items(
            listOf(
                InputItem.Message(role = "user", content = "Hello"),
                InputItem.FunctionCallOutput(callId = "call_123", output = "Done")
            )
        )
        val json = OpenRouterJson.encodeToString(ResponseInput.serializer(), input)
        val decoded = OpenRouterJson.decodeFromString(ResponseInput.serializer(), json)
        
        decoded shouldBe input
    }
    
    test("ResponseInput.Items deserialization from array") {
        val json = """[{"type":"message","role":"user","content":"Hello"}]"""
        val decoded = OpenRouterJson.decodeFromString(ResponseInput.serializer(), json)
        
        decoded shouldBe ResponseInput.Items(
            listOf(InputItem.Message(role = "user", content = "Hello"))
        )
    }
    
    test("ResponseTool serialization round-trip") {
        val tool = ResponseTool(
            type = "function",
            name = "get_weather",
            description = "Get current weather",
            parameters = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("location", buildJsonObject {
                        put("type", "string")
                    })
                })
            }
        )
        val json = OpenRouterJson.encodeToString(ResponseTool.serializer(), tool)
        val decoded = OpenRouterJson.decodeFromString(ResponseTool.serializer(), json)
        
        decoded shouldBe tool
    }
    
    test("CreateResponseRequest with text input serialization") {
        val request = CreateResponseRequest(
            model = "openai/gpt-4",
            input = ResponseInput.Text("What is AI?"),
            instructions = "Be concise"
        )
        val json = OpenRouterJson.encodeToString(CreateResponseRequest.serializer(), request)
        
        json shouldBe """{"model":"openai/gpt-4","input":"What is AI?","instructions":"Be concise"}"""
    }
    
    test("CreateResponseRequest with array input serialization") {
        val request = CreateResponseRequest(
            model = "openai/gpt-4",
            input = ResponseInput.Items(
                listOf(
                    InputItem.Message(role = "user", content = "Hello")
                )
            ),
            instructions = "Be helpful"
        )
        val json = OpenRouterJson.encodeToString(CreateResponseRequest.serializer(), request)
        val decoded = OpenRouterJson.decodeFromString(CreateResponseRequest.serializer(), json)
        
        decoded shouldBe request
    }
    
    test("CreateResponseRequest with tools serialization") {
        val request = CreateResponseRequest(
            model = "openai/gpt-4",
            input = ResponseInput.Text("Get weather"),
            tools = listOf(
                ResponseTool(
                    type = "function",
                    name = "get_weather",
                    description = "Get weather",
                    parameters = buildJsonObject {
                        put("type", "object")
                    }
                )
            )
        )
        val json = OpenRouterJson.encodeToString(CreateResponseRequest.serializer(), request)
        val decoded = OpenRouterJson.decodeFromString(CreateResponseRequest.serializer(), json)
        
        decoded shouldBe request
    }
    
    test("CreateResponseRequest minimal serialization") {
        val request = CreateResponseRequest(
            model = "openai/gpt-4",
            input = ResponseInput.Text("Hello")
        )
        val json = OpenRouterJson.encodeToString(CreateResponseRequest.serializer(), request)
        
        json shouldBe """{"model":"openai/gpt-4","input":"Hello"}"""
    }
    
    test("ResponseObject deserialization") {
        val json = """
        {
            "id": "resp_123",
            "object": "response",
            "created": 1234567890,
            "model": "openai/gpt-4",
            "choices": [
                {
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "Hello!"
                    },
                    "finish_reason": "stop"
                }
            ]
        }
        """.trimIndent()
        
        val response = OpenRouterJson.decodeFromString(ResponseObject.serializer(), json)
        
        response.id shouldBe "resp_123"
        response.objectType shouldBe "response"
        response.created shouldBe 1234567890
        response.model shouldBe "openai/gpt-4"
        response.choices.size shouldBe 1
        response.choices[0].index shouldBe 0
        response.choices[0].message.role shouldBe "assistant"
        response.choices[0].message.content shouldBe "Hello!"
        response.choices[0].finishReason shouldBe "stop"
    }
    
    test("ResponseObject with tool calls deserialization") {
        val json = """
        {
            "id": "resp_456",
            "object": "response",
            "created": 1234567890,
            "model": "openai/gpt-4",
            "choices": [
                {
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": null,
                        "tool_calls": [
                            {
                                "id": "call_123",
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
        
        val response = OpenRouterJson.decodeFromString(ResponseObject.serializer(), json)
        
        response.id shouldBe "resp_456"
        response.choices[0].message.toolCalls?.size shouldBe 1
        response.choices[0].message.toolCalls?.get(0)?.id shouldBe "call_123"
        response.choices[0].message.toolCalls?.get(0)?.function?.name shouldBe "get_weather"
        response.choices[0].finishReason shouldBe "tool_calls"
    }
})
