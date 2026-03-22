package dev.toliner.openrouter.client

import dev.toliner.openrouter.serialization.OpenRouterJson
import dev.toliner.openrouter.testutil.mockEngineWithResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.runBlocking

class ProvidersApiTest : FunSpec({
    test("list() sends GET /providers and returns Provider list") {
        val engine = mockEngineWithResponse(
            responseBody = """
                {
                    "data": [
                        {
                            "name": "OpenAI",
                            "slug": "openai",
                            "privacy_policy_url": "https://openai.com/privacy",
                            "terms_of_service_url": "https://openai.com/terms",
                            "status_page_url": "https://status.openai.com"
                        },
                        {
                            "name": "Anthropic",
                            "slug": "anthropic",
                            "privacy_policy_url": "https://anthropic.com/privacy"
                        }
                    ]
                }
            """.trimIndent(),
            requestValidator = { req ->
                req.method shouldBe HttpMethod.Get
                req.url.toString() shouldBe "https://openrouter.ai/api/v1/providers"
                req.headers[HttpHeaders.Authorization] shouldBe "Bearer test-key"
            }
        )

        OpenRouterClient(engine, OpenRouterConfig(apiKey = "test-key")).use { client ->
            val providers = runBlocking { client.providers.list() }
            providers.size shouldBe 2
            providers[0].name shouldBe "OpenAI"
            providers[0].slug shouldBe "openai"
            providers[0].privacyPolicyUrl shouldBe "https://openai.com/privacy"
            providers[0].termsOfServiceUrl shouldBe "https://openai.com/terms"
            providers[0].statusPageUrl shouldBe "https://status.openai.com"
            providers[1].name shouldBe "Anthropic"
            providers[1].slug shouldBe "anthropic"
            providers[1].privacyPolicyUrl shouldBe "https://anthropic.com/privacy"
            providers[1].termsOfServiceUrl shouldBe null
            providers[1].statusPageUrl shouldBe null
        }
    }
})
