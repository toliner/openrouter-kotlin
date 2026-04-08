package dev.toliner.openrouter.l1.account

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AccountTypesTest : FunSpec({
    test("Credits should deserialize with total_credits and total_usage") {
        val json = """
            {
                "total_credits": 10.0,
                "total_usage": 3.5
            }
        """.trimIndent()

        val credits = OpenRouterJson.decodeFromString<Credits>(json)

        credits.totalCredits shouldBe 10.0
        credits.totalUsage shouldBe 3.5
    }

    test("KeyInfo should deserialize with all fields") {
        val json = """
            {
                "label": "Production API Key",
                "usage": 150.75,
                "limit": 1000.0,
                "is_free_tier": false,
                "rate_limit": {
                    "requests": 100,
                    "interval": "10s"
                }
            }
        """.trimIndent()

        val keyInfo = OpenRouterJson.decodeFromString<KeyInfo>(json)

        keyInfo.label shouldBe "Production API Key"
        keyInfo.usage shouldBe 150.75
        keyInfo.limit shouldBe 1000.0
        keyInfo.isFreeTier shouldBe false
        keyInfo.rateLimit.requests shouldBe 100
        keyInfo.rateLimit.interval shouldBe "10s"
    }

    test("KeyInfo should deserialize with null limit") {
        val json = """
            {
                "label": "Test Key",
                "usage": 50.0,
                "limit": null,
                "is_free_tier": true,
                "rate_limit": {
                    "requests": 10,
                    "interval": "1m"
                }
            }
        """.trimIndent()

        val keyInfo = OpenRouterJson.decodeFromString<KeyInfo>(json)

        keyInfo.label shouldBe "Test Key"
        keyInfo.limit shouldBe null
        keyInfo.isFreeTier shouldBe true
    }

    test("Activity should deserialize with data array") {
        val json = """
            {
                "data": [
                    {
                        "date": "2024-03-22",
                        "model": "openai/gpt-4o",
                        "model_permaslug": "openai/gpt-4o-2024-05-13",
                        "endpoint_id": "ep-123",
                        "provider_name": "OpenAI",
                        "usage": 2.5,
                        "byok_usage_inference": 0.0,
                        "requests": 150,
                        "prompt_tokens": 5000,
                        "completion_tokens": 3000,
                        "reasoning_tokens": 0
                    },
                    {
                        "date": "2024-03-21",
                        "model": "anthropic/claude-3-opus",
                        "model_permaslug": "anthropic/claude-3-opus-20240229",
                        "endpoint_id": "ep-456",
                        "provider_name": "Anthropic",
                        "usage": 3.75,
                        "byok_usage_inference": 1.0,
                        "requests": 200,
                        "prompt_tokens": 8000,
                        "completion_tokens": 5000,
                        "reasoning_tokens": 100
                    }
                ]
            }
        """.trimIndent()

        val activity = OpenRouterJson.decodeFromString<Activity>(json)

        activity.data.size shouldBe 2
        activity.data[0].date shouldBe "2024-03-22"
        activity.data[0].model shouldBe "openai/gpt-4o"
        activity.data[0].requests shouldBe 150
        activity.data[0].usage shouldBe 2.5
        activity.data[1].date shouldBe "2024-03-21"
        activity.data[1].model shouldBe "anthropic/claude-3-opus"
        activity.data[1].requests shouldBe 200
        activity.data[1].usage shouldBe 3.75
    }

    test("Activity should deserialize with empty data array") {
        val json = """
            {
                "data": []
            }
        """.trimIndent()

        val activity = OpenRouterJson.decodeFromString<Activity>(json)

        activity.data.size shouldBe 0
    }

    test("RateLimit should deserialize requests and interval") {
        val json = """
            {
                "requests": 50,
                "interval": "30s"
            }
        """.trimIndent()

        val rateLimit = OpenRouterJson.decodeFromString<RateLimit>(json)

        rateLimit.requests shouldBe 50
        rateLimit.interval shouldBe "30s"
    }

    test("ActivityItem should deserialize all fields") {
        val json = """
            {
                "date": "2024-03-20",
                "model": "openai/gpt-4o",
                "model_permaslug": "openai/gpt-4o-2024-05-13",
                "endpoint_id": "ep-789",
                "provider_name": "OpenAI",
                "usage": 1.25,
                "byok_usage_inference": 0.5,
                "requests": 100,
                "prompt_tokens": 4000,
                "completion_tokens": 2000,
                "reasoning_tokens": 50
            }
        """.trimIndent()

        val activityItem = OpenRouterJson.decodeFromString<ActivityItem>(json)

        activityItem.date shouldBe "2024-03-20"
        activityItem.model shouldBe "openai/gpt-4o"
        activityItem.modelPermaslug shouldBe "openai/gpt-4o-2024-05-13"
        activityItem.endpointId shouldBe "ep-789"
        activityItem.providerName shouldBe "OpenAI"
        activityItem.usage shouldBe 1.25
        activityItem.byokUsageInference shouldBe 0.5
        activityItem.requests shouldBe 100
        activityItem.promptTokens shouldBe 4000
        activityItem.completionTokens shouldBe 2000
        activityItem.reasoningTokens shouldBe 50
    }
})
