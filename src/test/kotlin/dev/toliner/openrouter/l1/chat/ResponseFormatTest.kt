package dev.toliner.openrouter.l1.chat

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

class ResponseFormatTest : FunSpec({

    test("ResponseFormat.Text round-trip serialization") {
        val original: ResponseFormat = ResponseFormat.Text
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

        decoded shouldBe original
        json shouldBe """{"type":"text"}"""
    }

    test("ResponseFormat.JsonObject round-trip serialization") {
        val original: ResponseFormat = ResponseFormat.JsonObject
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

        decoded shouldBe original
        json shouldBe """{"type":"json_object"}"""
    }

    test("ResponseFormat.JsonSchema with full config round-trip serialization") {
        val format = ResponseFormat.JsonSchema(
            jsonSchema = JsonSchemaConfig(
                name = "test_schema",
                description = "A test schema",
                schema = buildJsonObject {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("name") { put("type", "string") }
                        putJsonObject("age") { put("type", "integer") }
                    }
                    putJsonArray("required") { add("name"); add("age") }
                    put("additionalProperties", false)
                },
                strict = true
            )
        )
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), format)
        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

        decoded shouldBe format
    }

    test("ResponseFormat.JsonSchema serializes to correct type field") {
        val format = ResponseFormat.JsonSchema(
            jsonSchema = JsonSchemaConfig(
                name = "simple_schema",
                strict = true
            )
        )
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), format)

        json.contains(""""type":"json_schema"""") shouldBe true
        json.contains(""""name":"simple_schema"""") shouldBe true
    }

    test("ResponseFormat.Grammar round-trip serialization") {
        val original: ResponseFormat = ResponseFormat.Grammar(grammar = "root ::= \"hello\" | \"world\"")
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

        decoded shouldBe original
        json shouldBe """{"type":"grammar","grammar":"root ::= \"hello\" | \"world\""}"""
    }

    test("ResponseFormat.Python round-trip serialization") {
        val original: ResponseFormat = ResponseFormat.Python(python = "class Output(BaseModel):\n    name: str\n    score: int")
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

        decoded shouldBe original
    }

    test("ResponseFormat.Python serializes to correct type and python fields") {
        val pythonCode = "class Output(BaseModel): ..."
        val original: ResponseFormat = ResponseFormat.Python(python = pythonCode)
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)

        json.contains(""""type":"python"""") shouldBe true
        json.contains(""""python":""") shouldBe true
    }

    test("ResponseFormat.JsonSchema with minimal config round-trip") {
        val original: ResponseFormat = ResponseFormat.JsonSchema(
            jsonSchema = JsonSchemaConfig(name = "minimal")
        )
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), original)
        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), json)

        decoded shouldBe original
    }

    test("Deserialization ignores unknown fields in json_schema response format") {
        val jsonWithExtras = """{"type":"json_schema","json_schema":{"name":"my_schema","strict":true},"extra_field":"ignored"}"""

        shouldNotThrowAny {
            OpenRouterJson.decodeFromString(ResponseFormat.serializer(), jsonWithExtras)
        }

        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), jsonWithExtras)
        decoded shouldBe ResponseFormat.JsonSchema(
            jsonSchema = JsonSchemaConfig(name = "my_schema", strict = true)
        )
    }

    test("Deserialization ignores unknown fields in text response format") {
        val jsonWithExtras = """{"type":"text","unexpected_key":"value"}"""

        shouldNotThrowAny {
            OpenRouterJson.decodeFromString(ResponseFormat.serializer(), jsonWithExtras)
        }

        val decoded = OpenRouterJson.decodeFromString(ResponseFormat.serializer(), jsonWithExtras)
        decoded shouldBe ResponseFormat.Text
    }

    test("ResponseFormat.JsonSchema null optional fields are omitted in serialization") {
        val format = ResponseFormat.JsonSchema(
            jsonSchema = JsonSchemaConfig(
                name = "sparse_output",
                description = null,
                schema = null,
                strict = null
            )
        )
        val json = OpenRouterJson.encodeToString(ResponseFormat.serializer(), format)

        json.contains(""""description":""") shouldBe false
        json.contains(""""schema":""") shouldBe false
        json.contains(""""strict":""") shouldBe false
        json.contains(""""name":"sparse_output"""") shouldBe true
    }
})
