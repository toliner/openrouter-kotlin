package dev.toliner.openrouter.client

import dev.toliner.openrouter.error.OpenRouterException
import dev.toliner.openrouter.l1.chat.ChatCompletionRequest
import dev.toliner.openrouter.l1.chat.Message
import dev.toliner.openrouter.l1.embeddings.EmbeddingRequest
import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.serialization.StringOrArray
import dev.toliner.openrouter.serialization.Content
import dev.toliner.openrouter.testutil.mockEngineWithResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class OpenRouterClientTest : FunSpec({
    test("chat complete sends POST with expected URL and headers") {
        val request = ChatCompletionRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(Message.User(content = Content.Text("hello")))
        )
        val engine = mockEngineWithResponse(
            responseBody =
                """
                {
                  "id":"gen-123",
                  "model":"openai/gpt-4o-mini",
                  "object":"chat.completion",
                  "created":1,
                  "choices":[{"index":0,"message":{"role":"assistant","content":"Hello!"},"finish_reason":"stop"}],
                  "usage":{"prompt_tokens":1,"completion_tokens":1,"total_tokens":2}
                }
                """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/chat/completions"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers["HTTP-Referer"] shouldBe "https://example.com"
                req.headers["X-Title"] shouldBe "My App"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())
            }
        )

        OpenRouterClient(
            engine,
            OpenRouterConfig(apiKey = "test-key", httpReferer = "https://example.com", xTitle = "My App")
        ).use { client ->
            val response = runBlocking { client.chat.complete(request) }
            response.id shouldBe "gen-123"
            (response.choices[0].message as Message.Assistant).content shouldBe "Hello!"
        }
    }

    test("http status errors are mapped with errorFromStatus") {
        val request = ChatCompletionRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(Message.User(content = Content.Text("hello")))
        )

        val unauthorizedEngine = mockEngineWithResponse(
            responseBody = """{"error":{"message":"unauthorized","code":401}}""",
            statusCode = HttpStatusCode.Unauthorized
        )
        OpenRouterClient(unauthorizedEngine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val error = shouldThrow<OpenRouterException.Unauthorized> {
                runBlocking { client.chat.complete(request) }
            }
            error.body.message shouldBe "unauthorized"
            error.body.code shouldBe 401
        }

        val rateLimitEngine = mockEngineWithResponse(
            responseBody = """{"error":{"message":"rate limit","code":429}}""",
            statusCode = HttpStatusCode.TooManyRequests,
            headers = headersOf(
                HttpHeaders.RetryAfter to listOf("30"),
                HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())
            )
        )
        OpenRouterClient(rateLimitEngine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val error = shouldThrow<OpenRouterException.TooManyRequests> {
                runBlocking { client.chat.complete(request) }
            }
            error.body.message shouldBe "rate limit"
            error.retryAfter shouldBe 30
        }
    }

    test("in-band errors in completion response are detected") {
        val request = ChatCompletionRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(Message.User(content = Content.Text("hello")))
        )
        val engine = mockEngineWithResponse(
            responseBody =
                """
                {
                  "id":"gen-123",
                  "model":"openai/gpt-4o-mini",
                  "object":"chat.completion",
                  "created":1,
                  "choices":[
                    {
                      "index":0,
                      "message":{"role":"assistant","content":""},
                      "finish_reason":"error",
                      "error":{"message":"in-band failure","code":400}
                    }
                  ]
                }
                """.trimIndent()
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val error = shouldThrow<OpenRouterException.InBandError> {
                runBlocking { client.chat.complete(request) }
            }
            error.body.message shouldBe "in-band failure"
            error.body.code shouldBe 400
        }
    }

    test("models endpoints return expected payloads") {
        val requestedPaths = mutableListOf<String>()
        val engine = MockEngine { req ->
            requestedPaths += req.url.encodedPath
            req.method shouldBe HttpMethod.Get
            req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            req.headers["HTTP-Referer"] shouldBe "https://example.com"
            req.headers["X-Title"] shouldBe "My App"

            when (req.url.encodedPath) {
                "/api/v1/models" -> respond(
                    content =
                        """
                        {"data":[{"id":"openai/gpt-4o-mini","name":"GPT-4o mini","pricing":{"prompt":"0.1","completion":"0.2","request":"0","image":"0"},"context_length":128000,"architecture":{"modality":"text->text","tokenizer":"cl100k_base"},"top_provider":{"context_length":128000,"max_completion_tokens":4096,"is_moderated":false}}]}
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )

                "/api/v1/models/count" -> respond(
                    content = """{"count":123}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )

                "/api/v1/models/user" -> respond(
                    content =
                        """
                        {"data":[{"id":"anthropic/claude-3-haiku","name":"Claude Haiku","pricing":{"prompt":"0.1","completion":"0.2","request":"0","image":"0"},"context_length":200000,"architecture":{"modality":"text->text","tokenizer":"cl100k_base"},"top_provider":{"context_length":200000,"max_completion_tokens":4096,"is_moderated":true}}]}
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )

                "/api/v1/models/endpoints" -> respond(
                    content = """[{"url":"https://openrouter.ai/api/v1/chat/completions","headers":{"x-custom":"1"}}]""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )

                "/api/v1/models/endpoints/zdr" -> respond(
                    content = """[{"id":"openai/gpt-4o-mini","name":"GPT-4o mini","zdr_affected":true,"multiplier":1.25}]""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )

                "/api/v1/models/embedding" -> respond(
                    content = """[{"id":"openai/text-embedding-3-small","name":"Embedding 3 Small","pricing":{"prompt":"0.1","completion":"0","request":"0","image":"0"},"context_length":8192}]""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )

                else -> error("unexpected path: ${req.url.encodedPath}")
            }
        }

        OpenRouterClient(
            engine,
            OpenRouterConfig(apiKey = "test-key", httpReferer = "https://example.com", xTitle = "My App")
        ).use { client ->
            runBlocking {
                client.models.list().data[0].id shouldBe "openai/gpt-4o-mini"
                client.models.count().count shouldBe 123
                client.models.userModels().data[0].id shouldBe "anthropic/claude-3-haiku"
                client.models.endpoints()[0].url shouldContain "/chat/completions"
                client.models.zdrEndpoints()[0].zdrAffected shouldBe true
                client.models.embeddingModels()[0].id shouldBe "openai/text-embedding-3-small"
            }
        }

        requestedPaths shouldBe listOf(
            "/api/v1/models",
            "/api/v1/models/count",
            "/api/v1/models/user",
            "/api/v1/models/endpoints",
            "/api/v1/models/endpoints/zdr",
            "/api/v1/models/embedding"
        )
    }

    test("embeddings create sends POST and deserializes response") {
        val request = EmbeddingRequest(
            model = "openai/text-embedding-3-small",
            input = StringOrArray.Single("hello")
        )

        val engine = mockEngineWithResponse(
            responseBody =
                """
                {
                  "data":[{"embedding":[0.1,0.2],"index":0,"object":"embedding"}],
                  "model":"openai/text-embedding-3-small",
                  "usage":{"prompt_tokens":2,"completion_tokens":0,"total_tokens":2}
                }
                """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/embeddings"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val response = runBlocking { client.embeddings.create(request) }
            response.model shouldBe "openai/text-embedding-3-small"
            response.data shouldHaveSize 1
            response.data[0].embedding[0] shouldBe 0.1
        }
    }

    test("chat stream returns chunks and forces stream=true") {
        val request = ChatCompletionRequest(
            model = "openai/gpt-4o-mini",
            messages = listOf(Message.User(content = Content.Text("hello")))
        )

        val engine = mockEngineWithResponse(
            responseBody =
                """
                data: {"id":"gen-123","model":"openai/gpt-4o-mini","object":"chat.completion.chunk","created":1,"choices":[{"index":0,"delta":{"content":"Hello"}}]}

                data: [DONE]

                """.trimIndent(),
            headers = headersOf(HttpHeaders.ContentType, "text/event-stream"),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/chat/completions"
                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["stream"]?.jsonPrimitive?.content shouldBe "true"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val chunks = runBlocking { client.chat.stream(request).toList() }
            chunks shouldHaveSize 1
            chunks[0].choices[0].delta.content shouldBe "Hello"
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
