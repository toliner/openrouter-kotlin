package dev.toliner.openrouter.l1.auth

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString

class OAuthTypesTest : FunSpec({
    test("AuthCodeRequest serialization") {
        val request = AuthCodeRequest(
            callbackUrl = "https://myapp.com/auth/callback",
            codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            codeChallengeMethod = CodeChallengeMethod.S256,
            limit = 100.0
        )

        val json = OpenRouterJson.encodeToString(request)

        json shouldBe """{"callback_url":"https://myapp.com/auth/callback","code_challenge":"E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM","code_challenge_method":"S256","limit":100.0}"""
    }

    test("AuthCodeRequest serialization with minimal fields") {
        val request = AuthCodeRequest(
            callbackUrl = "https://myapp.com/callback"
        )

        val json = OpenRouterJson.encodeToString(request)

        json shouldBe """{"callback_url":"https://myapp.com/callback"}"""
    }

    test("AuthCodeResponse deserialization") {
        val json = """
            {
                "data": {
                    "authorization_url": "https://openrouter.ai/auth?code=abc123"
                }
            }
        """.trimIndent()

        val response = OpenRouterJson.decodeFromString<AuthCodeResponse>(json)

        response.data.authorizationUrl shouldBe "https://openrouter.ai/auth?code=abc123"
    }

    test("AuthKeyRequest serialization") {
        val request = AuthKeyRequest(
            code = "auth_code_abc123def456",
            codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk",
            codeChallengeMethod = CodeChallengeMethod.S256
        )

        val json = OpenRouterJson.encodeToString(request)

        json shouldBe """{"code":"auth_code_abc123def456","code_verifier":"dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk","code_challenge_method":"S256"}"""
    }

    test("AuthKeyRequest serialization with minimal fields") {
        val request = AuthKeyRequest(
            code = "auth_code_xyz"
        )

        val json = OpenRouterJson.encodeToString(request)

        json shouldBe """{"code":"auth_code_xyz"}"""
    }

    test("AuthKeyResponse deserialization") {
        val json = """
            {
                "key": "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",
                "user_id": "user_2yOPcMpKoQhcd4bVgSMlELRaIah"
            }
        """.trimIndent()

        val response = OpenRouterJson.decodeFromString<AuthKeyResponse>(json)

        response.key shouldBe "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"
        response.userId shouldBe "user_2yOPcMpKoQhcd4bVgSMlELRaIah"
    }

    test("AuthKeyResponse deserialization without user_id") {
        val json = """
            {
                "key": "sk-or-v1-abc123"
            }
        """.trimIndent()

        val response = OpenRouterJson.decodeFromString<AuthKeyResponse>(json)

        response.key shouldBe "sk-or-v1-abc123"
        response.userId shouldBe null
    }

    test("CodeChallengeMethod enum serialization") {
        OpenRouterJson.encodeToString(CodeChallengeMethod.S256) shouldBe "\"S256\""
        OpenRouterJson.encodeToString(CodeChallengeMethod.PLAIN) shouldBe "\"plain\""
    }
})
