package dev.toliner.openrouter.l1.generation

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GenerationTypesTest : FunSpec({
    test("Generation should deserialize with all fields") {
        val json = """
            {
                "id": "gen_01JAFQZR0XGPVT2B9FNXMXYW9F",
                "model": "openai/gpt-4",
                "usage": {
                    "prompt_tokens": 50,
                    "completion_tokens": 100,
                    "total_tokens": 150,
                    "cost": 0.0075
                },
                "tokens": 150,
                "streamed": false,
                "generation_time": 1234,
                "created_at": "2024-03-22T10:30:00Z",
                "native_tokens_prompt": 45,
                "native_tokens_completion": 95
            }
        """.trimIndent()

        val generation = OpenRouterJson.decodeFromString<Generation>(json)

        generation.id shouldBe "gen_01JAFQZR0XGPVT2B9FNXMXYW9F"
        generation.model shouldBe "openai/gpt-4"
        generation.usage.promptTokens shouldBe 50
        generation.usage.completionTokens shouldBe 100
        generation.usage.totalTokens shouldBe 150
        generation.usage.cost shouldBe 0.0075
        generation.tokens shouldBe 150
        generation.streamed shouldBe false
        generation.generationTime shouldBe 1234
        generation.createdAt shouldBe "2024-03-22T10:30:00Z"
        generation.nativeTokensPrompt shouldBe 45
        generation.nativeTokensCompletion shouldBe 95
    }

    test("Generation should deserialize with optional fields absent") {
        val json = """
            {
                "id": "gen_02",
                "model": "anthropic/claude-3",
                "usage": {
                    "prompt_tokens": 20,
                    "completion_tokens": 30,
                    "total_tokens": 50,
                    "cost": 0.002
                },
                "tokens": 50,
                "streamed": true,
                "generation_time": 500,
                "created_at": "2024-03-22T11:00:00Z"
            }
        """.trimIndent()

        val generation = OpenRouterJson.decodeFromString<Generation>(json)

        generation.id shouldBe "gen_02"
        generation.model shouldBe "anthropic/claude-3"
        generation.streamed shouldBe true
        generation.nativeTokensPrompt shouldBe null
        generation.nativeTokensCompletion shouldBe null
    }

    test("GenerationUsage should deserialize with cost as string") {
        val json = """
            {
                "prompt_tokens": 100,
                "completion_tokens": 200,
                "total_tokens": 300,
                "cost": 0.015
            }
        """.trimIndent()

        val usage = OpenRouterJson.decodeFromString<GenerationUsage>(json)

        usage.promptTokens shouldBe 100
        usage.completionTokens shouldBe 200
        usage.totalTokens shouldBe 300
        usage.cost shouldBe 0.015
    }
})
