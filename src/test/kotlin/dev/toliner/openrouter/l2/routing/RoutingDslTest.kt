package dev.toliner.openrouter.l2.routing

import dev.toliner.openrouter.l1.chat.DataCollection
import dev.toliner.openrouter.l1.chat.MaxPrice
import dev.toliner.openrouter.l1.chat.PercentileCutoffs
import dev.toliner.openrouter.l1.chat.PreferredLatency
import dev.toliner.openrouter.l1.chat.PreferredThroughput
import dev.toliner.openrouter.l1.chat.ProviderPreferences
import dev.toliner.openrouter.l1.chat.ProviderSort
import dev.toliner.openrouter.l1.chat.Quantization
import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.l2.chat.chatRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RoutingDslTest : FunSpec({
    test("basic provider routing DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                order = listOf("OpenAI", "Azure")
                allowFallbacks = true
                requireParameters = true
                dataCollection = DataCollection.DENY
                ignore = listOf("Together")
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.order shouldBe listOf("OpenAI", "Azure")
        provider.allowFallbacks shouldBe true
        provider.requireParameters shouldBe true
        provider.dataCollection shouldBe DataCollection.DENY
        provider.ignore shouldBe listOf("Together")
    }

    test("only and new boolean fields DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                only = listOf("OpenAI", "Anthropic")
                zdr = true
                enforceDistillableText = false
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.only shouldBe listOf("OpenAI", "Anthropic")
        provider.zdr shouldBe true
        provider.enforceDistillableText shouldBe false
    }

    test("quantizations DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                quantizations = listOf(Quantization.FP16, Quantization.BF16)
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.quantizations shouldBe listOf(Quantization.FP16, Quantization.BF16)
    }

    test("sortBy simple string DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                sortBy("price")
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.sort shouldBe ProviderSort.Simple("price")
    }

    test("sort advanced object DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                sort {
                    by = "throughput"
                    partition = "model"
                }
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.sort shouldBe ProviderSort.Advanced(by = "throughput", partition = "model")
    }

    test("maxPrice DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                maxPrice {
                    prompt = "1.0"
                    completion = "2.0"
                }
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.maxPrice shouldBe MaxPrice(prompt = "1.0", completion = "2.0")
    }

    test("preferredMinThroughput number DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                preferredMinThroughput(128.0)
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.preferredMinThroughput shouldBe PreferredThroughput.Value(128.0)
    }

    test("preferredMinThroughput percentile DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                preferredMinThroughput {
                    p50 = 100.0
                    p90 = 50.0
                }
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.preferredMinThroughput shouldBe PreferredThroughput.Percentile(
            PercentileCutoffs(p50 = 100.0, p90 = 50.0)
        )
    }

    test("preferredMaxLatency number DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                preferredMaxLatency(2.5)
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.preferredMaxLatency shouldBe PreferredLatency.Value(2.5)
    }

    test("preferredMaxLatency percentile DSL") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello!")
            provider {
                preferredMaxLatency {
                    p50 = 1.0
                    p75 = 2.0
                    p90 = 5.0
                    p99 = 10.0
                }
            }
        }

        val provider = request.provider ?: error("provider missing")
        provider.preferredMaxLatency shouldBe PreferredLatency.Percentile(
            PercentileCutoffs(p50 = 1.0, p75 = 2.0, p90 = 5.0, p99 = 10.0)
        )
    }

    test("full provider JSON serialization") {
        val prefs = ProviderPreferences(
            order = listOf("OpenAI", "Azure"),
            allowFallbacks = false,
            requireParameters = true,
            dataCollection = DataCollection.ALLOW,
            ignore = listOf("Together")
        )

        val json = OpenRouterJson.encodeToString(ProviderPreferences.serializer(), prefs)
        json shouldBe """{"order":["OpenAI","Azure"],"ignore":["Together"],"allow_fallbacks":false,"require_parameters":true,"data_collection":"allow"}"""
    }
})
