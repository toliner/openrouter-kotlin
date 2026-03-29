# l2/ — High-Level DSL Builders

Fluent Kotlin DSL builders that construct l1 data model instances. No HTTP, no serialization logic.

## Structure

- `OpenRouterDslMarker.kt` — `@DslMarker annotation class OpenRouterDslMarker` preventing scope leakage.
- `chat/` — `ChatRequestBuilder`, `ChatApi.invoke` extension for `client.chat { }` syntax, top-level `chatRequest {}` function.
- `stream/` — `StreamDsl` (extension `suspend fun OpenRouterClient.chatStream {}`), `FlowExtensions` (`collectContent()`, `collectContentAndUsage()`).
- `tools/` — `ToolsBuilder`, `FunctionToolBuilder`, `JsonSchemaBuilder`, `FunctionParameterBuilder`.
- `routing/` — `ProviderRoutingBuilder`.

## Builder Pattern

Every builder follows this structure:

```kotlin
@OpenRouterDslMarker
class XxxBuilder {
    // Mutable var properties mirroring l1 fields
    var fieldName: Type? = null

    // Private mutable lists for collection fields
    private val items = mutableListOf<ItemType>()

    // Methods for adding to collections
    fun addItem(value: ItemType) { items.add(value) }

    // Nested builder methods
    fun nested(block: NestedBuilder.() -> Unit) {
        // ...
    }

    // build() produces the l1 type
    fun build(): L1Type {
        return L1Type(
            fieldName = requireNotNull(fieldName) { "fieldName is required" },
            items = items.toList(),
        )
    }
}
```

## Conventions

- **Always** annotate builder classes with `@OpenRouterDslMarker`.
- Mutable `var` properties for optional fields (default `null`).
- Private `mutableListOf` for collection fields, exposed via add/builder methods.
- `build()` method uses `requireNotNull` / `require` for validation.
- Top-level entry-point functions use the pattern: `fun xxxRequest(block: XxxBuilder.() -> Unit): L1Type = XxxBuilder().apply(block).build()`.
- Extension functions on `ChatApi` / `OpenRouterClient` for ergonomic API: `suspend fun ChatApi.invoke(block: ChatRequestBuilder.() -> Unit)`.

## Message Builder Methods (ChatRequestBuilder)

```kotlin
fun systemMessage(content: String)      // Adds Message.System
fun userMessage(content: String)        // Adds Message.User (string content)
fun userMessage(block: ...)             // Adds Message.User (structured content)
fun assistantMessage(content: String)   // Adds Message.Assistant
fun toolMessage(toolCallId: String, content: String)  // Adds Message.Tool
```

## Testing

Tests are in `src/test/kotlin/dev/toliner/openrouter/l2/`. Test builders by constructing with the DSL and asserting the `build()` output matches expected l1 types:

```kotlin
class ChatRequestBuilderTest : FunSpec({
    test("builds valid request") {
        val request = chatRequest {
            model = "test-model"
            systemMessage("Hello")
        }
        request.model shouldBe "test-model"
    }
})
```
