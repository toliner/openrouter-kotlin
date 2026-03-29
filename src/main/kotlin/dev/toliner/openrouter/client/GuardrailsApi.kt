package dev.toliner.openrouter.client

import dev.toliner.openrouter.l1.guardrails.AddAssignmentRequest
import dev.toliner.openrouter.l1.guardrails.CreateGuardrailRequest
import dev.toliner.openrouter.l1.guardrails.Guardrail
import dev.toliner.openrouter.l1.guardrails.GuardrailAssignment
import dev.toliner.openrouter.l1.guardrails.UpdateGuardrailRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * API for managing content guardrails.
 *
 * This API provides methods for creating, managing, and assigning guardrails that
 * enforce content policies and safety rules for model generations.
 *
 * Instances of this class are created internally by [OpenRouterClient].
 * Access via [OpenRouterClient.guardrails].
 *
 * @see Guardrail
 * @see GuardrailAssignment
 * @see CreateGuardrailRequest
 * @see UpdateGuardrailRequest
 */
public class GuardrailsApi internal constructor(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    /**
     * Lists all guardrails for the account.
     *
     * Calls the `/guardrails` endpoint to retrieve all guardrails configured
     * for the authenticated account.
     *
     * @return A list of [Guardrail] objects.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see Guardrail
     */
    public suspend fun list(): List<Guardrail> = getAndDecode("${config.baseUrl}/guardrails")

    /**
     * Creates a new guardrail.
     *
     * Calls the `/guardrails` endpoint with a POST request to create a new
     * guardrail with the specified configuration.
     *
     * @param request The guardrail creation request containing rules and settings.
     * @return The newly created [Guardrail].
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see CreateGuardrailRequest
     * @see Guardrail
     */
    public suspend fun create(request: CreateGuardrailRequest): Guardrail {
        val response = httpClient.post("${config.baseUrl}/guardrails") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    /**
     * Retrieves details for a specific guardrail.
     *
     * Calls the `/guardrails/{id}` endpoint to get information about a specific
     * guardrail identified by its ID.
     *
     * @param id The guardrail ID.
     * @return The [Guardrail] details.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the guardrail is not found.
     * @see Guardrail
     */
    public suspend fun get(id: String): Guardrail = getAndDecode("${config.baseUrl}/guardrails/$id")

    /**
     * Updates an existing guardrail.
     *
     * Calls the `/guardrails/{id}` endpoint with a PATCH request to update the
     * configuration of an existing guardrail.
     *
     * @param id The guardrail ID to update.
     * @param request The update request containing modified guardrail settings.
     * @return The updated [Guardrail].
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the guardrail is not found.
     * @see UpdateGuardrailRequest
     * @see Guardrail
     */
    public suspend fun update(id: String, request: UpdateGuardrailRequest): Guardrail {
        val response = httpClient.patch("${config.baseUrl}/guardrails/$id") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    /**
     * Deletes a guardrail.
     *
     * Calls the `/guardrails/{id}` endpoint with a DELETE request to permanently
     * remove the specified guardrail.
     *
     * @param id The guardrail ID to delete.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the guardrail is not found.
     */
    public suspend fun delete(id: String) {
        val response = httpClient.delete("${config.baseUrl}/guardrails/$id") {
            applyOpenRouterHeaders(config)
        }
        response.throwIfErrorStatus()
    }

    /**
     * Lists all assignments for a specific guardrail.
     *
     * Calls the `/guardrails/{id}/assignments` endpoint to retrieve all assignments
     * (models or keys) that are subject to the specified guardrail.
     *
     * @param id The guardrail ID.
     * @return A list of [GuardrailAssignment] objects.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the guardrail is not found.
     * @see GuardrailAssignment
     */
    public suspend fun listAssignments(id: String): List<GuardrailAssignment> = 
        getAndDecode("${config.baseUrl}/guardrails/$id/assignments")

    /**
     * Adds an assignment to a guardrail.
     *
     * Calls the `/guardrails/{id}/assignments` endpoint with a POST request to
     * assign the guardrail to a specific model or API key.
     *
     * @param id The guardrail ID.
     * @param request The assignment request specifying what to assign the guardrail to.
     * @return The newly created [GuardrailAssignment].
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails.
     * @see AddAssignmentRequest
     * @see GuardrailAssignment
     */
    public suspend fun addAssignment(id: String, request: AddAssignmentRequest): GuardrailAssignment {
        val response = httpClient.post("${config.baseUrl}/guardrails/$id/assignments") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    /**
     * Removes an assignment from a guardrail.
     *
     * Calls the `/guardrails/{id}/assignments/{assignmentId}` endpoint with a DELETE
     * request to remove the specified assignment from the guardrail.
     *
     * @param id The guardrail ID.
     * @param assignmentId The assignment ID to remove.
     * @throws dev.toliner.openrouter.error.OpenRouterException if the request fails or the assignment is not found.
     */
    public suspend fun removeAssignment(id: String, assignmentId: String) {
        val response = httpClient.delete("${config.baseUrl}/guardrails/$id/assignments/$assignmentId") {
            applyOpenRouterHeaders(config)
        }
        response.throwIfErrorStatus()
    }

    private suspend inline fun <reified T> getAndDecode(url: String): T {
        val response = httpClient.get(url) {
            applyOpenRouterHeaders(config)
        }
        return response.decodeBodyOrThrow()
    }
}
