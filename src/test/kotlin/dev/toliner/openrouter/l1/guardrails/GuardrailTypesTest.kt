package dev.toliner.openrouter.l1.guardrails

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class GuardrailTypesTest : FunSpec({
    test("Guardrail should deserialize with all fields") {
        val json = """
            {
                "id": "guardrail_abc123",
                "name": "content-policy",
                "description": "Content moderation policy",
                "config": {
                    "blocked_topics": ["violence", "hate"],
                    "severity": "strict"
                },
                "created_at": 1679500800,
                "updated_at": 1679587200
            }
        """.trimIndent()

        val guardrail = OpenRouterJson.decodeFromString<Guardrail>(json)

        guardrail.id shouldBe "guardrail_abc123"
        guardrail.name shouldBe "content-policy"
        guardrail.description shouldBe "Content moderation policy"
        guardrail.config shouldBe JsonObject(
            mapOf(
                "blocked_topics" to kotlinx.serialization.json.JsonArray(
                    listOf(JsonPrimitive("violence"), JsonPrimitive("hate"))
                ),
                "severity" to JsonPrimitive("strict")
            )
        )
        guardrail.createdAt shouldBe 1679500800
        guardrail.updatedAt shouldBe 1679587200
    }

    test("Guardrail should deserialize with optional fields absent") {
        val json = """
            {
                "id": "guardrail_xyz789",
                "name": "basic-policy",
                "created_at": 1679500800
            }
        """.trimIndent()

        val guardrail = OpenRouterJson.decodeFromString<Guardrail>(json)

        guardrail.id shouldBe "guardrail_xyz789"
        guardrail.name shouldBe "basic-policy"
        guardrail.description shouldBe null
        guardrail.config shouldBe null
        guardrail.updatedAt shouldBe null
    }

    test("Guardrail list response should deserialize") {
        val json = """
            [
                {
                    "id": "guardrail_1",
                    "name": "policy-1",
                    "created_at": 1679500800
                },
                {
                    "id": "guardrail_2",
                    "name": "policy-2",
                    "created_at": 1679587200
                }
            ]
        """.trimIndent()

        val guardrails = OpenRouterJson.decodeFromString<List<Guardrail>>(json)

        guardrails.size shouldBe 2
        guardrails[0].id shouldBe "guardrail_1"
        guardrails[0].name shouldBe "policy-1"
        guardrails[1].id shouldBe "guardrail_2"
        guardrails[1].name shouldBe "policy-2"
    }

    test("GuardrailAssignment should deserialize with all fields") {
        val json = """
            {
                "id": "assignment_abc123",
                "guardrail_id": "guardrail_xyz789",
                "target_type": "api_key",
                "target_id": "sk_live_key123",
                "created_at": 1679500800
            }
        """.trimIndent()

        val assignment = OpenRouterJson.decodeFromString<GuardrailAssignment>(json)

        assignment.id shouldBe "assignment_abc123"
        assignment.guardrailId shouldBe "guardrail_xyz789"
        assignment.targetType shouldBe "api_key"
        assignment.targetId shouldBe "sk_live_key123"
        assignment.createdAt shouldBe 1679500800
    }

    test("GuardrailAssignment list response should deserialize") {
        val json = """
            [
                {
                    "id": "assignment_1",
                    "guardrail_id": "guardrail_1",
                    "target_type": "api_key",
                    "target_id": "sk_live_key1",
                    "created_at": 1679500800
                },
                {
                    "id": "assignment_2",
                    "guardrail_id": "guardrail_1",
                    "target_type": "model",
                    "target_id": "gpt-4",
                    "created_at": 1679587200
                }
            ]
        """.trimIndent()

        val assignments = OpenRouterJson.decodeFromString<List<GuardrailAssignment>>(json)

        assignments.size shouldBe 2
        assignments[0].id shouldBe "assignment_1"
        assignments[0].targetType shouldBe "api_key"
        assignments[1].id shouldBe "assignment_2"
        assignments[1].targetType shouldBe "model"
    }

    test("CreateGuardrailRequest should serialize with all fields") {
        val request = CreateGuardrailRequest(
            name = "my-policy",
            description = "Custom policy",
            config = JsonObject(mapOf("key" to JsonPrimitive("value")))
        )

        val json = OpenRouterJson.encodeToString(CreateGuardrailRequest.serializer(), request)

        json shouldBe """{"name":"my-policy","description":"Custom policy","config":{"key":"value"}}"""
    }

    test("CreateGuardrailRequest should serialize with minimal fields") {
        val request = CreateGuardrailRequest(
            name = "minimal-policy",
            description = null,
            config = null
        )

        val json = OpenRouterJson.encodeToString(CreateGuardrailRequest.serializer(), request)

        json shouldBe """{"name":"minimal-policy"}"""
    }

    test("UpdateGuardrailRequest should serialize with all fields") {
        val request = UpdateGuardrailRequest(
            name = "updated-policy",
            description = "Updated description",
            config = JsonObject(mapOf("updated" to JsonPrimitive("true")))
        )

        val json = OpenRouterJson.encodeToString(UpdateGuardrailRequest.serializer(), request)

        json shouldBe """{"name":"updated-policy","description":"Updated description","config":{"updated":"true"}}"""
    }

    test("UpdateGuardrailRequest should serialize with partial fields") {
        val request = UpdateGuardrailRequest(
            name = "new-name",
            description = null,
            config = null
        )

        val json = OpenRouterJson.encodeToString(UpdateGuardrailRequest.serializer(), request)

        json shouldBe """{"name":"new-name"}"""
    }

    test("UpdateGuardrailRequest should serialize with only description") {
        val request = UpdateGuardrailRequest(
            name = null,
            description = "New description",
            config = null
        )

        val json = OpenRouterJson.encodeToString(UpdateGuardrailRequest.serializer(), request)

        json shouldBe """{"description":"New description"}"""
    }

    test("AddAssignmentRequest should serialize with all fields") {
        val request = AddAssignmentRequest(
            targetType = "api_key",
            targetId = "sk_live_key123"
        )

        val json = OpenRouterJson.encodeToString(AddAssignmentRequest.serializer(), request)

        json shouldBe """{"target_type":"api_key","target_id":"sk_live_key123"}"""
    }

    test("AddAssignmentRequest should serialize for model target") {
        val request = AddAssignmentRequest(
            targetType = "model",
            targetId = "gpt-4-turbo"
        )

        val json = OpenRouterJson.encodeToString(AddAssignmentRequest.serializer(), request)

        json shouldBe """{"target_type":"model","target_id":"gpt-4-turbo"}"""
    }
})
