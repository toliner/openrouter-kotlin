package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.client.OpenRouterConfig
import dev.toliner.openrouter.l1.chat.Message
import dev.toliner.openrouter.l1.chat.ResponseFormat
import dev.toliner.openrouter.serialization.Content
import dev.toliner.openrouter.serialization.StringOrArray
import dev.toliner.openrouter.testutil.mockEngineWithResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking

class ChatDslTest : FunSpec({
    test("chatRequest minimal - model and single user message") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
        }
        
        request.model shouldBe "openai/gpt-4o"
        request.messages shouldHaveSize 1
        request.messages[0].shouldBeInstanceOf<Message.User>()
        val userMsg = request.messages[0] as Message.User
        userMsg.content.shouldBeInstanceOf<Content.Text>()
        (userMsg.content as Content.Text).value shouldBe "Hello!"
        request.temperature.shouldBeNull()
        request.maxTokens.shouldBeNull()
        request.stream shouldBe false
    }
    
    test("chatRequest full - all optional fields and multiple messages") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            temperature = 0.7
            maxTokens = 1000
            topP = 0.9
            topK = 40
            frequencyPenalty = 0.5
            presencePenalty = 0.2
            repetitionPenalty = 1.1
            seed = 42
            stop = StringOrArray.Single("STOP")
            responseFormat = ResponseFormat(type = "json_object")
            
            systemMessage("You are a helpful assistant.")
            userMessage("What is Kotlin?")
            assistantMessage("Kotlin is a modern programming language.")
            userMessage("Tell me more.")
        }
        
        request.model shouldBe "openai/gpt-4o"
        request.temperature shouldBe 0.7
        request.maxTokens shouldBe 1000
        request.topP shouldBe 0.9
        request.topK shouldBe 40
        request.frequencyPenalty shouldBe 0.5
        request.presencePenalty shouldBe 0.2
        request.repetitionPenalty shouldBe 1.1
        request.seed shouldBe 42
        request.stop.shouldNotBeNull()
        request.responseFormat?.type shouldBe "json_object"
        
        request.messages shouldHaveSize 4
        request.messages[0].shouldBeInstanceOf<Message.System>()
        (request.messages[0] as Message.System).content shouldBe "You are a helpful assistant."
        
        request.messages[1].shouldBeInstanceOf<Message.User>()
        val user1 = request.messages[1] as Message.User
        (user1.content as Content.Text).value shouldBe "What is Kotlin?"
        
        request.messages[2].shouldBeInstanceOf<Message.Assistant>()
        (request.messages[2] as Message.Assistant).content shouldBe "Kotlin is a modern programming language."
        
        request.messages[3].shouldBeInstanceOf<Message.User>()
        val user2 = request.messages[3] as Message.User
        (user2.content as Content.Text).value shouldBe "Tell me more."
    }
    
    test("chatRequest systemMessage builder") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            systemMessage("System prompt")
            userMessage("User query")
        }
        
        request.messages shouldHaveSize 2
        request.messages[0].shouldBeInstanceOf<Message.System>()
        (request.messages[0] as Message.System).content shouldBe "System prompt"
    }
    
    test("chatRequest assistantMessage builder") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello")
            assistantMessage("Hi there!")
        }
        
        request.messages shouldHaveSize 2
        request.messages[1].shouldBeInstanceOf<Message.Assistant>()
        val assistant = request.messages[1] as Message.Assistant
        assistant.content shouldBe "Hi there!"
        assistant.toolCalls.shouldBeNull()
    }
    
    test("chatRequest toolMessage builder") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("What's the weather?")
            assistantMessage("Calling weather tool...")
            toolMessage(toolCallId = "call_123", content = "It's sunny, 72°F")
        }
        
        request.messages shouldHaveSize 3
        request.messages[2].shouldBeInstanceOf<Message.Tool>()
        val tool = request.messages[2] as Message.Tool
        tool.toolCallId shouldBe "call_123"
        tool.content shouldBe "It's sunny, 72°F"
    }
    
    test("chatRequest userMessage with Content.Parts via lambda") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage { Content.Text("Complex content") }
        }
        
        request.messages shouldHaveSize 1
        request.messages[0].shouldBeInstanceOf<Message.User>()
        val userMsg = request.messages[0] as Message.User
        userMsg.content.shouldBeInstanceOf<Content.Text>()
        (userMsg.content as Content.Text).value shouldBe "Complex content"
    }
    
    test("client.chat { } integration - end-to-end DSL request") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                  "id":"gen-456",
                  "model":"openai/gpt-4o",
                  "object":"chat.completion",
                  "created":1234567890,
                  "choices":[
                    {
                      "index":0,
                      "message":{"role":"assistant","content":"Hi! How can I help?"},
                      "finish_reason":"stop"
                    }
                  ],
                  "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/chat/completions"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }
        )
        
        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val response = runBlocking {
                client.chat {
                    model = "openai/gpt-4o"
                    temperature = 0.8
                    userMessage("Hello")
                }
            }
            
            response.id shouldBe "gen-456"
            response.choices shouldHaveSize 1
            val assistantMsg = response.choices[0].message as Message.Assistant
            assistantMsg.content shouldBe "Hi! How can I help?"
        }
    }
    
    test("chatRequest validation - missing model throws") {
        val exception = runCatching {
            chatRequest {
                userMessage("Hello")
            }
        }.exceptionOrNull()
        
        exception.shouldNotBeNull()
        exception.shouldBeInstanceOf<IllegalArgumentException>()
        exception.message shouldBe "model is required"
    }
    
    test("chatRequest validation - missing messages throws") {
        val exception = runCatching {
            chatRequest {
                model = "openai/gpt-4o"
            }
        }.exceptionOrNull()
        
        exception.shouldNotBeNull()
        exception.shouldBeInstanceOf<IllegalArgumentException>()
        exception.message shouldBe "at least one message is required"
    }
})
