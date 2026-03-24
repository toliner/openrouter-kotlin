## Task 1: Build Setup

### Dependencies Successfully Added
- Ktor 3.4.1 (client-core, client-cio, content-negotiation, serialization-kotlinx-json)
- kotlinx.serialization 1.10.0
- Kotest 6.1.0 (runner-junit5, assertions-core, property)
- ktor-client-mock for testing

### Build Configuration
- kotlin("plugin.serialization") is REQUIRED for @Serializable annotations to work
- useJUnitPlatform() is REQUIRED for Kotest tests to execute
- Gradle 9.2.0 + Kotlin 2.3.10 + JVM toolchain 25 works correctly

### Test Setup
- Minimal Kotest FunSpec test validates test runner configuration
- Pattern: `class XTest : FunSpec({ test("name") { ... } })`


## Task 2: Serialization Components

### OpenRouterJson Configuration
- Created centralized Json config with:
  - `ignoreUnknownKeys = true` for API version tolerance
  - `explicitNulls = false` to omit null fields in output
  - `coerceInputValues = true` for type mismatch handling
  - `encodeDefaults = false` to minimize payload size
- Pattern: `val OpenRouterJson = Json { ... }` as top-level val

### Union Type Serialization Pattern
Implemented 3 union types using custom KSerializer:
1. **StringOrArray**: `"text"` OR `["a","b"]`
2. **Content**: `"text"` OR `[{type:"text",text:"..."}]`
3. **ToolChoice**: `"auto"` OR `{type:"function",function:{name:"..."}}`

#### Implementation Pattern
```kotlin
@Serializable(with = XSerializer::class)
sealed interface X {
    data class Variant1(...) : X
    data class Variant2(...) : X
}

object XSerializer : KSerializer<X> {
    override fun deserialize(decoder: Decoder): X {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        return when {
            element is JsonPrimitive -> Variant1(...)
            else -> decoder.json.decodeFromJsonElement(Variant2.serializer(), element)
        }
    }
    
    override fun serialize(encoder: Encoder, value: X) {
        require(encoder is JsonEncoder)
        when (value) {
            is Variant1 -> encoder.encodeString(...)
            is Variant2 -> encoder.encodeSerializableValue(Variant2.serializer(), value)
        }
    }
}
```

### Key Learnings
- **JsonDecoder/JsonEncoder** required for union type pattern (check with `require()`)
- **@SerialName** annotations needed for all JSON field mappings (no JsonNamingStrategy)
- **Property-based testing** with `checkAll(Arb.X)` validates round-trip serialization
- **Local data classes** in serializer can model intermediate JSON structures
- **Sealed interfaces** preferred over sealed classes for union types

### Test Coverage
- Round-trip tests for each variant (serialize → deserialize == original)
- Property-based tests using Kotest Arb generators
- Explicit JSON string matching for format validation
- All tests pass with Gradle build

### TDD Process
- RED phase: Write failing test (compilation errors expected)
- GREEN phase: Implement minimal code to pass tests
- Tests confirmed behavior before moving to next serializer


## Task 3: Error Exception Hierarchy

### Pattern
- `sealed class OpenRouterException` with one subclass per HTTP error family used by OpenRouter
- `errorFromStatus()` maps HTTP status codes to typed exceptions
- `OpenRouterException.isRetryable` is an extension property, not business logic

### Key Learnings
- `ErrorBody` and `ErrorResponse` should both be `@Serializable`
- `provider_error` needs `@SerialName("provider_error")`
- Known retryable statuses are limited to transport/rate-limit style failures; everything else stays false unless it is `UnknownError` with a retryable code
- `kotlin-ls` was unavailable in this environment, so build verification was used instead of LSP diagnostics

## Task 4: Chat Completions Types

### Message Sealed Class Pattern
- **Custom KSerializer required** for role-based discrimination (not type-based)
- OpenRouter API uses `role` field (not a separate discriminator) to identify message types
- Pattern: Sealed class with custom serializer that reads `role` field first, then deserializes specific variant
- Message variants: System, User (with Content union type), Assistant (with optional tool_calls), Tool

### Message Serializer Implementation
- Must use `JsonDecoder`/`JsonEncoder` and check with `require()`
- Read full JSON object, extract `role` field, then deserialize to specific variant
- When serializing nested union types (Content inside Message.User), **avoid nested encoder contexts**
- Solution: Manually unwrap Content variants to avoid `encodeToJsonElement(Content.serializer(), ...)` which creates nested encoder
- Directly build JSON with `when (content) { is Text -> JsonPrimitive(...) is Parts -> ... }`

### Type Definitions Completed
1. **Message.kt**: Sealed class with System, User, Assistant, Tool + ToolCall, FunctionCall (response types)
2. **Tool.kt**: FunctionTool, FunctionDefinition (request types) - use `JsonElement` for flexible parameters
3. **ResponseFormat.kt**: Simple data class with type field
4. **ProviderPreferences.kt**: All provider preference fields with @SerialName
5. **Usage.kt**: Token usage tracking
6. **Trace.kt**: Distributed tracing support
7. **ChatCompletionRequest.kt**: ALL fields including model, messages, temperature, max_tokens, top_p, top_k, frequency_penalty, presence_penalty, repetition_penalty, seed, stop, stream, tools, tool_choice, response_format, provider, trace, transforms, route, models
8. **ChatCompletionResponse.kt**: Response with choices + usage
9. **ChatCompletionChunk.kt**: Streaming chunk with Delta (role, content, tool_calls may be incremental)

### JsonElement for Flexible Parameters
- Use `JsonElement` type for function parameters (instead of `Map<String, Any>`)
- Build with `buildJsonObject { put("key", JsonPrimitive("value")) }`
- Avoids internal serializer access issues

### Test Coverage
- Message serialization round-trip for all variants (System, User text/multipart, Assistant, Tool)
- Tool types (FunctionTool, ToolCall, FunctionCall)
- Support types (ResponseFormat, ProviderPreferences, Usage, Trace)
- ChatCompletionRequest: minimal (model + messages) + full (all optional fields)
- ChatCompletionResponse: normal response + tool_calls variant
- ChatCompletionChunk: delta content, delta tool_calls, finish_reason, usage

### Key Learnings
- **Polymorphic-by-field pattern**: When API uses a data field (not type discriminator) for polymorphism, use custom serializer
- **Nested encoder issues**: Calling `encoder.json.encodeToJsonElement(CustomSerializer.serializer(), value)` fails if CustomSerializer expects JsonEncoder context
- **Solution**: Manually unwrap union types when building JSON in parent serializer
- **JsonElement**: Preferred for arbitrary JSON structures (like function parameters) - more flexible than Map

## Task 5: SSE Streaming Parser

### Parsing Strategy
- `Flow<ServerSentEvent>.toChatCompletionChunks(json)` should stop on `data: [DONE]` via `takeWhile` before parse.
- Use `event.data?.trim()` as first gate: null/blank payloads are skipped.
- Comment-like payloads (`:` prefix) should be ignored in parser logic to avoid JSON decode failures.

### Debug/Usage/Error Chunk Handling
- Debug-only chunks (`choices: []` + `debug`) can be skipped safely by pre-parsing to `JsonElement` and checking object shape before decoding into `ChatCompletionChunk`.
- Usage-only chunks (`choices: []` + `usage`) should still be emitted because they carry final token accounting.
- Mid-stream error detection works by checking decoded `ChunkChoice` entries for `finish_reason == "error" && error != null`, then throwing `OpenRouterException.StreamError`.

### Type Model Adjustment
- Both `ChunkChoice` and `Choice` need optional `error: ErrorBody?` to support stream and non-stream in-band error detection.
- Keeping `error` optional preserves backward compatibility with normal success payloads.

### Test Approach
- Hand-crafted `flowOf(ServerSentEvent(...))` tests are stable for SSE transform logic and avoid KTOR-7910 timing behavior.
- Include both debug behaviors in tests: skip debug-only chunk, but emit normal chunk even when unknown `debug` field is present (`ignoreUnknownKeys=true`).

## Task 6: Models Type + Endpoints

### Type Definitions Completed
1. **Model.kt**: Core model type with nested data classes (Pricing, Architecture, TopProvider, PerRequestLimits)
2. **ModelList.kt**: Simple wrapper with `data: List<Model>`
3. **ModelsCount.kt**: Model count response (`count: Int`)
4. **ModelEndpoint.kt**: Endpoint info with URL and headers (using `JsonElement` for flexible headers)
5. **ZdrEndpoint.kt**: ZDR impact preview type
6. **EmbeddingModel.kt**: Embedding model info (reuses `Pricing` from Model.kt)

### Critical Finding: Pricing Fields Are Strings
OpenRouter API returns pricing values as **string-typed numbers**, not doubles:
- Example: `"prompt": "0.0000005"`, `"completion": "0.0000015"`
- Rationale: Avoids floating-point precision issues in financial calculations
- Pattern: Define as `String` in Kotlin, convert to `BigDecimal` or `Double` only when needed

### Nested Data Classes Pattern
Structured complex types using separate data classes:
- `Pricing`: Reusable across Model and EmbeddingModel
- `Architecture`: Modality, tokenizer, instruct_type
- `TopProvider`: Context length, max completion tokens, moderation status
- `PerRequestLimits`: Optional token limits (nullable fields)

### All Fields Use @SerialName
Every snake_case JSON field has explicit @SerialName annotation:
- `context_length` → `@SerialName("context_length") val contextLength`
- `top_provider` → `@SerialName("top_provider") val topProvider`
- `per_request_limits` → `@SerialName("per_request_limits") val perRequestLimits`
- No JsonNamingStrategy used (explicit annotations only)

### Test Coverage
- Model: Full deserialization with all nested types + unknown field tolerance
- ModelList: Data array parsing + top-level unknown fields ignored
- ModelsCount, ModelEndpoint, ZdrEndpoint, EmbeddingModel: Basic deserialization
- All tests validate `ignoreUnknownKeys = true` behavior from OpenRouterJson

### TDD Process
RED → GREEN cycle followed:
1. Write failing test with expected JSON structure
2. Implement minimal type definition with @Serializable + @SerialName
3. Verify compilation + test passes
4. Evidence files created: task-6-model-types.txt, task-6-unknown-fields.txt

## Task 7: Embeddings Types

### Learning
- EmbeddingRequest uses StringOrArray for input so single-string and array payloads share one type.
- EmbeddingResponse reuses dev.toliner.openrouter.l1.chat.Usage; the embeddings API shares the same usage shape as chat.
- Keeping @SerialName on every JSON field preserves the explicit mapping style used across the client.
- Test coverage should include both serialization forms for input plus response deserialization with usage.

### Follow-up
- `EmbeddingData` must expose the JSON field as backticked ``object`` to match the API payload exactly.
- Task 7 verification passed with `./gradlew test --tests "dev.toliner.openrouter.l1.embeddings.*"`.

## Task 5: SSE Streaming Parser (追加学習)

### Raw SSE Line耐性
- 変換対象が `ServerSentEvent.data` でも、テストでは `data: ...` プレフィックス付き文字列を直接流し込むケースがあるため、正規化レイヤーを先に入れると堅牢になる。
- 正規化は `trim()` → `data:` プレフィックス除去 → 空文字判定の順にすると、`data: ` (空data行) を安全にスキップできる。
- `[DONE]` 判定も正規化後に行うことで、`data: [DONE]` と `[DONE]` の両方を同一ロジックで扱える。

## Task 8: OpenRouterClient + API統合

### Client構築パターン
- `OpenRouterClient(engine, config)` で `HttpClient` を構築し、`ContentNegotiation(json(OpenRouterJson))` + `expectSuccess = false` を固定すると、HTTPエラーをコールサイト側で一貫処理しやすい。
- 共通ヘッダー付与は `HttpRequestBuilder` 拡張 (`Authorization`, 任意で `HTTP-Referer`, `X-Title`) にまとめると、Chat/Models/Embeddings 間で重複を避けられる。

### エラーハンドリング統合
- HTTPステータスは `throwIfErrorStatus()` で先に判定し、`errorFromStatus(status, body, retryAfter)` へ集約する。
- エラーボディは `{"error": {...}}` と `{...}` の両形を受けるため、`ErrorResponse` → `ErrorBody` の順でフォールバック復元すると耐性が上がる。
- Chat complete はデコード後に `checkInBandError()` を必ず通すことで、HTTP 200 の in-band failure を `OpenRouterException.InBandError` に正規化できる。

### Streaming統合
- `stream()` は送信時に `request.copy(stream = true)` を強制し、呼び出し側が `stream` 指定を忘れても一貫した動作になる。
- 受信SSEは `data:` 行を `ServerSentEvent` に変換して `toChatCompletionChunks(OpenRouterJson)` に流すと、既存の `[DONE]` 処理・mid-stream error処理を再利用できる。

## Task 9: Generation + Account Types and Endpoints

### Type Definitions Completed
1. **Generation.kt**: Generation metadata type with GenerationUsage nested class
2. **Credits.kt**: Account credits balance (total_credits, total_usage)
3. **KeyInfo.kt**: API key information with RateLimit nested class
4. **Activity.kt**: Daily usage activity with DailyActivity nested class

### API Endpoints Added
1. **GenerationApi**: `get(id)` returns Generation metadata for a request ID
2. **AccountApi**: `credits()`, `keyInfo()`, `activity()` for account management
3. **OpenRouterClient**: Integrated `generation` and `account` properties

### Critical Finding: Generation Cost is Double, Not String
- Initial implementation used `String` for `GenerationUsage.cost` (following Task 6's Pricing pattern)
- OpenRouter API returns `cost` as **numeric value** (e.g., `0.0075`), not string
- **Root cause**: Task 6's Pricing fields (`"prompt": "0.0000005"`) were strings to preserve precision
- **Generation API difference**: `usage.cost` field is a computed **transaction total**, not a unit rate
- **Resolution**: Changed `GenerationUsage.cost` from `String` to `Double`

### Pattern Consistency
- All GET endpoints use `getAndDecode<T>(url)` pattern from ModelsApi/EmbeddingsApi
- GenerationApi uses `parameter("id", id)` for query string construction
- AccountApi endpoints require Management API Key (documented in types, not enforced in client)
- All nested types use `@SerialName` annotations for snake_case JSON fields

### Test Coverage
- Generation types: Full deserialization with all fields + optional fields (native_tokens_*)
- Account types: Credits, KeyInfo (with null limit), Activity (empty + populated data arrays)
- Nested types: GenerationUsage, RateLimit, DailyActivity
- All tests validate `ignoreUnknownKeys = true` behavior from OpenRouterJson

### TDD Process
- RED phase: Tests written first with expected JSON structures
- GREEN phase: Types implemented with @Serializable + @SerialName
- Fixed cost type mismatch: String → Double after test failure analysis
- Full test suite passes after adjustment

### Integration
- `OpenRouterClient.generation` and `OpenRouterClient.account` properties added
- Consistent with existing `chat`, `models`, `embeddings` pattern
- All API classes share `httpClient` and `config` from client constructor

## 2026-03-22 17:36:49 - Task 10: Key Management CRUD

### Pattern Applied
- **Full CRUD operations**: list (GET), create (POST), get (GET with path param), update (PATCH), delete (DELETE 204)
- **DELETE 204 No Content handling**: Use `response.throwIfErrorStatus()` without body decode - critical for endpoints that return no content
- **PATCH partial update**: Send only fields that change (nullable fields excluded from JSON when null)
- **Content-Type requirement**: POST/PATCH must set `contentType(ContentType.Application.Json)` before `setBody()` to avoid serialization errors

### Implementation Details
- **ApiKey type**: Complex nested structure with RateLimit, all snake_case fields use @SerialName
- **CreateKeyRequest**: All optional fields except `name` (nullable with default = null)
- **UpdateKeyRequest**: ALL fields optional (true partial update pattern)
- **KeysApi integration**: Added to OpenRouterClient as `val keys: KeysApi`

### Test Coverage
- **KeyTypesTest**: 8 tests covering ApiKey/CreateKeyRequest/UpdateKeyRequest serialization/deserialization
  - Full fields, optional fields, list responses, partial serialization
- **KeysApiTest**: 6 tests covering all 5 CRUD operations
  - HTTP method validation, URL path validation, header validation
  - Request body JSON validation for POST/PATCH
  - PATCH partial update verification (only changed fields sent)
  - DELETE 204 No Content handling (no exception thrown)

### Gotchas Encountered
1. **Content-Type header in tests**: Must use `shouldStartWith(ContentType.Application.Json.toString())` not `shouldBe` because Ktor adds charset parameter
2. **contentType() required**: Without it, Ktor throws "Fail to prepare request body" error - not caught by MockEngine validation
3. **DELETE 204 pattern**: No body to decode, just `throwIfErrorStatus()` - different from other operations

### Key Learnings
- PATCH endpoints with partial updates require nullable fields but should NOT send null values - use JSON serialization defaults
- DELETE 204 is simpler than other operations - just validate status, no body processing
- Test validators must check actual HTTP behavior (Content-Type with charset) not ideal JSON types
- contentType() call is mandatory for POST/PATCH even though ContentNegotiation plugin is installed

### Files Created
- src/main/kotlin/dev/toliner/openrouter/l1/keys/ApiKey.kt
- src/main/kotlin/dev/toliner/openrouter/l1/keys/CreateKeyRequest.kt
- src/main/kotlin/dev/toliner/openrouter/l1/keys/UpdateKeyRequest.kt
- src/main/kotlin/dev/toliner/openrouter/client/KeysApi.kt
- src/test/kotlin/dev/toliner/openrouter/l1/keys/KeyTypesTest.kt
- src/test/kotlin/dev/toliner/openrouter/client/KeysApiTest.kt

### Files Modified
- src/main/kotlin/dev/toliner/openrouter/client/OpenRouterClient.kt (added keys property)

### Evidence
- .sisyphus/evidence/task-10-keys-crud.txt: Full test suite output showing all 14 keys tests passing (8 KeyTypesTest + 6 KeysApiTest)

## [2026-03-22 08:43] Task 11: Guardrails CRUD + Assignments

### Pattern Applied
- **8 endpoints total**: 5 CRUD operations + 3 assignment operations
- **Nested path pattern**: `/guardrails/{id}/assignments` and `/guardrails/{id}/assignments/{assignmentId}`
- **Two DELETE 204 operations**: delete guardrail, remove assignment
- Followed Task 10 KeysApi pattern exactly for CRUD operations

### Type Definitions
- **Guardrail**: id, name, description?, config (JsonObject?), created_at, updated_at?
- **GuardrailAssignment**: id, guardrail_id, target_type, target_id, created_at
- **CreateGuardrailRequest**: name, description?, config?
- **UpdateGuardrailRequest**: name?, description?, config? (all optional for partial PATCH)
- **AddAssignmentRequest**: target_type, target_id

### Test Coverage
- **GuardrailTypesTest**: 12 tests covering all type serialization/deserialization
  - Guardrail with all/minimal fields
  - GuardrailAssignment with all fields
  - Request types with all/partial fields
  - Verified partial PATCH omits null fields from JSON
- **GuardrailsApiTest**: 9 tests covering all 8 endpoints + partial update variant
  - All HTTP methods validated: GET, POST, PATCH, DELETE
  - All URL paths validated including nested /assignments paths
  - Request body JSON structure validated
  - DELETE 204 No Content handling for both delete operations

### Integration
- Added `val guardrails: GuardrailsApi` to OpenRouterClient
- Consistent with existing API structure (chat, models, embeddings, generation, account, keys)

### Key Learnings
- **JsonObject for flexible config**: Used `kotlinx.serialization.json.JsonObject` for guardrail config field to allow flexible schema without type constraints
- **Nested path pattern**: Assignment endpoints use `/guardrails/{id}/assignments` base path - reusable pattern for future nested resources
- **Two DELETE operations in one API**: Both `delete(id)` and `removeAssignment(id, assignmentId)` use same `response.throwIfErrorStatus()` pattern
- **Assignment target types**: "api_key" and "model" are primary target types for guardrail assignments

### TDD Discipline
- **RED phase first**: Wrote all tests before implementation (21 tests total)
- **GREEN phase**: Implemented types → API → client integration
- **Result**: All tests passed on first run, no refactoring needed
- **No regressions**: Full test suite (all prior tasks) still passes

### Test Evidence
- Evidence saved to `.sisyphus/evidence/task-11-guardrails.txt`
- 21 tests total: 12 type tests + 9 API tests
- All passed with 0 failures, 0 errors

### Commit Readiness
- ✅ All implementation files created
- ✅ All test files created
- ✅ Tests pass
- ✅ No regressions
- ✅ Ready for commit: `feat(l1/guardrails): add guardrails CRUD and assignments`

## [2026-03-22] Task 12: Providers + OAuth

### Pattern Applied
- 1 GET endpoint (providers list with wrapper response)
- 2 POST endpoints (OAuth PKCE flow: createAuthCode for authorization URL, exchangeCode for API key)
- OAuth PKCE flow types: request/response pairs with optional PKCE parameters

### Test Coverage
- ProviderTypesTest: Provider type deserialization with optional fields
- OAuthTypesTest: OAuth request/response serialization including CodeChallengeMethod enum
- ProvidersApiTest: MockEngine validation for GET /providers with data wrapper
- AuthApiTest: MockEngine tests for both POST endpoints (createAuthCode and exchangeCode)

### Integration
- Added providers and auth properties to OpenRouterClient
- ProvidersApi wraps response in ProvidersResponse to handle {data: [...]} structure
- AuthApi provides createAuthCode() and exchangeCode() methods

### Key Learnings
- Providers endpoint returns {data: [Provider]} wrapper (unlike guardrails which returns [Guardrail] directly)
- OAuth PKCE flow has two steps: 
  1. POST /auth/keys/code (request with callback_url, optional PKCE params) → {data: {authorization_url}}
  2. POST /auth/keys (request with code, optional code_verifier) → {key, user_id}
- CodeChallengeMethod enum uses @SerialName for "S256" and "plain" values
- All OAuth parameters (code_challenge, code_verifier, code_challenge_method, limit) are optional
- AuthCodeResponse uses nested data structure: {data: {authorization_url}}
- AuthKeyResponse is flat: {key, user_id}

### Type Definitions
**Provider**: name, slug, privacy_policy_url, terms_of_service_url, status_page_url (all URLs optional)
**OAuth types**:
- AuthCodeRequest: callback_url, code_challenge?, code_challenge_method?, limit?
- AuthCodeResponse: {data: {authorization_url}}
- AuthKeyRequest: code, code_verifier?, code_challenge_method?
- AuthKeyResponse: key, user_id?
- CodeChallengeMethod: S256 | plain

### Evidence
All tests pass - saved to .sisyphus/evidence/task-12-providers-oauth.txt

## [2026-03-24 08:49:48] Task 12: Providers + OAuth (Implementation Verification)

### Context
Task 12 implementation was already complete from a previous session. This session verified all components and ran full test suite to confirm correctness.

### Verification Results
- ✅ RED phase: All 4 test files exist and comprehensive
  - ProviderTypesTest: 2 tests (full/minimal fields)
  - OAuthTypesTest: 8 tests (all 4 types + enum + serialization variants)
  - ProvidersApiTest: 1 test (MockEngine GET validation)
  - AuthApiTest: 4 tests (both POST endpoints, full/minimal variants)

- ✅ GREEN phase: All implementations complete
  - Provider.kt: @Serializable data class with @SerialName on all fields
  - AuthKey.kt: 4 OAuth types + CodeChallengeMethod enum
  - ProvidersApi.kt: list() with ProvidersResponse wrapper
  - AuthApi.kt: createAuthCode() and exchangeCode() with contentType(Application.Json)
  - OpenRouterClient.kt: providers and auth properties integrated

- ✅ Full test suite: BUILD SUCCESSFUL, 4 actionable tasks up-to-date
- ✅ Evidence saved to .sisyphus/evidence/task-12-providers-oauth.txt

### Pattern Reconfirmed
1. **GET endpoint with wrapper**: ProvidersApi uses private ProvidersResponse type to handle {data: [...]} structure
2. **POST endpoints**: Both auth methods use contentType(ContentType.Application.Json) before setBody()
3. **OAuth PKCE naming**: 
   - AuthCodeRequest/Response for POST /auth/keys/code (create authorization)
   - AuthKeyRequest/Response for POST /auth/keys (exchange code for key)

### TDD Discipline
This task demonstrates the value of TDD: all tests existed from a prior session, making verification instant. No implementation changes needed.

### Completion Status
Task 12 is 100% complete and verified. Ready for commit (if not already committed).


## [2026-03-24T08:59:27+09:00] Task 13: Responses API (beta)

### Pattern Applied
- @ExperimentalOpenRouterApi annotation for beta stability marker
- Union type for input field (String | Array<InputItem>)
- Sealed class with polymorphic serialization for InputItem variants
- POST-only endpoint (no streaming support)

### Type Definitions
- CreateResponseRequest: input union, model, instructions, tools, temperature, topP, maxTokens
- ResponseObject: response structure with choices array
- InputItem: sealed class with Message, FunctionCallOutput variants
- ResponseTool: tool definition with type, name, description, parameters

### Annotation Usage
- @ExperimentalOpenRouterApi on ALL public types in responses package
- @ExperimentalOpenRouterApi on ResponsesApi class and methods
- @ExperimentalOpenRouterApi on OpenRouterClient.responses property
- Warnings appear in compile output for beta API usage (expected behavior)

### Implementation Details
- ResponseInput union type uses custom KSerializer pattern from Task 2
- InputItem uses kotlinx.serialization polymorphic serialization with @SerialName
- ResponseInputSerializer handles JsonPrimitive (String) and JsonArray variants
- Test helper bodyAsText() extension required in each test file (private scope)

### Key Learnings
- Beta API requires explicit opt-in via annotation at all public boundaries
- Input union type reuses established union serializer pattern
- Streaming NOT supported (Responses API has different SSE format than Chat)
- Test file requires private extension function for request body access
- @OptIn(ExperimentalOpenRouterApi::class) required on test classes using beta API

## [2026-03-25T09:15:00+09:00] Task 14: Chat DSL Builder

### Pattern Applied
- @DslMarker annotation for scope isolation in nested DSL contexts
- Builder pattern: mutable builder → immutable result via `build()`
- Extension function on OpenRouterClient for ergonomic API surface
- chatRequest { } for standalone request builder
- client.chat { } for direct API call with DSL

### DSL Design
- **chatRequest { } function**: Creates ChatCompletionRequest from DSL
- **client.chat { } extension**: Combines DSL with API call in one expression
- **Message builders**: systemMessage, userMessage, assistantMessage, toolMessage
- **All fields supported**: All ChatCompletionRequest fields exposed as mutable properties
- **Validation in build()**: requireNotNull for model, require for non-empty messages

### Implementation Details
- @OpenRouterDslMarker prevents scope leakage (outer builder properties inaccessible in nested contexts)
- ChatRequestBuilder accumulates messages in private mutableList
- Two userMessage variants: String (creates Content.Text) and lambda returning Content
- stream field forced to false (non-streaming DSL as per Task 14)
- assistantMessage accepts optional toolCalls parameter
- build() returns immutable ChatCompletionRequest

### Key Learnings
- **@DslMarker critical**: Without it, nested DSL contexts could accidentally reference outer scope properties
- **Builder validation**: Use requireNotNull/require in build() instead of constructor to allow gradual property assignment
- **Extension functions**: `suspend fun OpenRouterClient.chat(block)` provides natural API - reads like "client.chat { model = ... }"
- **Mutable builder → immutable result**: Standard pattern for type-safe DSL builders in Kotlin
- **Two userMessage overloads**: String convenience + lambda flexibility for Content.Parts use cases

### Test Coverage
- Minimal request (model + single user message)
- Full request (all optional fields + multiple message types)
- Message builder tests for each type (system, user, assistant, tool)
- userMessage lambda variant (Content.Text via lambda)
- client.chat { } integration test with MockEngine
- Validation tests (missing model, missing messages)

### TDD Process
- **RED phase**: Wrote comprehensive test suite first, verified compilation errors
- **GREEN phase**: Implemented @DslMarker → ChatRequestBuilder → ChatDsl.kt
- **Result**: All tests passed on first run after implementation
- **No regressions**: Full test suite passes

### Files Created
- src/main/kotlin/dev/toliner/openrouter/l2/OpenRouterDslMarker.kt
- src/main/kotlin/dev/toliner/openrouter/l2/chat/ChatRequestBuilder.kt
- src/main/kotlin/dev/toliner/openrouter/l2/chat/ChatDsl.kt
- src/test/kotlin/dev/toliner/openrouter/l2/chat/ChatDslTest.kt

### Evidence
- .sisyphus/evidence/task-14-dsl-minimal.txt: DSL-specific tests passing
- .sisyphus/evidence/task-14-dsl-integration.txt: Full test suite passing (no regressions)

### API Example
```kotlin
val response = client.chat {
    model = "openai/gpt-4o"
    temperature = 0.7
    systemMessage("You are a helpful assistant.")
    userMessage("What is Kotlin?")
}
```

### Commit Readiness
- ✅ All implementation files created
- ✅ All test files created  
- ✅ Tests pass
- ✅ No regressions
- ✅ Ready for commit: `feat(l2): add chat DSL builder`

## [2026-03-25 01:20] Task 15: Stream DSL Builder

### Pattern Applied
- **chatStream { }**: Extension reuses ChatRequestBuilder from Task 14
- Auto-sets `stream=true` via `.build().copy(stream = true)`
- Returns `Flow<ChatCompletionChunk>` from existing `chat.stream(request)`
- No modifications needed to ChatRequestBuilder (true reuse)

### Flow Extensions Implementation
- **collectContent()**: `Flow.fold("")` accumulator for `delta.content`
  - Pattern: `fold("") { acc, chunk -> acc + (chunk.choices.firstOrNull()?.delta?.content ?: "") }`
  - Null-safe: handles missing choices, null content
- **collectContentAndUsage()**: Tracks last usage chunk during fold
  - Pattern: `var lastUsage: Usage? = null` + `fold` + side-effect capture
  - Returns `Pair<String, Usage>` with default Usage(0,0,0) if none provided
  - Last usage wins when multiple chunks contain usage

### TDD Execution
- **RED Phase**: Tests written first with unresolved references
- **GREEN Phase**: Implementation passed all tests immediately
- Test coverage: 8 test cases covering edge cases (null content, empty choices, multiple usage)

### Key Learnings
- **Immutable request modification**: `.copy(stream = true)` pattern perfect for DSL overrides
- **Flow.fold() for accumulation**: Clean pattern for aggregating streaming chunks
- **Side-effect tracking in fold**: Capture auxiliary data (usage) while accumulating content
- **Reuse builder with extension**: No need to modify existing builder, wrap with extension
- **JsonPrimitive access**: Use `.content` property, not `.boolean` (e.g., `jsonPrimitive.content.toBoolean()`)

### Test Structure
- MockEngine validates request body: `stream=true`, `model` passthrough
- Manual Flow creation: `flowOf(...)` for testing extension functions in isolation
- Comprehensive edge cases: null handling, empty collections, multiple usage objects
