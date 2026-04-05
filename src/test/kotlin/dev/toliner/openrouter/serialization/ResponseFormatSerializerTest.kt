package dev.toliner.openrouter.serialization

import dev.toliner.openrouter.l1.chat.JsonSchemaConfig
import dev.toliner.openrouter.l1.chat.ResponseFormat
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class ResponseFormatSerializerTest : FunSpec({

    context("deserialization") {
        test("deserializing {\"type\":\"text\"} produces ResponseFormat.Text") {
            val json = """{"type":"text"}"""
            val result = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

            result shouldBe ResponseFormat.Text
        }

        test("deserializing {\"type\":\"json_object\"} produces ResponseFormat.JsonObject") {
            val json = """{"type":"json_object"}"""
            val result = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

            result shouldBe ResponseFormat.JsonObject
        }

        test("deserializing full json_schema JSON produces correct ResponseFormat.JsonSchema") {
            val json = """
                {
                  "type": "json_schema",
                  "json_schema": {
                    "name": "output_schema",
                    "description": "Schema for structured output",
                    "schema": {
                      "type": "object",
                      "properties": {
                        "name": {"type": "string"},
                        "age": {"type": "integer"}
                      },
                      "required": ["name", "age"],
                      "additionalProperties": false
                    },
                    "strict": true
                  }
                }
            """.trimIndent()

            val result = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

            result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
            val jsonSchema = result as ResponseFormat.JsonSchema
            jsonSchema.jsonSchema.name shouldBe "output_schema"
            jsonSchema.jsonSchema.description shouldBe "Schema for structured output"
            jsonSchema.jsonSchema.strict shouldBe true
            jsonSchema.jsonSchema.schema shouldBe buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("name") { put("type", "string") }
                    putJsonObject("age") { put("type", "integer") }
                }
                putJsonArray("required") { add("name"); add("age") }
                put("additionalProperties", false)
            }
        }

        test("deserializing json_schema with minimal fields produces correct ResponseFormat.JsonSchema") {
            val json = """{"type":"json_schema","json_schema":{"name":"minimal_schema"}}"""
            val result = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

            result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
            val jsonSchema = result as ResponseFormat.JsonSchema
            jsonSchema.jsonSchema.name shouldBe "minimal_schema"
            jsonSchema.jsonSchema.description shouldBe null
            jsonSchema.jsonSchema.schema shouldBe null
            jsonSchema.jsonSchema.strict shouldBe null
        }

        test("deserializing grammar JSON produces correct ResponseFormat.Grammar") {
            val grammarStr = "root ::= \"hello\" | \"world\""
            val json = """{"type":"grammar","grammar":"root ::= \"hello\" | \"world\""}"""
            val result = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

            result shouldBe ResponseFormat.Grammar(grammar = grammarStr)
        }

        test("deserializing python JSON produces correct ResponseFormat.Python") {
            val pythonCode = "class Output(BaseModel):\n    name: str"
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), ResponseFormat.Python(python = pythonCode))
            val result = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

            result shouldBe ResponseFormat.Python(python = pythonCode)
        }

        test("deserializing unknown type throws error") {
            val json = """{"type":"unsupported_format"}"""

            shouldThrow<IllegalStateException> {
                OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            }
        }

        test("deserializing missing type field throws error") {
            val json = """{"json_schema":{"name":"no_type"}}"""

            shouldThrow<IllegalStateException> {
                OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            }
        }

        test("deserializing grammar type without grammar field throws error") {
            val json = """{"type":"grammar"}"""

            shouldThrow<IllegalStateException> {
                OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            }
        }

        test("deserializing python type without python field throws error") {
            val json = """{"type":"python"}"""

            shouldThrow<IllegalStateException> {
                OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            }
        }
    }

    context("serialization") {
        test("serializing ResponseFormat.Text produces correct JSON") {
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), ResponseFormat.Text)

            json shouldBe """{"type":"text"}"""
        }

        test("serializing ResponseFormat.JsonObject produces correct JSON") {
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), ResponseFormat.JsonObject)

            json shouldBe """{"type":"json_object"}"""
        }

        test("serializing ResponseFormat.Grammar produces correct JSON") {
            val grammar = "root ::= [a-z]+"
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), ResponseFormat.Grammar(grammar = grammar))

            json shouldBe """{"type":"grammar","grammar":"root ::= [a-z]+"}"""
        }

        test("serializing ResponseFormat.Python produces correct JSON") {
            val python = "class Output(BaseModel): ..."
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), ResponseFormat.Python(python = python))

            json shouldBe """{"type":"python","python":"class Output(BaseModel): ..."}"""
        }

        test("serializing ResponseFormat.JsonSchema produces correct type field") {
            val format = ResponseFormat.JsonSchema(
                jsonSchema = JsonSchemaConfig(
                    name = "test_schema",
                    strict = true
                )
            )
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), format)

            json.contains(""""type":"json_schema"""") shouldBe true
            json.contains(""""name":"test_schema"""") shouldBe true
            json.contains(""""strict":true""") shouldBe true
        }

        test("serializing ResponseFormat.JsonSchema with full schema produces valid JSON") {
            val format = ResponseFormat.JsonSchema(
                jsonSchema = JsonSchemaConfig(
                    name = "full_schema",
                    description = "A full schema",
                    schema = buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("field") { put("type", "string") }
                        }
                    },
                    strict = false
                )
            )
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), format)

            json.contains(""""type":"json_schema"""") shouldBe true
            json.contains(""""name":"full_schema"""") shouldBe true
            json.contains(""""description":"A full schema"""") shouldBe true
            json.contains(""""strict":false""") shouldBe true
        }

        test("serializing ResponseFormat.JsonSchema with null fields omits them") {
            val format = ResponseFormat.JsonSchema(
                jsonSchema = JsonSchemaConfig(
                    name = "sparse_schema",
                    description = null,
                    schema = null,
                    strict = null
                )
            )
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), format)

            json.contains("description") shouldBe false
            json.contains("\"schema\"") shouldBe false
            json.contains("strict") shouldBe false
        }
    }

    context("round-trip") {
        test("ResponseFormat.Text round-trips correctly") {
            val original: ResponseFormat = ResponseFormat.Text
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
            val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            decoded shouldBe original
        }

        test("ResponseFormat.JsonObject round-trips correctly") {
            val original: ResponseFormat = ResponseFormat.JsonObject
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
            val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            decoded shouldBe original
        }

        test("ResponseFormat.Grammar round-trips correctly") {
            val original: ResponseFormat = ResponseFormat.Grammar(grammar = "root ::= \"yes\" | \"no\"")
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
            val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            decoded shouldBe original
        }

        test("ResponseFormat.Python round-trips correctly") {
            val original: ResponseFormat = ResponseFormat.Python(python = "class Result(BaseModel):\n    value: int")
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
            val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            decoded shouldBe original
        }

        test("ResponseFormat.JsonSchema round-trips correctly") {
            val original: ResponseFormat = ResponseFormat.JsonSchema(
                jsonSchema = JsonSchemaConfig(
                    name = "roundtrip_schema",
                    description = "Round-trip test",
                    schema = buildJsonObject {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("id") { put("type", "integer") }
                        }
                    },
                    strict = true
                )
            )
            val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
            val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)
            decoded shouldBe original
        }
    }
})
