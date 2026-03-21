package dev.toliner.openrouter.l1.models

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

class ModelTypesTest : FunSpec({
    test("Model deserialization - full model with all fields") {
        val json = """
            {
              "id": "openai/gpt-3.5-turbo",
              "name": "OpenAI: GPT-3.5 Turbo",
              "description": "GPT-3.5 Turbo is a fast, inexpensive model for simple tasks.",
              "pricing": {
                "prompt": "0.0000005",
                "completion": "0.0000015",
                "request": "0.0",
                "image": "0.0"
              },
              "context_length": 16385,
              "architecture": {
                "modality": "text->text",
                "tokenizer": "GPT",
                "instruct_type": "none"
              },
              "top_provider": {
                "context_length": 16385,
                "max_completion_tokens": 4096,
                "is_moderated": true
              },
              "per_request_limits": {
                "prompt_tokens": null,
                "completion_tokens": null
              }
            }
        """.trimIndent()

        val model = OpenRouterJson.decodeFromString<Model>(json)
        
        model.id shouldBe "openai/gpt-3.5-turbo"
        model.name shouldBe "OpenAI: GPT-3.5 Turbo"
        model.description shouldBe "GPT-3.5 Turbo is a fast, inexpensive model for simple tasks."
        
        model.pricing.prompt shouldBe "0.0000005"
        model.pricing.completion shouldBe "0.0000015"
        model.pricing.request shouldBe "0.0"
        model.pricing.image shouldBe "0.0"
        
        model.contextLength shouldBe 16385
        
        model.architecture.modality shouldBe "text->text"
        model.architecture.tokenizer shouldBe "GPT"
        model.architecture.instructType shouldBe "none"
        
        model.topProvider.contextLength shouldBe 16385
        model.topProvider.maxCompletionTokens shouldBe 4096
        model.topProvider.isModerated shouldBe true
        
        model.perRequestLimits shouldNotBe null
        model.perRequestLimits?.promptTokens shouldBe null
        model.perRequestLimits?.completionTokens shouldBe null
    }
    
    test("Model deserialization - ignores unknown fields") {
        val json = """
            {
              "id": "test/model",
              "name": "Test Model",
              "pricing": {
                "prompt": "0.001",
                "completion": "0.002",
                "request": "0.0",
                "image": "0.0"
              },
              "context_length": 8192,
              "architecture": {
                "modality": "text->text",
                "tokenizer": "GPT",
                "instruct_type": "none"
              },
              "top_provider": {
                "context_length": 8192,
                "max_completion_tokens": 2048,
                "is_moderated": false
              },
              "unknown_field": "should be ignored",
              "another_unknown": 12345
            }
        """.trimIndent()

        val model = OpenRouterJson.decodeFromString<Model>(json)
        model.id shouldBe "test/model"
        model.name shouldBe "Test Model"
    }
    
    test("ModelList deserialization - data array with multiple models") {
        val json = """
            {
              "data": [
                {
                  "id": "openai/gpt-4",
                  "name": "OpenAI: GPT-4",
                  "pricing": {
                    "prompt": "0.00003",
                    "completion": "0.00006",
                    "request": "0.0",
                    "image": "0.0"
                  },
                  "context_length": 8192,
                  "architecture": {
                    "modality": "text->text",
                    "tokenizer": "GPT",
                    "instruct_type": "none"
                  },
                  "top_provider": {
                    "context_length": 8192,
                    "max_completion_tokens": 4096,
                    "is_moderated": true
                  }
                },
                {
                  "id": "anthropic/claude-3-opus",
                  "name": "Anthropic: Claude 3 Opus",
                  "pricing": {
                    "prompt": "0.000015",
                    "completion": "0.000075",
                    "request": "0.0",
                    "image": "0.0"
                  },
                  "context_length": 200000,
                  "architecture": {
                    "modality": "text+image->text",
                    "tokenizer": "Claude",
                    "instruct_type": "none"
                  },
                  "top_provider": {
                    "context_length": 200000,
                    "max_completion_tokens": 4096,
                    "is_moderated": false
                  }
                }
              ]
            }
        """.trimIndent()

        val modelList = OpenRouterJson.decodeFromString<ModelList>(json)
        
        modelList.data.size shouldBe 2
        modelList.data[0].id shouldBe "openai/gpt-4"
        modelList.data[0].name shouldBe "OpenAI: GPT-4"
        modelList.data[1].id shouldBe "anthropic/claude-3-opus"
        modelList.data[1].name shouldBe "Anthropic: Claude 3 Opus"
    }
    
    test("ModelList deserialization - ignores unknown top-level fields") {
        val json = """
            {
              "data": [
                {
                  "id": "test/model",
                  "name": "Test",
                  "pricing": {
                    "prompt": "0.001",
                    "completion": "0.002",
                    "request": "0.0",
                    "image": "0.0"
                  },
                  "context_length": 4096,
                  "architecture": {
                    "modality": "text->text",
                    "tokenizer": "GPT",
                    "instruct_type": "none"
                  },
                  "top_provider": {
                    "context_length": 4096,
                    "max_completion_tokens": 1024,
                    "is_moderated": false
                  }
                }
              ],
              "meta": {"unknown": "field"},
              "version": "1.0"
            }
        """.trimIndent()

        val modelList = OpenRouterJson.decodeFromString<ModelList>(json)
        modelList.data.size shouldBe 1
    }
    
    test("ModelsCount deserialization") {
        val json = """
            {
              "count": 142
            }
        """.trimIndent()

        val modelsCount = OpenRouterJson.decodeFromString<ModelsCount>(json)
        modelsCount.count shouldBe 142
    }
    
    test("ModelEndpoint deserialization") {
        val json = """
            {
              "url": "https://api.openai.com/v1/chat/completions",
              "headers": {
                "Authorization": "Bearer sk-xxx"
              }
            }
        """.trimIndent()

        val endpoint = OpenRouterJson.decodeFromString<ModelEndpoint>(json)
        endpoint.url shouldBe "https://api.openai.com/v1/chat/completions"
        endpoint.headers shouldNotBe null
    }
    
    test("ZdrEndpoint deserialization") {
        val json = """
            {
              "id": "openai/gpt-4",
              "name": "OpenAI: GPT-4",
              "zdr_affected": true,
              "multiplier": 1.5
            }
        """.trimIndent()

        val zdr = OpenRouterJson.decodeFromString<ZdrEndpoint>(json)
        zdr.id shouldBe "openai/gpt-4"
        zdr.name shouldBe "OpenAI: GPT-4"
        zdr.zdrAffected shouldBe true
        zdr.multiplier shouldBe 1.5
    }
    
    test("EmbeddingModel deserialization") {
        val json = """
            {
              "id": "openai/text-embedding-ada-002",
              "name": "OpenAI: Text Embedding Ada 002",
              "pricing": {
                "prompt": "0.0000001",
                "completion": "0.0",
                "request": "0.0",
                "image": "0.0"
              },
              "context_length": 8191
            }
        """.trimIndent()

        val embeddingModel = OpenRouterJson.decodeFromString<EmbeddingModel>(json)
        embeddingModel.id shouldBe "openai/text-embedding-ada-002"
        embeddingModel.name shouldBe "OpenAI: Text Embedding Ada 002"
        embeddingModel.pricing.prompt shouldBe "0.0000001"
        embeddingModel.contextLength shouldBe 8191
    }
})
