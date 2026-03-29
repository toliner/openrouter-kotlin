# l1/ — Low-Level API Data Models

Pure data classes and sealed types that mirror the OpenRouter JSON API schema exactly. No logic, no HTTP, no builders.

## Structure

Organized by API endpoint:
- `chat/` — ChatCompletionRequest, ChatCompletionResponse, Message (sealed), Choice, Usage, FunctionTool, ToolCall, etc.
- `models/` — Model, ModelPricing, ModelArchitecture, etc.
- `guardrails/` — GuardrailsRequest, GuardrailsResponse, etc.
- `responses/` — ResponsesRequest, ResponsesResponse, etc. (experimental)
- `keys/` — KeysResponse, KeyData, etc.
- `embeddings/` — EmbeddingsRequest, EmbeddingsResponse, etc.
- `account/` — AccountResponse, RateLimitResponse, etc.
- `auth/`, `generation/`, `providers/` — Smaller endpoint models (1-2 files each).

## Conventions

### Every type MUST:
1. Be annotated with `@Serializable`.
2. Be a `data class` (for concrete types) or `sealed class`/`sealed interface` (for union types).
3. Have explicit `@SerialName("snake_case")` on **every** serialized property — never rely on naming strategies.
4. Default all optional fields to `null`.

### Example
```kotlin
@Serializable
data class ExampleRequest(
    @SerialName("model")
    val model: String,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("temperature")
    val temperature: Double? = null,
)
```

### Sealed Types with Discriminator
```kotlin
@Serializable
sealed interface Message {
    @Serializable
    @SerialName("system")
    data class System(
        @SerialName("content") val content: Content,
    ) : Message
    // ...
}
```

The `@SerialName` on the subtype provides the discriminator value (e.g., `"role": "system"`).

### Union Types (JSON polymorphism)
When a field can be `string | object` or `string | array`, define a sealed interface and a custom serializer in the `serialization/` package. Annotate the sealed parent with `@Serializable(with = XxxSerializer::class)`.

## What NOT to Put Here
- Builder logic → belongs in `l2/`
- HTTP calls → belongs in `client/`
- Custom serialization logic → belongs in `serialization/`
- Error types → belongs in `error/`

## Testing

Tests are in `src/test/kotlin/dev/toliner/openrouter/l1/`. Pattern:
```kotlin
class XxxTest : FunSpec({
    test("round-trip serialization") {
        val original = ExampleRequest(model = "test")
        val json = OpenRouterJson.encodeToString(original)
        val decoded = OpenRouterJson.decodeFromString<ExampleRequest>(json)
        decoded shouldBe original
    }
})
```

Test both round-trip and explicit JSON string assertions for critical fields.
