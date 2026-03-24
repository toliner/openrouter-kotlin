package dev.toliner.openrouter.client

import dev.toliner.openrouter.ExperimentalOpenRouterApi
import dev.toliner.openrouter.l1.responses.CreateResponseRequest
import dev.toliner.openrouter.l1.responses.ResponseInput
import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.testutil.mockEngineWithResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalOpenRouterApi::class)
class ResponsesApiTest : FunSpec({
    test("create() sends POST /responses with CreateResponseRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
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
                                "content": "Hello! I'm an AI assistant."
                            },
                            "finish_reason": "stop"
                        }
                    ]
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/responses"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["model"]?.jsonPrimitive?.content shouldBe "openai/gpt-4"
                bodyJson["input"]?.jsonPrimitive?.content shouldBe "What is AI?"
                bodyJson["instructions"]?.jsonPrimitive?.content shouldBe "Be concise"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = CreateResponseRequest(
                model = "openai/gpt-4",
                input = ResponseInput.Text("What is AI?"),
                instructions = "Be concise"
            )
            val response = runBlocking { client.responses.create(request) }
            
            response.id shouldBe "resp_123"
            response.objectType shouldBe "response"
            response.model shouldBe "openai/gpt-4"
            response.choices.size shouldBe 1
            response.choices[0].message.content shouldBe "Hello! I'm an AI assistant."
        }
    }

    test("create() with minimal request") {
        val engine = mockEngineWithResponse(
            responseBody = """
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
                                "content": "Response text"
                            },
                            "finish_reason": "stop"
                        }
                    ]
                }
            """.trimIndent(),
            requestValidator = { req ->
                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["model"]?.jsonPrimitive?.content shouldBe "openai/gpt-4"
                bodyJson["input"]?.jsonPrimitive?.content shouldBe "Hello"
                bodyJson.containsKey("instructions") shouldBe false
                bodyJson.containsKey("tools") shouldBe false
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = CreateResponseRequest(
                model = "openai/gpt-4",
                input = ResponseInput.Text("Hello")
            )
            val response = runBlocking { client.responses.create(request) }
            
            response.id shouldBe "resp_456"
        }
    }
})

private fun HttpRequestData.bodyAsText(): String {
    val content = body
    return when (content) {
        is OutgoingContent.ByteArrayContent -> content.bytes().decodeToString()
        is OutgoingContent.NoContent -> ""
        else -> error("unsupported content type for test: ${content::class.simpleName}")
    }
}
