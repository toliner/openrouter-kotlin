package dev.toliner.openrouter.l2.tools

import dev.toliner.openrouter.l1.chat.FunctionTool
import dev.toliner.openrouter.l2.chat.chatRequest
import dev.toliner.openrouter.serialization.ToolChoice
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ToolDslTest : FunSpec({
    test("tool definition DSL - single function with parameters") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("What's the weather in Tokyo?")
            tools {
                function("get_weather") {
                    description = "Get the current weather"
                    parameters {
                        property("location", "string") {
                            description = "The city name"
                        }
                        required("location")
                    }
                }
            }
            toolChoice = ToolChoice.Mode("auto")
        }
        
        request.tools.shouldNotBeNull()
        request.tools!! shouldHaveSize 1
        
        val tool = request.tools!![0]
        tool.type shouldBe "function"
        tool.function.name shouldBe "get_weather"
        tool.function.description shouldBe "Get the current weather"
        
        val params = tool.function.parameters!!.jsonObject
        params["type"]?.jsonPrimitive?.content shouldBe "object"
        
        val properties = params["properties"]?.jsonObject
        properties.shouldNotBeNull()
        val locationProp = properties["location"]?.jsonObject
        locationProp.shouldNotBeNull()
        locationProp["type"]?.jsonPrimitive?.content shouldBe "string"
        locationProp["description"]?.jsonPrimitive?.content shouldBe "The city name"
        
        val required = params["required"]?.jsonArray
        required.shouldNotBeNull()
        required shouldHaveSize 1
        required[0].jsonPrimitive.content shouldBe "location"
        
        request.toolChoice shouldBe ToolChoice.Mode("auto")
    }
    
    test("tool definition DSL - multiple properties with different types") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Book a flight")
            tools {
                function("book_flight") {
                    description = "Book a flight ticket"
                    parameters {
                        property("from", "string") {
                            description = "Departure city"
                        }
                        property("to", "string") {
                            description = "Arrival city"
                        }
                        property("date", "string") {
                            description = "Flight date in YYYY-MM-DD format"
                        }
                        property("passengers", "integer") {
                            description = "Number of passengers"
                        }
                        required("from", "to", "date")
                    }
                }
            }
        }
        
        val tool = request.tools!![0]
        val params = tool.function.parameters!!.jsonObject
        val properties = params["properties"]?.jsonObject!!
        
        properties.size shouldBe 4
        properties["from"]?.jsonObject?.get("type")?.jsonPrimitive?.content shouldBe "string"
        properties["to"]?.jsonObject?.get("type")?.jsonPrimitive?.content shouldBe "string"
        properties["date"]?.jsonObject?.get("type")?.jsonPrimitive?.content shouldBe "string"
        properties["passengers"]?.jsonObject?.get("type")?.jsonPrimitive?.content shouldBe "integer"
        
        val required = params["required"]?.jsonArray!!
        required shouldHaveSize 3
        required.map { it.jsonPrimitive.content }.toSet() shouldBe setOf("from", "to", "date")
    }
    
    test("tool definition DSL - multiple functions") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Help me plan a trip")
            tools {
                function("get_weather") {
                    description = "Get weather information"
                    parameters {
                        property("city", "string")
                        required("city")
                    }
                }
                function("search_hotels") {
                    description = "Search for hotels"
                    parameters {
                        property("city", "string")
                        property("checkin", "string")
                        property("checkout", "string")
                        required("city", "checkin", "checkout")
                    }
                }
                function("book_restaurant") {
                    description = "Book a restaurant"
                    parameters {
                        property("name", "string")
                        property("time", "string")
                        property("guests", "integer")
                        required("name", "time")
                    }
                }
            }
        }
        
        request.tools.shouldNotBeNull()
        request.tools!! shouldHaveSize 3
        
        request.tools!![0].function.name shouldBe "get_weather"
        request.tools!![1].function.name shouldBe "search_hotels"
        request.tools!![2].function.name shouldBe "book_restaurant"
        
        val weatherParams = request.tools!![0].function.parameters!!.jsonObject
        val weatherRequired = weatherParams["required"]?.jsonArray!!
        weatherRequired shouldHaveSize 1
        
        val hotelParams = request.tools!![1].function.parameters!!.jsonObject
        val hotelRequired = hotelParams["required"]?.jsonArray!!
        hotelRequired shouldHaveSize 3
        
        val restaurantParams = request.tools!![2].function.parameters!!.jsonObject
        val restaurantRequired = restaurantParams["required"]?.jsonArray!!
        restaurantRequired shouldHaveSize 2
    }
    
    test("tool definition DSL - function without description") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            tools {
                function("simple_func") {
                    parameters {
                        property("arg", "string")
                    }
                }
            }
        }
        
        val tool = request.tools!![0]
        tool.function.name shouldBe "simple_func"
        tool.function.description.shouldBeNull()
    }
    
    test("tool definition DSL - function without parameters") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            tools {
                function("no_params") {
                    description = "A function with no parameters"
                }
            }
        }
        
        val tool = request.tools!![0]
        tool.function.name shouldBe "no_params"
        tool.function.description shouldBe "A function with no parameters"
        tool.function.parameters.shouldBeNull()
    }
    
    test("tool definition DSL - property without description") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            tools {
                function("test_func") {
                    parameters {
                        property("simple_prop", "string")
                    }
                }
            }
        }
        
        val tool = request.tools!![0]
        val params = tool.function.parameters!!.jsonObject
        val prop = params["properties"]?.jsonObject?.get("simple_prop")?.jsonObject!!
        prop["type"]?.jsonPrimitive?.content shouldBe "string"
        prop["description"].shouldBeNull()
    }
    
    test("tool definition DSL - parameters without required fields") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            tools {
                function("optional_params") {
                    parameters {
                        property("opt1", "string")
                        property("opt2", "integer")
                    }
                }
            }
        }
        
        val tool = request.tools!![0]
        val params = tool.function.parameters!!.jsonObject
        params["required"].shouldBeNull()
    }
    
    test("ToolChoice.Auto variant") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            toolChoice = ToolChoice.Mode("auto")
        }
        
        request.toolChoice shouldBe ToolChoice.Mode("auto")
    }
    
    test("ToolChoice.None variant") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            toolChoice = ToolChoice.Mode("none")
        }
        
        request.toolChoice shouldBe ToolChoice.Mode("none")
    }
    
    test("ToolChoice.Required variant") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            toolChoice = ToolChoice.Mode("required")
        }
        
        request.toolChoice shouldBe ToolChoice.Mode("required")
    }
    
    test("ToolChoice.Function variant - specific function") {
        val request = chatRequest {
            model = "openai/gpt-4o"
            userMessage("Test")
            tools {
                function("func1") {
                    description = "First function"
                }
                function("func2") {
                    description = "Second function"
                }
            }
            toolChoice = ToolChoice.Function(
                function = dev.toliner.openrouter.serialization.FunctionChoice(name = "func1")
            )
        }
        
        request.toolChoice shouldBe ToolChoice.Function(
            function = dev.toliner.openrouter.serialization.FunctionChoice(name = "func1")
        )
    }
})
