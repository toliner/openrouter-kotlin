package dev.toliner.openrouter.l2.chat

import dev.toliner.openrouter.l1.chat.JsonSchemaConfig
import dev.toliner.openrouter.l1.chat.ResponseFormat
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

@Serializable
private data class TestOutput(
    @SerialName("name") val name: String,
    @SerialName("score") val score: Int,
    @SerialName("tags") val tags: List<String>
)

class ResponseFormatBuilderTest : FunSpec({

    test("text() produces ResponseFormat.Text") {
        val result = ResponseFormatBuilder().apply { text() }.build()

        result shouldBe ResponseFormat.Text
    }

    test("jsonObject() produces ResponseFormat.JsonObject") {
        val result = ResponseFormatBuilder().apply { jsonObject() }.build()

        result shouldBe ResponseFormat.JsonObject
    }

    test("grammar() produces correct ResponseFormat.Grammar") {
        val grammarStr = "root ::= [a-zA-Z]+"
        val result = ResponseFormatBuilder().apply { grammar(grammarStr) }.build()

        result shouldBe ResponseFormat.Grammar(grammar = grammarStr)
    }

    test("python() produces correct ResponseFormat.Python") {
        val pythonCode = "class Output(BaseModel):\n    name: str\n    score: int"
        val result = ResponseFormatBuilder().apply { python(pythonCode) }.build()

        result shouldBe ResponseFormat.Python(python = pythonCode)
    }

    test("jsonSchema(name) { ... } manual builder produces correct ResponseFormat.JsonSchema") {
        val schema = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("name") { put("type", "string") }
                putJsonObject("age") { put("type", "integer") }
            }
            putJsonArray("required") { add("name"); add("age") }
            put("additionalProperties", false)
        }

        val result = ResponseFormatBuilder().apply {
            jsonSchema("my_schema") {
                description = "Custom schema for testing"
                strict = true
                this.schema = schema
            }
        }.build()

        result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
        val jsonSchemaFormat = result as ResponseFormat.JsonSchema
        jsonSchemaFormat.jsonSchema.name shouldBe "my_schema"
        jsonSchemaFormat.jsonSchema.description shouldBe "Custom schema for testing"
        jsonSchemaFormat.jsonSchema.strict shouldBe true
        jsonSchemaFormat.jsonSchema.schema shouldBe schema
    }

    test("jsonSchema(name) { ... } with null fields produces ResponseFormat.JsonSchema with null optionals") {
        val result = ResponseFormatBuilder().apply {
            jsonSchema("bare_schema") {}
        }.build()

        result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
        val jsonSchemaFormat = result as ResponseFormat.JsonSchema
        jsonSchemaFormat.jsonSchema.name shouldBe "bare_schema"
        jsonSchemaFormat.jsonSchema.description shouldBe null
        jsonSchemaFormat.jsonSchema.schema shouldBe null
        jsonSchemaFormat.jsonSchema.strict shouldBe null
    }

    test("jsonSchema<T>(name) auto-generates schema from @Serializable type") {
        val result = ResponseFormatBuilder().apply {
            jsonSchema<TestOutput>("test_output")
        }.build()

        result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
        val jsonSchemaFormat = result as ResponseFormat.JsonSchema
        jsonSchemaFormat.jsonSchema.name shouldBe "test_output"
        jsonSchemaFormat.jsonSchema.strict shouldBe true
        jsonSchemaFormat.jsonSchema.schema.shouldNotBeNull()

        val schema = jsonSchemaFormat.jsonSchema.schema!!
        schema["type"]?.toString() shouldBe """"object""""
        schema["properties"].shouldNotBeNull()
        val properties = schema["properties"]!!
        properties.toString().contains("name") shouldBe true
        properties.toString().contains("score") shouldBe true
        properties.toString().contains("tags") shouldBe true
    }

    test("jsonSchema<T>(name, description) includes description in config") {
        val result = ResponseFormatBuilder().apply {
            jsonSchema<TestOutput>("test_output", description = "Extracts test output from text")
        }.build()

        result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
        val jsonSchemaFormat = result as ResponseFormat.JsonSchema
        jsonSchemaFormat.jsonSchema.name shouldBe "test_output"
        jsonSchemaFormat.jsonSchema.description shouldBe "Extracts test output from text"
    }

    test("jsonSchema<T>(name, strict=false) sets strict to false") {
        val result = ResponseFormatBuilder().apply {
            jsonSchema<TestOutput>("test_output_loose", strict = false)
        }.build()

        result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
        val jsonSchemaFormat = result as ResponseFormat.JsonSchema
        jsonSchemaFormat.jsonSchema.name shouldBe "test_output_loose"
        jsonSchemaFormat.jsonSchema.strict shouldBe false
    }

    test("jsonSchema<T>(name, strict=null) sets strict to null") {
        val result = ResponseFormatBuilder().apply {
            jsonSchema<TestOutput>("test_output_no_strict", strict = null)
        }.build()

        result.shouldBeInstanceOf<ResponseFormat.JsonSchema>()
        val jsonSchemaFormat = result as ResponseFormat.JsonSchema
        jsonSchemaFormat.jsonSchema.strict shouldBe null
    }

    test("build() without calling any format method throws IllegalArgumentException") {
        shouldThrow<IllegalArgumentException> {
            ResponseFormatBuilder().build()
        }
    }

    test("build() error message is descriptive") {
        val exception = runCatching {
            ResponseFormatBuilder().build()
        }.exceptionOrNull()

        exception.shouldNotBeNull()
        exception.shouldBeInstanceOf<IllegalArgumentException>()
    }

    test("responseFormat DSL in chatRequest produces correct format") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Hello")
            responseFormat = ResponseFormat.Text
        }

        request.responseFormat shouldBe ResponseFormat.Text
    }

    test("JsonSchemaConfigBuilder sets all fields correctly") {
        val schema = buildJsonObject { put("type", "object") }
        val config = JsonSchemaConfigBuilder("config_test").apply {
            description = "Test description"
            this.schema = schema
            strict = true
        }.build()

        config shouldBe JsonSchemaConfig(
            name = "config_test",
            description = "Test description",
            schema = schema,
            strict = true
        )
    }

    test("JsonSchemaConfigBuilder with only name builds minimal config") {
        val config = JsonSchemaConfigBuilder("only_name").build()

        config shouldBe JsonSchemaConfig(
            name = "only_name",
            description = null,
            schema = null,
            strict = null
        )
    }
})
