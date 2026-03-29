# openrouter-kotlin

Kotlin client library for the OpenRouter API.

## Architecture

### Layer Model
The codebase follows a strict 3-layer architecture:

- **l1/** — Low-level data models. `@Serializable data class` / `sealed class` mirroring the OpenRouter JSON API exactly. No logic, no builders, no HTTP.
- **l2/** — High-level DSL builders. `@OpenRouterDslMarker`-annotated builder classes that produce l1 types. No HTTP, no serialization logic.
- **client/** — API implementation. Ktor-based HTTP client classes that consume l1 request types and return l1 response types.
- **serialization/** — Custom `KSerializer` implementations for JSON union types (string|array, string|object).
- **streaming/** — SSE parsing and in-band error detection for streaming responses.
- **error/** — Sealed exception hierarchy mapping HTTP status codes to typed exceptions.

Dependencies flow **downward only**: `client/` → `l1/`, `l2/` → `l1/`, `client/` → `serialization/`, `client/` → `streaming/`, `client/` → `error/`. The `l1/` and `l2/` layers never depend on `client/`.

### Package Root
All source code lives under `dev.toliner.openrouter` in `src/main/kotlin/` and `src/test/kotlin/`.

## Build & Tooling

- **Build system**: Gradle Kotlin DSL, single module
- **Kotlin**: 2.3.10 with kotlinx-serialization compiler plugin
- **JDK**: 25 (required — set in `jitpack.yml` via SDKMAN)
- **Dependencies**: Ktor client (CIO engine), kotlinx-serialization-json 1.10.0
- **Testing**: Kotest with JUnit5 runner
- **Publishing**: maven-publish plugin, JitPack, GitHub Releases on `v*.*.*` tags
- **CI**: GitHub Actions — build+test on push to main and PRs (`.github/workflows/ci.yml`), release on tags (`.github/workflows/release.yml`)

### Common Commands
```bash
./gradlew build          # Full build + tests
./gradlew test           # Tests only
./gradlew publishToMavenLocal  # Local publish for testing
```

## Conventions

### Code Style
- `kotlin.code.style=official` (in `gradle.properties`). No detekt, ktlint, or spotless.
- No `@Suppress` annotations anywhere in the codebase.
- No `TODO`, `FIXME`, or `HACK` comments.

### Kotlin Patterns
- All API methods are `suspend` functions (coroutines throughout).
- Optional fields default to `null`, never use sentinel values.
- Every serialized field has an explicit `@SerialName("snake_case")` annotation — do not rely on `Json { namingStrategy }`.
- Sealed classes/interfaces for union types, with `@Serializable(with = XxxSerializer::class)` on the sealed parent.
- `@RequiresOptIn` used via `@ExperimentalOpenRouterApi` for unstable API surfaces (currently: ResponsesApi).

### Error Handling
- Never throw raw exceptions from API calls. All errors go through the sealed `OpenRouterException` hierarchy.
- HTTP errors map via `errorFromStatus()` in the error package.
- In-band errors (error inside a successful HTTP response) detected and thrown as `OpenRouterException.InBandError`.
- Streaming errors detected and thrown as `OpenRouterException.StreamError`.

### Testing
- Kotest `FunSpec` DSL — every test class extends `FunSpec({ ... })`.
- Test file structure mirrors main source structure.
- l1 tests: Round-trip serialization (`encodeToString` → `decodeFromString` → `shouldBe original`).
- client tests: `mockEngineWithResponse()` helper from `testutil/MockEngineHelpers.kt`, with `requestValidator` lambdas.
- Use `shouldThrow<ExceptionType>` for error path assertions.
- Descriptive English test names.

### Adding a New API Endpoint
1. Create l1 request/response data classes in `l1/{endpoint}/` with `@Serializable` + `@SerialName`.
2. Create an `XxxApi` class in `client/` taking `(HttpClient, OpenRouterConfig)`.
3. Follow the POST or GET pattern from existing Api classes (see `client/AGENTS.md`).
4. Wire it into `OpenRouterClient` as a `val xxx: XxxApi` property.
5. Optionally add l2 DSL builders in `l2/{endpoint}/`.
6. Add tests mirroring the main structure in `src/test/kotlin/`.

### Adding a New l1 Data Model
1. Place in the appropriate `l1/{endpoint}/` package.
2. Use `@Serializable data class` with explicit `@SerialName` on every field.
3. Default optional fields to `null`.
4. If the type has JSON union variants, create a sealed interface + custom serializer in `serialization/`.
5. Add round-trip serialization tests.

## Finalize
On complete task,
1. write after action report to .sisyphus/notepads/${task_name}/aar.md
2. commit all changes
