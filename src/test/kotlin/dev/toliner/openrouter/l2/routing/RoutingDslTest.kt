package dev.toliner.openrouter.l2.routing

import dev.toliner.openrouter.l1.chat.ProviderPreferences
import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.l2.chat.chatRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RoutingDslTest : FunSpec({
    test("provider routing DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                order = listOf("OpenAI", "Azure")
                allowFallbacks = true
                requireParameters = true
                dataCollection = "deny"
                preferredMinThroughput = 128
                ignore = listOf("Together")
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.order shouldBe listOf("OpenAI", "Azure")
        provider.allowFallbacks shouldBe true
        provider.requireParameters shouldBe true
        provider.dataCollection shouldBe "deny"
        provider.preferredMinThroughput shouldBe 128
        provider.ignore shouldBe listOf("Together")
    }

    test("provider JSON serialization") {
        val prefs = ProviderPreferences(
            order = listOf("OpenAI", "Azure"),
            allowFallbacks = false,
            requireParameters = true,
            dataCollection = "allow",
            preferredMinThroughput = 64,
            ignore = listOf("Together")
        )

        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        json shouldBe """{"order":["OpenAI","Azure"],"allow_fallbacks":false,"require_parameters":true,"data_collection":"allow","preferred_min_throughput":64,"ignore":["Together"]}"""
    }
})
