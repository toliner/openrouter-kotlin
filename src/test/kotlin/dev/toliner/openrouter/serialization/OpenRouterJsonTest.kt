package dev.toliner.openrouter.serialization

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenRouterJsonTest : FunSpec({
    test("ignoreUnknownKeys should ignore extra JSON fields") {
        @Serializable
        data class TestData(val field: String)
        
        val json = """{"field":"value","extra":"ignored"}"""
        
        shouldNotThrowAny {
            OpenRouterJson.decodeFromString<TestData>(json)
        }
        
        val result = OpenRouterJson.decodeFromString<TestData>(json)
        result.field shouldBe "value"
    }
    
    test("explicitNulls=false should omit null fields in output") {
        @Serializable
        data class TestData(val field: String, val nullable: String? = null)
        
        val data = TestData("value", null)
        val json = OpenRouterJson.encodeToString(TestData.serializer(), data)
        
        json shouldBe """{"field":"value"}"""
    }
    
    test("coerceInputValues should handle type mismatches gracefully") {
        @Serializable
        data class TestData(val field: String = "default")
        
        val jsonWithNull = """{"field":null}"""
        
        shouldNotThrowAny {
            OpenRouterJson.decodeFromString<TestData>(jsonWithNull)
        }
        
        val result = OpenRouterJson.decodeFromString<TestData>(jsonWithNull)
        result.field shouldBe "default"
    }
    
    test("encodeDefaults=false should not encode default values") {
        @Serializable
        data class TestData(val field: String = "default")
        
        val data = TestData()
        val json = OpenRouterJson.encodeToString(TestData.serializer(), data)
        
        json shouldBe """{}"""
    }
})
