package dev.toliner.openrouter.l1.keys

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KeyTypesTest : FunSpec({
    test("ApiKey should deserialize with all fields") {
        val json = """
            {
                "hash": "sk_live_abc123def456",
                "name": "production-key",
                "label": "Production Server",
                "usage": 150.50,
                "limit": 1000.00,
                "limit_remaining": 849.50,
                "is_free_tier": false,
                "rate_limit": {
                    "requests": 100,
                    "interval": "10s"
                },
                "disabled": false,
                "created_at": 1679500800,
                "updated_at": 1679587200
            }
        """.trimIndent()

        val apiKey = OpenRouterJson.decodeFromString<ApiKey>(json)

        apiKey.hash shouldBe "sk_live_abc123def456"
        apiKey.name shouldBe "production-key"
        apiKey.label shouldBe "Production Server"
        apiKey.usage shouldBe 150.50
        apiKey.limit shouldBe 1000.00
        apiKey.limitRemaining shouldBe 849.50
        apiKey.isFreeTier shouldBe false
        apiKey.rateLimit?.requests shouldBe 100
        apiKey.rateLimit?.interval shouldBe "10s"
        apiKey.disabled shouldBe false
        apiKey.createdAt shouldBe 1679500800
        apiKey.updatedAt shouldBe 1679587200
    }

    test("ApiKey should deserialize with optional fields absent") {
        val json = """
            {
                "hash": "sk_live_xyz789",
                "name": "test-key",
                "usage": 50.00,
                "is_free_tier": true,
                "disabled": false,
                "created_at": 1679500800
            }
        """.trimIndent()

        val apiKey = OpenRouterJson.decodeFromString<ApiKey>(json)

        apiKey.hash shouldBe "sk_live_xyz789"
        apiKey.name shouldBe "test-key"
        apiKey.label shouldBe null
        apiKey.limit shouldBe null
        apiKey.limitRemaining shouldBe null
        apiKey.rateLimit shouldBe null
        apiKey.updatedAt shouldBe null
    }

    test("ApiKey list response should deserialize") {
        val json = """
            [
                {
                    "hash": "sk_live_key1",
                    "name": "key-1",
                    "usage": 100.00,
                    "is_free_tier": false,
                    "disabled": false,
                    "created_at": 1679500800
                },
                {
                    "hash": "sk_live_key2",
                    "name": "key-2",
                    "usage": 200.00,
                    "is_free_tier": false,
                    "disabled": true,
                    "created_at": 1679587200
                }
            ]
        """.trimIndent()

        val apiKeys = OpenRouterJson.decodeFromString<List<ApiKey>>(json)

        apiKeys.size shouldBe 2
        apiKeys[0].hash shouldBe "sk_live_key1"
        apiKeys[0].disabled shouldBe false
        apiKeys[1].hash shouldBe "sk_live_key2"
        apiKeys[1].disabled shouldBe true
    }

    test("CreateKeyRequest should serialize with all fields") {
        val request = CreateKeyRequest(
            name = "my-key",
            label = "Production",
            limit = 100.0,
            disabled = false
        )

        val json = OpenRouterJson.encodeToString(CreateKeyRequest.serializer(), request)

        json shouldBe """{"name":"my-key","label":"Production","limit":100.0,"disabled":false}"""
    }

    test("CreateKeyRequest should serialize with minimal fields") {
        val request = CreateKeyRequest(
            name = "minimal-key",
            label = null,
            limit = null,
            disabled = null
        )

        val json = OpenRouterJson.encodeToString(CreateKeyRequest.serializer(), request)

        json shouldBe """{"name":"minimal-key"}"""
    }

    test("UpdateKeyRequest should serialize with all fields") {
        val request = UpdateKeyRequest(
            label = "Updated Label",
            limit = 200.0,
            disabled = true
        )

        val json = OpenRouterJson.encodeToString(UpdateKeyRequest.serializer(), request)

        json shouldBe """{"label":"Updated Label","limit":200.0,"disabled":true}"""
    }

    test("UpdateKeyRequest should serialize with partial fields") {
        val request = UpdateKeyRequest(
            label = "New Label",
            limit = null,
            disabled = null
        )

        val json = OpenRouterJson.encodeToString(UpdateKeyRequest.serializer(), request)

        json shouldBe """{"label":"New Label"}"""
    }

    test("UpdateKeyRequest should serialize with only limit") {
        val request = UpdateKeyRequest(
            label = null,
            limit = 500.0,
            disabled = null
        )

        val json = OpenRouterJson.encodeToString(UpdateKeyRequest.serializer(), request)

        json shouldBe """{"limit":500.0}"""
    }
})
