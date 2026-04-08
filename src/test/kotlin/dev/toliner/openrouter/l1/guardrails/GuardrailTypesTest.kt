package dev.toliner.openrouter.l1.guardrails

import dev.toliner.openrouter.serialization.OpenRouterJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GuardrailTypesTest : FunSpec({
    test("Guardrail should deserialize with all fields") {
        val json = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "name": "Production Guardrail",
                "description": "Guardrail for production environment",
                "limit_usd": 100.0,
                "reset_interval": "monthly",
                "allowed_providers": ["openai", "anthropic"],
                "ignored_providers": ["azure"],
                "allowed_models": ["openai/gpt-4"],
                "enforce_zdr": false,
                "created_at": "2025-08-24T10:30:00Z",
                "updated_at": "2025-08-24T15:45:00Z"
            }
        """.trimIndent()

        val guardrail = OpenRouterJson.decodeFromString<Guardrail>(json)

        guardrail.id shouldBe "550e8400-e29b-41d4-a716-446655440000"
        guardrail.name shouldBe "Production Guardrail"
        guardrail.description shouldBe "Guardrail for production environment"
        guardrail.limitUsd shouldBe 100.0
        guardrail.resetInterval shouldBe "monthly"
        guardrail.allowedProviders shouldBe listOf("openai", "anthropic")
        guardrail.ignoredProviders shouldBe listOf("azure")
        guardrail.allowedModels shouldBe listOf("openai/gpt-4")
        guardrail.enforceZdr shouldBe false
        guardrail.createdAt shouldBe "2025-08-24T10:30:00Z"
        guardrail.updatedAt shouldBe "2025-08-24T15:45:00Z"
    }

    test("Guardrail should deserialize with optional fields absent") {
        val json = """
            {
                "id": "guardrail_xyz789",
                "name": "basic-policy",
                "created_at": "2025-08-24T10:30:00Z"
            }
        """.trimIndent()

        val guardrail = OpenRouterJson.decodeFromString<Guardrail>(json)

        guardrail.id shouldBe "guardrail_xyz789"
        guardrail.name shouldBe "basic-policy"
        guardrail.description shouldBe null
        guardrail.limitUsd shouldBe null
        guardrail.resetInterval shouldBe null
        guardrail.allowedProviders shouldBe null
        guardrail.updatedAt shouldBe null
    }

    test("Guardrail list response should deserialize") {
        val json = """
            [
                {
                    "id": "guardrail_1",
                    "name": "policy-1",
                    "created_at": "2025-01-01T00:00:00Z"
                },
                {
                    "id": "guardrail_2",
                    "name": "policy-2",
                    "created_at": "2025-01-02T00:00:00Z"
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
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "key_hash": "c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93",
                "guardrail_id": "550e8400-e29b-41d4-a716-446655440001",
                "key_name": "Production Key",
                "key_label": "prod-key",
                "assigned_by": "user_abc123",
                "created_at": "2025-08-24T10:30:00Z"
            }
        """.trimIndent()

        val assignment = OpenRouterJson.decodeFromString<GuardrailAssignment>(json)

        assignment.id shouldBe "550e8400-e29b-41d4-a716-446655440000"
        assignment.keyHash shouldBe "c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93"
        assignment.guardrailId shouldBe "550e8400-e29b-41d4-a716-446655440001"
        assignment.keyName shouldBe "Production Key"
        assignment.keyLabel shouldBe "prod-key"
        assignment.assignedBy shouldBe "user_abc123"
        assignment.createdAt shouldBe "2025-08-24T10:30:00Z"
    }

    test("GuardrailAssignment should deserialize with null assigned_by") {
        val json = """
            {
                "id": "assignment_1",
                "key_hash": "abc123",
                "guardrail_id": "guardrail_1",
                "key_name": "Key 1",
                "key_label": "key-1",
                "assigned_by": null,
                "created_at": "2025-08-24T10:30:00Z"
            }
        """.trimIndent()

        val assignment = OpenRouterJson.decodeFromString<GuardrailAssignment>(json)
        assignment.assignedBy shouldBe null
    }

    test("CreateGuardrailRequest should serialize with all fields") {
        val request = CreateGuardrailRequest(
            name = "my-policy",
            description = "Custom policy",
            limitUsd = 50.0,
            resetInterval = "monthly",
            allowedProviders = listOf("openai", "anthropic"),
            enforceZdr = false
        )

        val json = OpenRouterJson.encodeToString(CreateGuardrailRequest.serializer(), request)
        val decoded = OpenRouterJson.decodeFromString<CreateGuardrailRequest>(json)

        decoded shouldBe request
    }

    test("CreateGuardrailRequest should serialize with minimal fields") {
        val request = CreateGuardrailRequest(name = "minimal-policy")

        val json = OpenRouterJson.encodeToString(CreateGuardrailRequest.serializer(), request)

        json shouldBe """{"name":"minimal-policy"}"""
    }

    test("UpdateGuardrailRequest should serialize with all fields") {
        val request = UpdateGuardrailRequest(
            name = "updated-policy",
            description = "Updated description",
            limitUsd = 75.0,
            resetInterval = "weekly"
        )

        val json = OpenRouterJson.encodeToString(UpdateGuardrailRequest.serializer(), request)
        val decoded = OpenRouterJson.decodeFromString<UpdateGuardrailRequest>(json)

        decoded shouldBe request
    }

    test("UpdateGuardrailRequest should serialize with partial fields") {
        val request = UpdateGuardrailRequest(name = "new-name")

        val json = OpenRouterJson.encodeToString(UpdateGuardrailRequest.serializer(), request)

        json shouldBe """{"name":"new-name"}"""
    }

    test("UpdateGuardrailRequest should serialize with only description") {
        val request = UpdateGuardrailRequest(description = "New description")

        val json = OpenRouterJson.encodeToString(UpdateGuardrailRequest.serializer(), request)

        json shouldBe """{"description":"New description"}"""
    }

    test("AddAssignmentRequest should serialize") {
        val request = AddAssignmentRequest(
            keyHashes = listOf("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93")
        )

        val json = OpenRouterJson.encodeToString(AddAssignmentRequest.serializer(), request)

        json shouldBe """{"key_hashes":["c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93"]}"""
    }

    test("AddAssignmentRequest should serialize with multiple keys") {
        val request = AddAssignmentRequest(
            keyHashes = listOf("hash1", "hash2", "hash3")
        )

        val json = OpenRouterJson.encodeToString(AddAssignmentRequest.serializer(), request)

        json shouldBe """{"key_hashes":["hash1","hash2","hash3"]}"""
    }
})
