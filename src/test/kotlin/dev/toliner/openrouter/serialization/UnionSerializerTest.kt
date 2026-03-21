package dev.toliner.openrouter.serialization

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class UnionSerializerTest : FunSpec({
    context("StringOrArray serializer") {
        test("deserialize string as Single") {
            val json = """"test string""""
            val result = OpenRouterJson.decodeFromString(StringOrArray.serializer(), json)
            
            result shouldBe StringOrArray.Single("test string")
        }
        
        test("deserialize array as Multiple") {
            val json = """["a","b","c"]"""
            val result = OpenRouterJson.decodeFromString(StringOrArray.serializer(), json)
            
            result shouldBe StringOrArray.Multiple(listOf("a", "b", "c"))
        }
        
        test("serialize Single as string") {
            val data = StringOrArray.Single("test")
            val json = OpenRouterJson.encodeToString(StringOrArray.serializer(), data)
            
            json shouldBe """"test""""
        }
        
        test("serialize Multiple as array") {
            val data = StringOrArray.Multiple(listOf("x", "y"))
            val json = OpenRouterJson.encodeToString(StringOrArray.serializer(), data)
            
            json shouldBe """["x","y"]"""
        }
        
        test("round-trip property test for Single") {
            checkAll(Arb.string()) { str ->
                val original = StringOrArray.Single(str)
                val json = OpenRouterJson.encodeToString(StringOrArray.serializer(), original)
                val deserialized = OpenRouterJson.decodeFromString(StringOrArray.serializer(), json)
                
                deserialized shouldBe original
            }
        }
        
        test("round-trip property test for Multiple") {
            checkAll(Arb.list(Arb.string(), 1..10)) { list ->
                val original = StringOrArray.Multiple(list)
                val json = OpenRouterJson.encodeToString(StringOrArray.serializer(), original)
                val deserialized = OpenRouterJson.decodeFromString(StringOrArray.serializer(), json)
                
                deserialized shouldBe original
            }
        }
    }
    
    context("Content serializer") {
        test("deserialize string as Text") {
            val json = """"simple text""""
            val result = OpenRouterJson.decodeFromString(Content.serializer(), json)
            
            result shouldBe Content.Text("simple text")
        }
        
        test("deserialize array as Parts") {
            val json = """[{"type":"text","text":"hello"},{"type":"text","text":"world"}]"""
            val result = OpenRouterJson.decodeFromString(Content.serializer(), json)
            
            result shouldBe Content.Parts(listOf(
                ContentPart.TextPart(text = "hello"),
                ContentPart.TextPart(text = "world")
            ))
        }
        
        test("serialize Text as string") {
            val data = Content.Text("test content")
            val json = OpenRouterJson.encodeToString(Content.serializer(), data)
            
            json shouldBe """"test content""""
        }
        
        test("serialize Parts as array") {
            val data = Content.Parts(listOf(
                ContentPart.TextPart(text = "part1")
            ))
            val json = OpenRouterJson.encodeToString(Content.serializer(), data)
            
            json shouldBe """[{"type":"text","text":"part1"}]"""
        }
        
        test("round-trip property test for Text") {
            checkAll(Arb.string()) { str ->
                val original = Content.Text(str)
                val json = OpenRouterJson.encodeToString(Content.serializer(), original)
                val deserialized = OpenRouterJson.decodeFromString(Content.serializer(), json)
                
                deserialized shouldBe original
            }
        }
        
        test("round-trip property test for Parts") {
            checkAll(Arb.list(Arb.string(), 1..5)) { texts ->
                val parts = texts.map { ContentPart.TextPart(text = it) }
                val original = Content.Parts(parts)
                val json = OpenRouterJson.encodeToString(Content.serializer(), original)
                val deserialized = OpenRouterJson.decodeFromString(Content.serializer(), json)
                
                deserialized shouldBe original
            }
        }
    }
    
    context("ToolChoice serializer") {
        test("deserialize string as Mode") {
            val json = """"auto""""
            val result = OpenRouterJson.decodeFromString(ToolChoice.serializer(), json)
            
            result shouldBe ToolChoice.Mode("auto")
        }
        
        test("deserialize object as Function") {
            val json = """{"type":"function","function":{"name":"test_func"}}"""
            val result = OpenRouterJson.decodeFromString(ToolChoice.serializer(), json)
            
            result shouldBe ToolChoice.Function(
                function = FunctionChoice(name = "test_func")
            )
        }
        
        test("serialize Mode as string") {
            val data = ToolChoice.Mode("none")
            val json = OpenRouterJson.encodeToString(ToolChoice.serializer(), data)
            
            json shouldBe """"none""""
        }
        
        test("serialize Function as object") {
            val data = ToolChoice.Function(
                function = FunctionChoice(name = "my_func")
            )
            val json = OpenRouterJson.encodeToString(ToolChoice.serializer(), data)
            
            json shouldBe """{"type":"function","function":{"name":"my_func"}}"""
        }
        
        test("round-trip property test for Mode") {
            checkAll(Arb.string()) { str ->
                val original = ToolChoice.Mode(str)
                val json = OpenRouterJson.encodeToString(ToolChoice.serializer(), original)
                val deserialized = OpenRouterJson.decodeFromString(ToolChoice.serializer(), json)
                
                deserialized shouldBe original
            }
        }
        
        test("round-trip property test for Function") {
            checkAll(Arb.string()) { name ->
                val original = ToolChoice.Function(FunctionChoice(name))
                val json = OpenRouterJson.encodeToString(ToolChoice.serializer(), original)
                val deserialized = OpenRouterJson.decodeFromString(ToolChoice.serializer(), json)
                
                deserialized shouldBe original
            }
        }
    }
})
