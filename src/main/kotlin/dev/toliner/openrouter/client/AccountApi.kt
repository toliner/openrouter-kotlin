package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.account.Activity
import dev.toliner.openrouter.l1.account.Credits
import dev.toliner.openrouter.l1.account.KeyInfo
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class AccountApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun credits(): Credits = getAndDecode("${config.baseUrl}/credits")

    suspend fun keyInfo(): KeyInfo = getAndDecode("${config.baseUrl}/key")

    suspend fun activity(): Activity = getAndDecode("${config.baseUrl}/activity")

    private suspend inline fun <reified T> getAndDecode(url: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
        }
        return response.decodeBodyOrThrow()
    }
}
