package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ProviderPreferencesTest : FunSpec({
    test("DataCollection enum round-trip") {
        val prefs = ProviderPreferences(dataCollection = DataCollection.DENY)
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"data_collection":"deny"}"""
    }

    test("DataCollection allow serialization") {
        val prefs = ProviderPreferences(dataCollection = DataCollection.ALLOW)
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)

        json shouldBe """{"data_collection":"allow"}"""
    }

    test("Quantization list round-trip") {
        val prefs = ProviderPreferences(
            quantizations = listOf(Quantization.FP16, Quantization.INT8, Quantization.UNKNOWN)
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"quantizations":["fp16","int8","unknown"]}"""
    }

    test("ProviderSort simple string round-trip") {
        val prefs = ProviderPreferences(sort = ProviderSort.Simple("price"))
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"sort":"price"}"""
    }

    test("ProviderSort advanced object round-trip") {
        val prefs = ProviderPreferences(
            sort = ProviderSort.Advanced(by = "throughput", partition = "model")
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"sort":{"by":"throughput","partition":"model"}}"""
    }

    test("MaxPrice round-trip") {
        val prefs = ProviderPreferences(
            maxPrice = MaxPrice(prompt = "1.0", completion = "2.0")
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"max_price":{"prompt":"1.0","completion":"2.0"}}"""
    }

    test("PreferredThroughput number value round-trip") {
        val prefs = ProviderPreferences(
            preferredMinThroughput = PreferredThroughput.Value(128.0)
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"preferred_min_throughput":128.0}"""
    }

    test("PreferredThroughput percentile cutoffs round-trip") {
        val prefs = ProviderPreferences(
            preferredMinThroughput = PreferredThroughput.Percentile(
                PercentileCutoffs(p50 = 100.0, p90 = 50.0)
            )
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"preferred_min_throughput":{"p50":100.0,"p90":50.0}}"""
    }

    test("PreferredLatency number value round-trip") {
        val prefs = ProviderPreferences(
            preferredMaxLatency = PreferredLatency.Value(2.5)
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"preferred_max_latency":2.5}"""
    }

    test("PreferredLatency percentile cutoffs round-trip") {
        val prefs = ProviderPreferences(
            preferredMaxLatency = PreferredLatency.Percentile(
                PercentileCutoffs(p50 = 1.0, p75 = 2.0, p90 = 5.0, p99 = 10.0)
            )
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"preferred_max_latency":{"p50":1.0,"p75":2.0,"p90":5.0,"p99":10.0}}"""
    }

    test("new boolean fields round-trip") {
        val prefs = ProviderPreferences(
            zdr = true,
            enforceDistillableText = false
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"zdr":true,"enforce_distillable_text":false}"""
    }

    test("only field round-trip") {
        val prefs = ProviderPreferences(
            only = listOf("OpenAI", "Anthropic")
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
        json shouldBe """{"only":["OpenAI","Anthropic"]}"""
    }

    test("full ProviderPreferences round-trip") {
        val prefs = ProviderPreferences(
            order = listOf("OpenAI", "Anthropic"),
            only = listOf("OpenAI", "Anthropic", "Google"),
            ignore = listOf("Together"),
            allowFallbacks = true,
            requireParameters = true,
            dataCollection = DataCollection.DENY,
            zdr = true,
            enforceDistillableText = false,
            quantizations = listOf(Quantization.FP16, Quantization.BF16),
            sort = ProviderSort.Simple("price"),
            maxPrice = MaxPrice(prompt = "1.0", completion = "2.0"),
            preferredMinThroughput = PreferredThroughput.Value(128.0),
            preferredMaxLatency = PreferredLatency.Value(5.0)
        )
        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        val decoded = OpenRouterJson.decodeFromString(ProviderPreferences.serializer(), json)

        decoded shouldBe prefs
    }
})
