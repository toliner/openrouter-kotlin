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
                        "requests": 150,
                        "cost": 2.5
                    },
                    {
                        "date": "2024-03-21",
                        "requests": 200,
                        "cost": 3.75
                    }
                ]
            }
        """.trimIndent()

        val activity = OpenRouterJson.decodeFromString<Activity>(json)

        activity.data.size shouldBe 2
        activity.data[0].date shouldBe "2024-03-22"
        activity.data[0].requests shouldBe 150
        activity.data[0].cost shouldBe 2.5
        activity.data[1].date shouldBe "2024-03-21"
        activity.data[1].requests shouldBe 200
        activity.data[1].cost shouldBe 3.75
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

    test("DailyActivity should deserialize date, requests, and cost") {
        val json = """
            {
                "date": "2024-03-20",
                "requests": 100,
                "cost": 1.25
            }
        """.trimIndent()

        val dailyActivity = OpenRouterJson.decodeFromString<DailyActivity>(json)

        dailyActivity.date shouldBe "2024-03-20"
        dailyActivity.requests shouldBe 100
        dailyActivity.cost shouldBe 1.25
    }
})
