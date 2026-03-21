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
