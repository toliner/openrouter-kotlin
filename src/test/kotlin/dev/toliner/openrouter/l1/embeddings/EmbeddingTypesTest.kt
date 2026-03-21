package dev.toliner.openrouter.l1.embeddings

import dev.toliner.openrouter.l1.chat.Usage
import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.serialization.StringOrArray
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EmbeddingTypesTest : FunSpec({
    test("EmbeddingRequest serializes single string input") {
        val request = EmbeddingRequest(
            model = "openai/text-embedding-3-small",
            input = StringOrArray.Single("Hello"),
            encodingFormat = "float"
        )

        val json = OpenRouterJson.encodeToString(EmbeddingRequest.serializer(), request)

        json shouldBe """{"model":"openai/text-embedding-3-small","input":"Hello","encoding_format":"float"}"""
    }

    test("EmbeddingRequest serializes array input") {
        val request = EmbeddingRequest(
            model = "openai/text-embedding-3-small",
            input = StringOrArray.Multiple(listOf("Hello", "World"))
        )

        val json = OpenRouterJson.encodeToString(EmbeddingRequest.serializer(), request)

        json shouldBe """{"model":"openai/text-embedding-3-small","input":["Hello","World"]}"""
    }

    test("EmbeddingResponse deserializes data and usage") {
        val json = """
            {
              "data": [
                {
                  "embedding": [0.1, -0.2, 0.3],
                  "index": 0,
                  "object": "embedding"
                }
              ],
              "model": "openai/text-embedding-3-small",
              "usage": {
                "prompt_tokens": 2,
                "completion_tokens": 0,
                "total_tokens": 2
              }
            }
        """.trimIndent()

        val response = OpenRouterJson.decodeFromString(EmbeddingResponse.serializer(), json)

        response.model shouldBe "openai/text-embedding-3-small"
        response.data.size shouldBe 1
        response.data[0].embedding shouldBe listOf(0.1, -0.2, 0.3)
        response.data[0].index shouldBe 0
        response.data[0].`object` shouldBe "embedding"
        response.usage shouldBe Usage(promptTokens = 2, completionTokens = 0, totalTokens = 2)
    }
})
