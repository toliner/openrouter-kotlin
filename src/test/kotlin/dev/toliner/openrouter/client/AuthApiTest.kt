package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.auth.AuthCodeRequest
import dev.toliner.openrouter.l1.auth.AuthKeyRequest
import dev.toliner.openrouter.l1.auth.CodeChallengeMethod
import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.testutil.mockEngineWithResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.client.request.HttpRequestData
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthApiTest : FunSpec({
    test("createAuthCode() sends POST /auth/keys/code with AuthCodeRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "data": {
                        "authorization_url": "https://openrouter.ai/auth?code=abc123"
                    }
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/auth/keys/code"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["callback_url"]?.jsonPrimitive?.content shouldBe "https://myapp.com/auth/callback"
                bodyJson["code_challenge"]?.jsonPrimitive?.content shouldBe "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
                bodyJson["code_challenge_method"]?.jsonPrimitive?.content shouldBe "S256"
                bodyJson["limit"]?.jsonPrimitive?.content shouldBe "100.0"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = AuthCodeRequest(
                callbackUrl = "https://myapp.com/auth/callback",
                codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
                codeChallengeMethod = CodeChallengeMethod.S256,
                limit = 100.0
            )
            val response = runBlocking { client.auth.createAuthCode(request) }
            response.data.authorizationUrl shouldBe "https://openrouter.ai/auth?code=abc123"
        }
    }

    test("createAuthCode() sends POST with minimal AuthCodeRequest") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "data": {
                        "authorization_url": "https://openrouter.ai/auth?code=xyz789"
                    }
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/auth/keys/code"

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["callback_url"]?.jsonPrimitive?.content shouldBe "https://myapp.com/callback"
                bodyJson.containsKey("code_challenge") shouldBe false
                bodyJson.containsKey("code_challenge_method") shouldBe false
                bodyJson.containsKey("limit") shouldBe false
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = AuthCodeRequest(
                callbackUrl = "https://myapp.com/callback"
            )
            val response = runBlocking { client.auth.createAuthCode(request) }
            response.data.authorizationUrl shouldBe "https://openrouter.ai/auth?code=xyz789"
        }
    }

    test("exchangeCode() sends POST /auth/keys with AuthKeyRequest body") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "key": "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",
                    "user_id": "user_2yOPcMpKoQhcd4bVgSMlELRaIah"
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/auth/keys"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
                req.headers[HttpHeaders.ContentType]?.shouldStartWith(ContentType.Application.Json.toString())

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["code"]?.jsonPrimitive?.content shouldBe "auth_code_abc123def456"
                bodyJson["code_verifier"]?.jsonPrimitive?.content shouldBe "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
                bodyJson["code_challenge_method"]?.jsonPrimitive?.content shouldBe "S256"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = AuthKeyRequest(
                code = "auth_code_abc123def456",
                codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk",
                codeChallengeMethod = CodeChallengeMethod.S256
            )
            val response = runBlocking { client.auth.exchangeCode(request) }
            response.key shouldBe "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"
            response.userId shouldBe "user_2yOPcMpKoQhcd4bVgSMlELRaIah"
        }
    }

    test("exchangeCode() sends POST with minimal AuthKeyRequest") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "key": "sk-or-v1-abc123"
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Post
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/auth/keys"

                val bodyJson = OpenRouterJson.parseToJsonElement(req.bodyAsText()).jsonObject
                bodyJson["code"]?.jsonPrimitive?.content shouldBe "auth_code_xyz"
                bodyJson.containsKey("code_verifier") shouldBe false
                bodyJson.containsKey("code_challenge_method") shouldBe false
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val request = AuthKeyRequest(
                code = "auth_code_xyz"
            )
            val response = runBlocking { client.auth.exchangeCode(request) }
            response.key shouldBe "sk-or-v1-abc123"
            response.userId shouldBe null
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
