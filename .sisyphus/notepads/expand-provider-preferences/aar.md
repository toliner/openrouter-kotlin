# AAR: Expand ProviderPreferences

## Summary
Expanded `ProviderPreferences` from 7 to 13 properties to match the full OpenRouter `/chat/completions` API spec. Converted `dataCollection` from `String` to a type-safe `DataCollection` enum. Added L2 DSL support for all new properties including nested builders.

## Changes

### New L1 Types (src/main/kotlin/dev/toliner/openrouter/l1/chat/)
- `DataCollection.kt` — enum (ALLOW, DENY), replaces raw String
- `Quantization.kt` — enum (INT4, INT8, FP4, FP6, FP8, FP16, BF16, FP32, UNKNOWN)
- `MaxPrice.kt` — data class for hard price gate (prompt, completion, image, audio, request)
- `PercentileCutoffs.kt` — data class shared by throughput/latency (p50, p75, p90, p99)
- `ProviderSort.kt` — sealed interface: Simple(string) | Advanced(by, partition)
- `PreferredThroughput.kt` — sealed interface: Value(number) | Percentile(cutoffs)
- `PreferredLatency.kt` — sealed interface: Value(number) | Percentile(cutoffs)

### New Serializers (src/main/kotlin/dev/toliner/openrouter/serialization/)
- `ProviderSortSerializer.kt` — string ↔ Simple, object ↔ Advanced
- `PreferredThroughputSerializer.kt` — number ↔ Value, object ↔ Percentile
- `PreferredLatencySerializer.kt` — number ↔ Value, object ↔ Percentile

### Updated
- `ProviderPreferences.kt` — added: only, zdr, enforceDistillableText, quantizations, maxPrice, preferredMaxLatency; changed: dataCollection (String→DataCollection), sort (String→ProviderSort), preferredMinThroughput (Int→PreferredThroughput)
- `ProviderRoutingBuilder.kt` — full rewrite with all properties + nested DSL builders (ProviderSortBuilder, MaxPriceBuilder, PercentileCutoffsBuilder)

### Tests
- `ProviderPreferencesTest.kt` — 14 new round-trip tests covering all new types
- `RoutingDslTest.kt` — 11 tests covering all new DSL features
- `ChatCompletionTypesTest.kt` — updated dataCollection from String to DataCollection enum

## Decision Log
- `PercentileCutoffs` is shared between throughput and latency (same p50/p75/p90/p99 structure)
- `MaxPrice` fields are `String?` (BigNumber format per API spec) not `Double`
- `PreferredThroughput.Value` and `PreferredLatency.Value` use `Double` since the JSON values are numbers
- Union types use sealed interface + custom serializer pattern consistent with existing `StringOrArray`, `ToolChoice`
