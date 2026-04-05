# After Action Report: ResponseFormat json_schema Support

## Task
`ChatRequestBuilder.responseFormat` が構造化出力の `json_schema` 方式に対応していない問題の調査と修正。

## Findings
- `ResponseFormat` は `data class ResponseFormat(val type: String)` という単純な型で、`json_schema` に必要なネストオブジェクトを表現できなかった
- OpenRouter API は `response_format` に5つのバリアント（text, json_object, json_schema, grammar, python）を持つ oneOf union 型を定義

## Changes

### Dependencies
- `org.jetbrains.kotlinx:kotlinx-schema-generator-json:0.4.4` を追加（JetBrains製、@Serializable型からJSON Schema自動生成）

### l1 Layer
- `ResponseFormat` を `sealed interface` に変更（破壊的変更）
- 5バリアント: `Text`, `JsonObject`, `JsonSchema`, `Grammar`, `Python`
- `JsonSchemaConfig` data class 新設（name, description, schema, strict）

### serialization Layer
- `ResponseFormatSerializer` 新設 — `type` フィールドで分岐するカスタム KSerializer

### l2 Layer
- `ResponseFormatBuilder` DSL 新設
  - `jsonSchema<T>(name)` — reified型パラメータから JSON Schema 自動生成
  - `jsonSchema(name) { schema = ... }` — 手書きスキーマ
  - `text()`, `jsonObject()`, `grammar()`, `python()`
- `ChatRequestBuilder` に `responseFormat { }` DSL メソッド追加

### Tests
- `ResponseFormatTest.kt` — 11 round-trip tests
- `ResponseFormatSerializerTest.kt` — ~25 serializer tests
- `ResponseFormatBuilderTest.kt` — 13 DSL tests (auto-gen + manual)
- 既存テスト2ファイル修正

## Breaking Changes
- `ResponseFormat(type = "json_object")` → `ResponseFormat.JsonObject`
- `ResponseFormat(type = "text")` → `ResponseFormat.Text`
- `responseFormat?.type` → pattern match on sealed subtypes
