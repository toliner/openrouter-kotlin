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

class GuardrailsApi(
    private val httpClient: HttpClient,
    private val config: OpenRouterConfig
) {
    suspend fun list(): List<Guardrail> = getAndDecode("${config.baseUrl}/guardrails")

    suspend fun create(request: CreateGuardrailRequest): Guardrail {
        val response = httpClient.post("${config.baseUrl}/guardrails") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    suspend fun get(id: String): Guardrail = getAndDecode("${config.baseUrl}/guardrails/$id")

    suspend fun update(id: String, request: UpdateGuardrailRequest): Guardrail {
        val response = httpClient.patch("${config.baseUrl}/guardrails/$id") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    suspend fun delete(id: String) {
        val response = httpClient.delete("${config.baseUrl}/guardrails/$id") {
            applyOpenRouterHeaders(config)
        }
        response.throwIfErrorStatus()
    }

    suspend fun listAssignments(id: String): List<GuardrailAssignment> = 
        getAndDecode("${config.baseUrl}/guardrails/$id/assignments")

    suspend fun addAssignment(id: String, request: AddAssignmentRequest): GuardrailAssignment {
        val response = httpClient.post("${config.baseUrl}/guardrails/$id/assignments") {
            applyOpenRouterHeaders(config)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.decodeBodyOrThrow()
    }

    suspend fun removeAssignment(id: String, assignmentId: String) {
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
