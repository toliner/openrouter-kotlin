package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.account.Activity
import dev.toliner.openrouter.l1.account.Credits
import dev.toliner.openrouter.l1.account.KeyInfo
import io.ktor.client.HttpClient
import io.ktor.client.request.get

/**
 * API for account information and management.
 *
 * This API provides methods for retrieving information about the authenticated account,
 * including credit balance, API key details, and usage activity.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.account].
 *
 * @see Credits
 * @see KeyInfo
 * @see Activity
 */
public class AccountApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Retrieves the current credit balance for the account.
     *
     * Calls the `/credits` endpoint to get information about available credits,
     * including total balance and usage limits.
     *
     * @return A [Credits] object containing credit balance information.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see Credits
     */
    public suspend fun credits(): Credits = getAndDecode("${config.baseUrl}/credits")

    /**
     * Retrieves information about the current API key.
     *
     * Calls the `/key` endpoint to get details about the API key being used,
     * including its name, permissions, and rate limits.
     *
     * @return A [KeyInfo] object containing API key details.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see KeyInfo
     */
    public suspend fun keyInfo(): KeyInfo = getAndDecode("${config.baseUrl}/key")

    /**
     * Retrieves recent account activity.
     *
     * Calls the `/activity` endpoint to get information about recent API usage,
     * including request history and spending.
     *
     * @return An [Activity] object containing recent account activity.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see Activity
     */
    public suspend fun activity(): Activity = getAndDecode("${config.baseUrl}/activity")

    private suspend inline fun <reified T> getAndDecode(url: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
        }
        return response.decodeBodyOrThrow()
    }
}
