# After Action Report: Fix l1 Models to Match OpenRouter OpenAPI Schema

## Objective
Fix all l1 (low-level API data model) Kotlin classes to accurately match the OpenRouter OpenAPI schema at `.local/openrouter-openapi.json`. Primary issue was `Pricing` having incorrect required/optional fields.

## Changes Made

### Source Files Modified

| File | Key Changes |
|------|------------|
| `l1/models/Model.kt` | Added `canonicalSlug`, `huggingFaceId`, `created`, `supportedParameters`, `defaultParameters`, `knowledgeCutoff`, `expirationDate`, `links`. Made `perRequestLimits` required-nullable. New types: `DefaultParameters`, `ModelLinks`. |
| `l1/models/Model.kt` (Pricing) | Added 10 new optional fields: `imageToken`, `imageOutput`, `audio`, `audioOutput`, `inputAudioCache`, `webSearch`, `internalReasoning`, `inputCacheRead`, `inputCacheWrite`, `discount`. Made `request`/`image` optional. |
| `l1/models/Model.kt` (Architecture) | Made `tokenizer` optional. Added `inputModalities`, `outputModalities`. |
| `l1/models/Model.kt` (TopProvider) | Made `contextLength` optional. |
| `l1/models/Model.kt` (PerRequestLimits) | Made `promptTokens`/`completionTokens` non-null required. |
| `l1/chat/ChatCompletionResponse.kt` | Added `systemFingerprint`, `serviceTier`. Made `finishReason` required-nullable. Added `logprobs`, re-added `error` to `Choice`. |
| `l1/chat/Usage.kt` | Added `CompletionTokensDetails`, `PromptTokensDetails`. |
| `l1/chat/ChatCompletionChunk.kt` | Added `systemFingerprint`, `serviceTier`, chunk-level `error`. Added `reasoning`, `refusal` to `Delta`. Removed `error` from `ChunkChoice`. |
| `l1/account/Activity.kt` | Renamed `DailyActivity` → `ActivityItem` with completely new field set. |
| `l1/guardrails/Guardrail.kt` | Removed `config`. Changed timestamps to ISO 8601 strings. Added guardrail-specific fields. |
| `l1/guardrails/Create/UpdateGuardrailRequest.kt` | Removed `config`. Added guardrail-specific fields. |
| `l1/guardrails/GuardrailAssignment.kt` | Replaced `targetType`/`targetId` with `keyHash`, `keyName`, `keyLabel`, `assignedBy`. |
| `l1/guardrails/AddAssignmentRequest.kt` | Replaced `targetType`/`targetId` with `keyHashes: List<String>`. |
| `streaming/SseParser.kt` | Updated to use `chunk.error` instead of `ChunkChoice.error`. |

### Test Files Modified

| File | Key Changes |
|------|------------|
| `l1/models/ModelTypesTest.kt` | Fully rewritten with new required fields in JSON fixtures. |
| `l1/guardrails/GuardrailTypesTest.kt` | Fully rewritten with new schema. |
| `l1/account/AccountTypesTest.kt` | Updated `DailyActivity` → `ActivityItem`, new JSON fixtures with all 11 fields. |
| `client/GuardrailsApiTest.kt` | Fully rewritten: new type constructors, JSON fixtures, assertions. |
| `streaming/SseParserTest.kt` | Moved error to chunk level in mid-stream error test. |
| `client/OpenRouterClientTest.kt` | Updated model JSON fixtures with new required fields. |

### Files Audited, No Changes Needed
- `ProviderPreferences.kt`, `MaxPrice.kt`, Keys (all), Embeddings (all), `Generation.kt`, `Provider.kt`, `AuthKey.kt`

## Known Deferred Work

The Guardrails **client API** (`GuardrailsApi.kt`) has structural mismatches with the OpenAPI spec that were NOT addressed:
- API paths differ: spec uses `/guardrails/{id}/assignments/keys` vs client `/guardrails/{id}/assignments`
- Responses are wrapped in the spec: `ListGuardrailsResponse { data, total_count }` vs client `List<Guardrail>`
- `addAssignment` POST returns `BulkAssignKeysResponse { assigned_count }` in spec, not `GuardrailAssignment`
- Remove is POST to `/remove` endpoint in spec, not DELETE

These require a client-layer refactor and are outside the scope of the l1 model fix task.

## Build Status
- `./gradlew build`: **PASS** (254 tests, 0 failures)
