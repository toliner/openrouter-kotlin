package dev.toliner.openrouter.l1.models

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ModelTypesTest : FunSpec({
    test("Model deserialization - full model with all fields") {
        val json = """
            {
              "id": "openai/gpt-3.5-turbo",
              "canonical_slug": "openai/gpt-3.5-turbo",
              "name": "OpenAI: GPT-3.5 Turbo",
              "created": 1692901234,
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
                "instruct_type": "none",
                "input_modalities": ["text"],
                "output_modalities": ["text"]
              },
              "top_provider": {
                "context_length": 16385,
                "max_completion_tokens": 4096,
                "is_moderated": true
              },
              "per_request_limits": null,
              "supported_parameters": ["temperature", "top_p", "max_tokens"],
              "default_parameters": {
                "temperature": 0.7,
                "top_p": 0.9
              },
              "knowledge_cutoff": "2024-10-01",
              "expiration_date": null,
              "links": {
                "details": "/api/v1/models/openai/gpt-3.5-turbo/endpoints"
              }
            }
        """.trimIndent()

        val model = OpenRouterJson.decodeFromString<Model>(json)
        
        model.id shouldBe "openai/gpt-3.5-turbo"
        model.canonicalSlug shouldBe "openai/gpt-3.5-turbo"
        model.name shouldBe "OpenAI: GPT-3.5 Turbo"
        model.created shouldBe 1692901234
        model.description shouldBe "GPT-3.5 Turbo is a fast, inexpensive model for simple tasks."
        
        model.pricing.prompt shouldBe "0.0000005"
        model.pricing.completion shouldBe "0.0000015"
        model.pricing.request shouldBe "0.0"
        model.pricing.image shouldBe "0.0"
        
        model.contextLength shouldBe 16385
        
        model.architecture.modality shouldBe "text->text"
        model.architecture.tokenizer shouldBe "GPT"
        model.architecture.instructType shouldBe "none"
        model.architecture.inputModalities shouldBe listOf("text")
        model.architecture.outputModalities shouldBe listOf("text")
        
        model.topProvider.contextLength shouldBe 16385
        model.topProvider.maxCompletionTokens shouldBe 4096
        model.topProvider.isModerated shouldBe true
        
        model.perRequestLimits shouldBe null
        model.supportedParameters shouldBe listOf("temperature", "top_p", "max_tokens")
        model.defaultParameters shouldNotBe null
        model.defaultParameters?.temperature shouldBe 0.7
        model.defaultParameters?.topP shouldBe 0.9
        model.knowledgeCutoff shouldBe "2024-10-01"
        model.expirationDate shouldBe null
        model.links.details shouldBe "/api/v1/models/openai/gpt-3.5-turbo/endpoints"
    }
    
    test("Model deserialization - ignores unknown fields") {
        val json = """
            {
              "id": "test/model",
              "canonical_slug": "test/model",
              "name": "Test Model",
              "created": 1692901234,
              "pricing": {
                "prompt": "0.001",
                "completion": "0.002"
              },
              "context_length": 8192,
              "architecture": {
                "modality": "text->text",
                "tokenizer": "GPT",
                "instruct_type": "none",
                "input_modalities": ["text"],
                "output_modalities": ["text"]
              },
              "top_provider": {
                "context_length": 8192,
                "max_completion_tokens": 2048,
                "is_moderated": false
              },
              "per_request_limits": null,
              "supported_parameters": [],
              "default_parameters": null,
              "links": {"details": "/test"},
              "unknown_field": "should be ignored",
              "another_unknown": 12345
            }
        """.trimIndent()

        val model = OpenRouterJson.decodeFromString<Model>(json)
        model.id shouldBe "test/model"
        model.name shouldBe "Test Model"
        model.pricing.request shouldBe null
        model.pricing.image shouldBe null
    }
    
    test("ModelList deserialization - data array with multiple models") {
        val json = """
            {
              "data": [
                {
                  "id": "openai/gpt-4",
                  "canonical_slug": "openai/gpt-4",
                  "name": "OpenAI: GPT-4",
                  "created": 1692901234,
                  "pricing": {
                    "prompt": "0.00003",
                    "completion": "0.00006"
                  },
                  "context_length": 8192,
                  "architecture": {
                    "modality": "text->text",
                    "input_modalities": ["text"],
                    "output_modalities": ["text"]
                  },
                  "top_provider": {
                    "context_length": 8192,
                    "max_completion_tokens": 4096,
                    "is_moderated": true
                  },
                  "per_request_limits": null,
                  "supported_parameters": ["temperature"],
                  "default_parameters": null,
                  "links": {"details": "/test"}
                },
                {
                  "id": "anthropic/claude-3-opus",
                  "canonical_slug": "anthropic/claude-3-opus",
                  "name": "Anthropic: Claude 3 Opus",
                  "created": 1692901234,
                  "pricing": {
                    "prompt": "0.000015",
                    "completion": "0.000075"
                  },
                  "context_length": 200000,
                  "architecture": {
                    "modality": "text+image->text",
                    "tokenizer": "Claude",
                    "input_modalities": ["text", "image"],
                    "output_modalities": ["text"]
                  },
                  "top_provider": {
                    "context_length": 200000,
                    "max_completion_tokens": 4096,
                    "is_moderated": false
                  },
                  "per_request_limits": null,
                  "supported_parameters": ["temperature", "top_p"],
                  "default_parameters": null,
                  "links": {"details": "/test"}
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
                "completion": "0.0"
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

    test("Pricing deserialization with extended fields") {
        val json = """
            {
              "prompt": "0.00003",
              "completion": "0.00006",
              "request": "0",
              "image": "0",
              "image_token": "0.001",
              "audio": "0.002",
              "web_search": "0.003",
              "internal_reasoning": "0.004",
              "input_cache_read": "0.005",
              "input_cache_write": "0.006",
              "discount": 0.9
            }
        """.trimIndent()

        val pricing = OpenRouterJson.decodeFromString<Pricing>(json)
        pricing.prompt shouldBe "0.00003"
        pricing.completion shouldBe "0.00006"
        pricing.imageToken shouldBe "0.001"
        pricing.audio shouldBe "0.002"
        pricing.webSearch shouldBe "0.003"
        pricing.internalReasoning shouldBe "0.004"
        pricing.inputCacheRead shouldBe "0.005"
        pricing.inputCacheWrite shouldBe "0.006"
        pricing.discount shouldBe 0.9
    }

    test("PerRequestLimits deserialization") {
        val json = """
            {
              "prompt_tokens": 1000,
              "completion_tokens": 500
            }
        """.trimIndent()

        val limits = OpenRouterJson.decodeFromString<PerRequestLimits>(json)
        limits.promptTokens shouldBe 1000
        limits.completionTokens shouldBe 500
    }

    test("DefaultParameters deserialization") {
        val json = """
            {
              "temperature": 0.7,
              "top_p": 0.9,
              "top_k": 50,
              "frequency_penalty": 0.5,
              "presence_penalty": 0.3,
              "repetition_penalty": 1.1
            }
        """.trimIndent()

        val params = OpenRouterJson.decodeFromString<DefaultParameters>(json)
        params.temperature shouldBe 0.7
        params.topP shouldBe 0.9
        params.topK shouldBe 50
        params.frequencyPenalty shouldBe 0.5
        params.presencePenalty shouldBe 0.3
        params.repetitionPenalty shouldBe 1.1
    }
})
