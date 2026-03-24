package dev.toliner.openrouter.l2.stream

import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.client.OpenRouterConfig
import dev.toliner.openrouter.l1.chat.ChatCompletionChunk
import dev.toliner.openrouter.l1.chat.ChunkChoice
import dev.toliner.openrouter.l1.chat.Delta
import dev.toliner.openrouter.l1.chat.Usage
import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class StreamDslTest : FunSpec({
    test("chatStream sets stream=true and returns Flow") {
        val chunk1 = ChatCompletionChunk(
            id = "gen-1",
            model = "openai/gpt-4o",
            objectType = "chat.completion.chunk",
            created = 1234567890,
            choices = listOf(ChunkChoice(index = 0, delta = Delta(content = "Hello"))),
            usage = null
        )
        
        val engine = MockEngine { req ->
            // Validate stream=true in request body
            val bodyJson = OpenRouterJson.parseToJsonElement(req.body.toByteArray().decodeToString()).jsonObject
            val streamValue = bodyJson["stream"]?.jsonPrimitive?.content?.toBoolean()
            streamValue shouldBe true
            
            // Validate model and messages are passed through
            bodyJson["model"]?.jsonPrimitive?.content shouldBe "openai/gpt-4o"
            
            respond(
                content = "data: ${OpenRouterJson.encodeToString(chunk1)}\n\ndata: [DONE]\n\n",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
            )
        }
        
        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val flow = runBlocking {
                client.chatStream {
                    model = "openai/gpt-4o"
                    userMessage("Hi")
                }
            }
            
            flow shouldNotBe null
        }
    }
    
    test("collectContent concatenates all delta.content") {
        val chunks = flowOf(
            ChatCompletionChunk(
                id = "1",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567890,
                choices = listOf(ChunkChoice(delta = Delta(content = "Hel"), index = 0))
            ),
            ChatCompletionChunk(
                id = "2",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567891,
                choices = listOf(ChunkChoice(delta = Delta(content = "lo "), index = 0))
            ),
            ChatCompletionChunk(
                id = "3",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567892,
                choices = listOf(ChunkChoice(delta = Delta(content = "World"), index = 0))
            )
        )
        
        val result = runBlocking { chunks.collectContent() }
        result shouldBe "Hello World"
    }
    
    test("collectContent handles null content gracefully") {
        val chunks = flowOf(
            ChatCompletionChunk(
                id = "1",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567890,
                choices = listOf(ChunkChoice(delta = Delta(content = "Hi"), index = 0))
            ),
            ChatCompletionChunk(
                id = "2",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567891,
                choices = listOf(ChunkChoice(delta = Delta(content = null), index = 0))
            ),
            ChatCompletionChunk(
                id = "3",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567892,
                choices = listOf(ChunkChoice(delta = Delta(content = "!"), index = 0))
            )
        )
        
        val result = runBlocking { chunks.collectContent() }
        result shouldBe "Hi!"
    }
    
    test("collectContent handles empty choices list") {
        val chunks = flowOf(
            ChatCompletionChunk(
                id = "1",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567890,
                choices = listOf(ChunkChoice(delta = Delta(content = "Hi"), index = 0))
            ),
            ChatCompletionChunk(
                id = "2",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567891,
                choices = emptyList()
            ),
            ChatCompletionChunk(
                id = "3",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567892,
                choices = listOf(ChunkChoice(delta = Delta(content = "!"), index = 0))
            )
        )
        
        val result = runBlocking { chunks.collectContent() }
        result shouldBe "Hi!"
    }
    
    test("collectContentAndUsage returns content and final usage") {
        val chunks = flowOf(
            ChatCompletionChunk(
                id = "1",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567890,
                choices = listOf(ChunkChoice(delta = Delta(content = "Hi"), index = 0)),
                usage = null
            ),
            ChatCompletionChunk(
                id = "2",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567891,
                choices = listOf(ChunkChoice(delta = Delta(content = "!"), index = 0)),
                usage = Usage(promptTokens = 10, completionTokens = 5, totalTokens = 15)
            )
        )
        
        val (content, usage) = runBlocking { chunks.collectContentAndUsage() }
        content shouldBe "Hi!"
        usage.totalTokens shouldBe 15
        usage.promptTokens shouldBe 10
        usage.completionTokens shouldBe 5
    }
    
    test("collectContentAndUsage returns default usage when none provided") {
        val chunks = flowOf(
            ChatCompletionChunk(
                id = "1",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567890,
                choices = listOf(ChunkChoice(delta = Delta(content = "Hello"), index = 0)),
                usage = null
            )
        )
        
        val (content, usage) = runBlocking { chunks.collectContentAndUsage() }
        content shouldBe "Hello"
        usage.totalTokens shouldBe 0
        usage.promptTokens shouldBe 0
        usage.completionTokens shouldBe 0
    }
    
    test("collectContentAndUsage uses last usage when multiple provided") {
        val chunks = flowOf(
            ChatCompletionChunk(
                id = "1",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567890,
                choices = listOf(ChunkChoice(delta = Delta(content = "Hi"), index = 0)),
                usage = Usage(promptTokens = 5, completionTokens = 2, totalTokens = 7)
            ),
            ChatCompletionChunk(
                id = "2",
                model = "test",
                objectType = "chat.completion.chunk",
                created = 1234567891,
                choices = listOf(ChunkChoice(delta = Delta(content = "!"), index = 0)),
                usage = Usage(promptTokens = 10, completionTokens = 5, totalTokens = 15)
            )
        )
        
        val (content, usage) = runBlocking { chunks.collectContentAndUsage() }
        content shouldBe "Hi!"
        usage.totalTokens shouldBe 15
    }
})
