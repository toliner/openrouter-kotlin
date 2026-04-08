package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.guardrails.AddAssignmentRequest
import dev.toliner.openrouter.l1.guardrails.CreateGuardrailRequest
import dev.toliner.openrouter.l1.guardrails.Guardrail
import dev.toliner.openrouter.l1.guardrails.GuardrailAssignment
import dev.toliner.openrouter.l1.guardrails.UpdateGuardrailRequest
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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GuardrailsApiTest : FunSpec({
    test("list() sends GET /guardrails and returns Guardrail list") {
        val engine = mockEngineWithResponse(
            responseBody = """
                [
                    {
                        "id": "guardrail_1",
                        "name": "content-policy",
                        "description": "Content moderation",
                        "limit_usd": 100.0,
                        "reset_interval": "monthly",
                        "created_at": "2024-03-22T00:00:00Z",
                        "updated_at": "2024-03-23T00:00:00Z"
                    },
                    {
                        "id": "guardrail_2",
                        "name": "basic-policy",
                        "created_at": "2024-03-22T00:00:00Z"
                    }
                ]
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Get
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val guardrails = runBlocking { client.guardrails.list() }
            guardrails.size shouldBe 2
            guardrails[0].id shouldBe "guardrail_1"
            guardrails[0].name shouldBe "content-policy"
            guardrails[0].description shouldBe "Content moderation"
            guardrails[0].limitUsd shouldBe 100.0
            guardrails[0].resetInterval shouldBe "monthly"
            guardrails[1].id shouldBe "guardrail_2"
            guardrails[1].name shouldBe "basic-policy"
        }
    }

    test("create() sends POST /guardrails with CreateGuardrailRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "id": "guardrail_new123",
                    "name": "my-policy",
                    "description": "Custom policy",
                    "limit_usd": 50.0,
                    "enforce_zdr": true,
                    "created_at": "2024-03-22T00:00:00Z"
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["name"]?.jsonPrimitive?.content shouldBe "my-policy"
                bodyJson["description"]?.jsonPrimitive?.content shouldBe "Custom policy"
                bodyJson["limit_usd"]?.jsonPrimitive?.content shouldBe "50.0"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = CreateGuardrailRequest(
                name = "my-policy",
                description = "Custom policy",
                limitUsd = 50.0
            )
            val guardrail = runBlocking { client.guardrails.create(request) }
            guardrail.id shouldBe "guardrail_new123"
            guardrail.name shouldBe "my-policy"
            guardrail.description shouldBe "Custom policy"
        }
    }

    test("get() sends GET /guardrails/{id} and returns specific Guardrail") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "id": "guardrail_abc123",
                    "name": "specific-policy",
                    "description": "Specific guardrail",
                    "allowed_providers": ["openai", "anthropic"],
                    "created_at": "2024-03-22T00:00:00Z",
                    "updated_at": "2024-03-23T00:00:00Z"
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Get
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails/guardrail_abc123"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val guardrail = runBlocking { client.guardrails.get("guardrail_abc123") }
            guardrail.id shouldBe "guardrail_abc123"
            guardrail.name shouldBe "specific-policy"
            guardrail.description shouldBe "Specific guardrail"
            guardrail.allowedProviders shouldBe listOf("openai", "anthropic")
        }
    }

    test("update() sends PATCH /guardrails/{id} with UpdateGuardrailRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "id": "guardrail_update456",
                    "name": "updated-policy",
                    "description": "Updated description",
                    "limit_usd": 200.0,
                    "created_at": "2024-03-22T00:00:00Z",
                    "updated_at": "2024-03-23T00:00:00Z"
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Patch
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails/guardrail_update456"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["name"]?.jsonPrimitive?.content shouldBe "updated-policy"
                bodyJson["description"]?.jsonPrimitive?.content shouldBe "Updated description"
                bodyJson["limit_usd"]?.jsonPrimitive?.content shouldBe "200.0"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = UpdateGuardrailRequest(
                name = "updated-policy",
                description = "Updated description",
                limitUsd = 200.0
            )
            val guardrail = runBlocking { client.guardrails.update("guardrail_update456", request) }
            guardrail.id shouldBe "guardrail_update456"
            guardrail.name shouldBe "updated-policy"
            guardrail.description shouldBe "Updated description"
        }
    }

    test("update() sends PATCH with partial UpdateGuardrailRequest (only changed fields)") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "id": "guardrail_partial789",
                    "name": "partial-policy",
                    "description": "New description",
                    "created_at": "2024-03-22T00:00:00Z"
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Patch
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails/guardrail_partial789"

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["description"]?.jsonPrimitive?.content shouldBe "New description"
                bodyJson.containsKey("name") shouldBe false
                bodyJson.containsKey("limit_usd") shouldBe false
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = UpdateGuardrailRequest(
                description = "New description"
            )
            val guardrail = runBlocking { client.guardrails.update("guardrail_partial789", request) }
            guardrail.description shouldBe "New description"
        }
    }

    test("delete() sends DELETE /guardrails/{id} and handles 204 No Content") {
        val engine = MockEngine { req ->
            req.method shouldBe HttpMethod.Delete
            req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails/guardrail_delete999"
            req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"

            respond(
                content = "",
                status = HttpStatusCode.NoContent,
                headers = headersOf()
            )
        }

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            // Should not throw exception for 204 No Content
            runBlocking { client.guardrails.delete("guardrail_delete999") }
        }
    }

    test("listAssignments() sends GET /guardrails/{id}/assignments and returns GuardrailAssignment list") {
        val engine = mockEngineWithResponse(
            responseBody = """
                [
                    {
                        "id": "assignment_1",
                        "key_hash": "abc123hash",
                        "guardrail_id": "guardrail_abc123",
                        "key_name": "Production Key",
                        "key_label": "prod-key",
                        "assigned_by": "user_001",
                        "created_at": "2024-03-22T00:00:00Z"
                    },
                    {
                        "id": "assignment_2",
                        "key_hash": "def456hash",
                        "guardrail_id": "guardrail_abc123",
                        "key_name": "Test Key",
                        "key_label": "test-key",
                        "assigned_by": null,
                        "created_at": "2024-03-23T00:00:00Z"
                    }
                ]
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Get
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails/guardrail_abc123/assignments"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val assignments = runBlocking { client.guardrails.listAssignments("guardrail_abc123") }
            assignments.size shouldBe 2
            assignments[0].id shouldBe "assignment_1"
            assignments[0].guardrailId shouldBe "guardrail_abc123"
            assignments[0].keyHash shouldBe "abc123hash"
            assignments[0].keyName shouldBe "Production Key"
            assignments[1].id shouldBe "assignment_2"
            assignments[1].keyHash shouldBe "def456hash"
            assignments[1].assignedBy shouldBe null
        }
    }

    test("addAssignment() sends POST /guardrails/{id}/assignments with AddAssignmentRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "id": "assignment_new123",
                    "key_hash": "key456hash",
                    "guardrail_id": "guardrail_xyz789",
                    "key_name": "New Key",
                    "key_label": "new-key",
                    "assigned_by": "user_002",
                    "created_at": "2024-03-22T00:00:00Z"
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails/guardrail_xyz789/assignments"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["key_hashes"]?.jsonArray?.size shouldBe 1
                bodyJson["key_hashes"]?.jsonArray?.get(0)?.jsonPrimitive?.content shouldBe "key456hash"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = AddAssignmentRequest(
                keyHashes = listOf("key456hash")
            )
            val assignment = runBlocking { client.guardrails.addAssignment("guardrail_xyz789", request) }
            assignment.id shouldBe "assignment_new123"
            assignment.guardrailId shouldBe "guardrail_xyz789"
            assignment.keyHash shouldBe "key456hash"
        }
    }

    test("removeAssignment() sends DELETE /guardrails/{id}/assignments/{assignmentId} and handles 204 No Content") {
        val engine = MockEngine { req ->
            req.method shouldBe HttpMethod.Delete
            req.url.toString() shouldBe "https://openrouter.ai/api/v1/guardrails/guardrail_abc123/assignments/assignment_999"
            req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"

            respond(
                content = "",
                status = HttpStatusCode.NoContent,
                headers = headersOf()
            )
        }

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            // Should not throw exception for 204 No Content
            runBlocking { client.guardrails.removeAssignment("guardrail_abc123", "assignment_999") }
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
