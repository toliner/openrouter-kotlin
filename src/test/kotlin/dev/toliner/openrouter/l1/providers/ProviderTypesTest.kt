package dev.toliner.openrouter.l1.providers

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class ProviderTypesTest : FunSpec({
    test("Provider deserialization with all fields") {
        val json = """
            {
                "name": "OpenAI",
                "slug": "openai",
                "privacy_policy_url": "https://openai.com/privacy",
                "terms_of_service_url": "https://openai.com/terms",
                "status_page_url": "https://status.openai.com"
            }
        """.trimIndent()

        val provider = OpenRouterJson.decodeFromString<Provider>(json)

        provider.name shouldBe "OpenAI"
        provider.slug shouldBe "openai"
        provider.privacyPolicyUrl shouldBe "https://openai.com/privacy"
        provider.termsOfServiceUrl shouldBe "https://openai.com/terms"
        provider.statusPageUrl shouldBe "https://status.openai.com"
    }

    test("Provider deserialization with minimal fields") {
        val json = """
            {
                "name": "TestProvider",
                "slug": "test-provider"
            }
        """.trimIndent()

        val provider = OpenRouterJson.decodeFromString<Provider>(json)

        provider.name shouldBe "TestProvider"
        provider.slug shouldBe "test-provider"
        provider.privacyPolicyUrl shouldBe null
        provider.termsOfServiceUrl shouldBe null
        provider.statusPageUrl shouldBe null
    }
})
