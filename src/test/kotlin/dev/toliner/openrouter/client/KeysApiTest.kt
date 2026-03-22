package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.keys.ApiKey
import dev.toliner.openrouter.l1.keys.CreateKeyRequest
import dev.toliner.openrouter.l1.keys.UpdateKeyRequest
import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.testutil.mockEngineWithResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class KeysApiTest : FunSpec({
    test("list() sends GET /keys and returns ApiKey list") {
        val engine = mockEngineWithResponse(
            responseBody = """
                [
                    {
                        "hash": "sk_live_key1",
                        "name": "production-key",
                        "label": "Production",
                        "usage": 100.00,
                        "limit": 1000.00,
                        "is_free_tier": false,
                        "disabled": false,
                        "created_at": 1679500800
                    },
                    {
                        "hash": "sk_live_key2",
                        "name": "test-key",
                        "usage": 50.00,
                        "is_free_tier": true,
                        "disabled": true,
                        "created_at": 1679587200
                    }
                ]
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Get
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/keys"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val keys = runBlocking { client.keys.list() }
            keys.size shouldBe 2
            keys[0].hash shouldBe "sk_live_key1"
            keys[0].name shouldBe "production-key"
            keys[0].disabled shouldBe false
            keys[1].hash shouldBe "sk_live_key2"
            keys[1].disabled shouldBe true
        }
    }

    test("create() sends POST /keys with CreateKeyRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "hash": "sk_live_new123",
                    "name": "my-key",
                    "label": "Production",
                    "usage": 0.00,
                    "limit": 100.00,
                    "is_free_tier": false,
                    "disabled": false,
                    "created_at": 1679500800
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/keys"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["name"]?.jsonPrimitive?.content shouldBe "my-key"
                bodyJson["label"]?.jsonPrimitive?.content shouldBe "Production"
                bodyJson["limit"]?.jsonPrimitive?.content shouldBe "100.0"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = CreateKeyRequest(
                name = "my-key",
                label = "Production",
                limit = 100.0,
                disabled = null
            )
            val key = runBlocking { client.keys.create(request) }
            key.hash shouldBe "sk_live_new123"
            key.name shouldBe "my-key"
            key.label shouldBe "Production"
        }
    }

    test("get() sends GET /keys/{hash} and returns specific ApiKey") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "hash": "sk_live_abc123",
                    "name": "specific-key",
                    "label": "Development",
                    "usage": 75.50,
                    "limit": 500.00,
                    "is_free_tier": false,
                    "disabled": false,
                    "created_at": 1679500800
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Get
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/keys/sk_live_abc123"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val key = runBlocking { client.keys.get("sk_live_abc123") }
            key.hash shouldBe "sk_live_abc123"
            key.name shouldBe "specific-key"
            key.label shouldBe "Development"
            key.usage shouldBe 75.50
        }
    }

    test("update() sends PATCH /keys/{hash} with UpdateKeyRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "hash": "sk_live_update456",
                    "name": "original-name",
                    "label": "Updated Label",
                    "usage": 100.00,
                    "limit": 200.00,
                    "is_free_tier": false,
                    "disabled": true,
                    "created_at": 1679500800,
                    "updated_at": 1679587200
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Patch
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/keys/sk_live_update456"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["label"]?.jsonPrimitive?.content shouldBe "Updated Label"
                bodyJson["limit"]?.jsonPrimitive?.content shouldBe "200.0"
                bodyJson["disabled"]?.jsonPrimitive?.content shouldBe "true"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = UpdateKeyRequest(
                label = "Updated Label",
                limit = 200.0,
                disabled = true
            )
            val key = runBlocking { client.keys.update("sk_live_update456", request) }
            key.hash shouldBe "sk_live_update456"
            key.label shouldBe "Updated Label"
            key.limit shouldBe 200.00
            key.disabled shouldBe true
        }
    }

    test("update() sends PATCH with partial UpdateKeyRequest (only changed fields)") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "hash": "sk_live_partial789",
                    "name": "partial-update-key",
                    "label": "New Label",
                    "usage": 50.00,
                    "limit": 500.00,
                    "is_free_tier": false,
                    "disabled": false,
                    "created_at": 1679500800
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Patch
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/keys/sk_live_partial789"

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["label"]?.jsonPrimitive?.content shouldBe "New Label"
                bodyJson.containsKey("limit") shouldBe false  // Not included in partial update
                bodyJson.containsKey("disabled") shouldBe false  // Not included in partial update
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = UpdateKeyRequest(
                label = "New Label",
                limit = null,
                disabled = null
            )
            val key = runBlocking { client.keys.update("sk_live_partial789", request) }
            key.label shouldBe "New Label"
        }
    }

    test("delete() sends DELETE /keys/{hash} and handles 204 No Content") {
        val engine = MockEngine { req ->
            req.method shouldBe HttpMethod.Delete
            req.url.toString() shouldBe "https://openrouter.ai/api/v1/keys/sk_live_delete999"
            req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"

            respond(
                content = "",
                status = HttpStatusCode.NoContent,
                headers = headersOf()
            )
        }

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            // Should not throw exception for 204 No Content
            runBlocking { client.keys.delete("sk_live_delete999") }
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
