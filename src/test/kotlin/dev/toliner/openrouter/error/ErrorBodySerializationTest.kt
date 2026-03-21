package dev.toliner.openrouter.error

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ErrorBodySerializationTest : FunSpec({
    test("deserialize full error response with provider_error") {
        val json = """{"error":{"message":"boom","code":429,"provider_error":{"message":"upstream boom","code":123}}}"""

        val result = OpenRouterJson.decodeFromString(ErrorResponse.serializer(), json)

        result shouldBe ErrorResponse(
            error = ErrorBody(
                message = "boom",
                code = 429,
                providerError = ProviderError(message = "upstream boom", code = 123),
            ),
        )
    }

    test("deserialize error response without provider_error") {
        val json = """{"error":{"message":"boom","code":400}}"""

        val result = OpenRouterJson.decodeFromString(ErrorResponse.serializer(), json)

        result shouldBe ErrorResponse(
            error = ErrorBody(
                message = "boom",
                code = 400,
                providerError = null,
            ),
        )
    }

    test("deserialize minimal error body") {
        val json = """{"message":"boom"}"""

        val result = OpenRouterJson.decodeFromString(ErrorBody.serializer(), json)

        result shouldBe ErrorBody(message = "boom")
    }
})
