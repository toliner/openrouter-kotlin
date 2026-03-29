# serialization/ — Custom Serializers

Custom `KSerializer` implementations for JSON union types that kotlinx-serialization cannot handle automatically.

## Structure

- **OpenRouterJson.kt** — Global `Json` instance used throughout the project.
- **ContentSerializer.kt** — Handles `Content` (string | array of content parts).
- **StringOrArraySerializer.kt** — Handles `StringOrArray` (string | array of strings).
- **ToolChoiceSerializer.kt** — Handles `ToolChoice` (string | object with function name).

## OpenRouterJson Configuration

```kotlin
val OpenRouterJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    isLenient = false
    encodeDefaults = false
}
```

All serialization in the project MUST use this instance. Never create a separate `Json` instance.

## Custom Serializer Pattern

Every custom serializer follows this exact structure:

### 1. Sealed Interface (in l1/ or where the type is used)
```kotlin
@Serializable(with = XxxSerializer::class)
sealed interface Xxx {
    @JvmInline value class StringVariant(val value: String) : Xxx
    data class ObjectVariant(...) : Xxx
}
```

### 2. Object Serializer (in serialization/)
```kotlin
object XxxSerializer : KSerializer<Xxx> {
    override val descriptor = buildClassSerialDescriptor("Xxx")

    override fun deserialize(decoder: Decoder): Xxx {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        return when (element) {
            is JsonPrimitive -> Xxx.StringVariant(element.content)
            else -> // decode object/array variant
        }
    }

    override fun serialize(encoder: Encoder, value: Xxx) {
        require(encoder is JsonEncoder)
        when (value) {
            is Xxx.StringVariant -> encoder.encodeString(value.value)
            is Xxx.ObjectVariant -> // encode object/array variant
        }
    }
}
```

### Key Rules
- Always `require(decoder is JsonDecoder)` / `require(encoder is JsonEncoder)` — these serializers are JSON-only.
- Use `buildClassSerialDescriptor("Name")` for the descriptor.
- Branch on `JsonPrimitive` vs other `JsonElement` subtypes in deserialize.
- Branch on sealed subtypes in serialize.
- ToolChoiceSerializer uses a local `@Serializable data class` inside the serialize/deserialize methods for the object variant.

## Testing

Tests in `src/test/kotlin/dev/toliner/openrouter/serialization/`. Both round-trip and property-based testing:

```kotlin
class XxxSerializerTest : FunSpec({
    test("round-trip string variant") {
        val original = Xxx.StringVariant("value")
        val json = OpenRouterJson.encodeToString(original)
        val decoded = OpenRouterJson.decodeFromString<Xxx>(json)
        decoded shouldBe original
    }

    test("property-based round-trip") {
        checkAll(Arb.string()) { s ->
            val original = Xxx.StringVariant(s)
            // round-trip assertion
        }
    }
})
```
