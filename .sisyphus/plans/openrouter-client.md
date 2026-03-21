# OpenRouter Kotlin APIクライアントライブラリの構築

本ExecPlanは生きたドキュメントである。`Progress`、`Surprises & Discoveries`、`Decision Log`、`Outcomes & Retrospective` セクションは作業の進行に合わせて更新すること。

本ドキュメントはリポジトリルートの `.agent/PLANS.md` に準拠して管理する。

## TL;DR

> **概要**: OpenRouter APIの全エンドポイントに対応するKotlin型付きクライアントライブラリを構築する。L1（型付きREST API層）とL2（Kotlin DSLラッパー層、Chat Completions中心）の2層アーキテクチャを採用。AWS CDKのL1/L2パターンに着想を得た設計。
>
> **成果物**:
> - L1: 全OpenRouterエンドポイントの型付きAPI（33エンドポイント）
> - L2: Chat Completions中心のKotlin DSL（chat/stream/tools/provider routing）
> - SSEストリーミング（Flow<T>）
> - 型付きエラーハンドリング（sealed class例外階層）
> - TDDによる包括的テストスイート
>
> **規模**: Large
> **並列実行**: YES - 5ウェーブ
> **クリティカルパス**: ビルド設定 → シリアライゼーション基盤 → L1 Core Inference → L1管理系API → L2 DSL

---

## Purpose / Big Picture

本変更により、Kotlin/JVM開発者はOpenRouter APIを型安全に利用できるようになる。200以上のAIモデルへのアクセス、ストリーミングレスポンス、プロバイダールーティング、ツール呼び出しを、IDEの補完が効く直感的なDSLで記述可能にする。

変更後にできること:
1. L1 APIで全OpenRouterエンドポイントに型安全にアクセスできる
2. L2 DSLでChat Completionsを直感的に記述できる（`chat { }`, `stream { }`, Tool DSL）
3. SSEストリーミングを`Flow<ChatCompletionChunk>`として受信できる
4. エラーをsealed classとしてハンドリングできる（型安全なwhen式）

動作確認方法: `./gradlew test`を実行し、全テストがパスすることを確認する。MockEngineを使ったテストにより、実際のAPIキーなしで全機能を検証できる。

---

## Progress

- [ ] Milestone 0: ビルド設定・依存関係のセットアップ
- [ ] Milestone 1: シリアライゼーション基盤・エラー型・カスタムシリアライザー
- [ ] Milestone 2: L1 Core Inference（Chat Completions + SSEストリーミング + Models）
- [ ] Milestone 3: L1管理系API（Keys, Credits, Analytics, Guardrails, Providers, OAuth, Generation）
- [ ] Milestone 4: L2 DSL（Chat, Stream, Tools, Provider Routing）
- [ ] 最終検証

---

## Surprises & Discoveries

（実装進行に伴い記録する）

---

## Decision Log

- Decision: JVM onlyで開始し、KMP対応は後回し
  Rationale: サーバーサイド・CLI用途が主。エンジン注入パターン（HttpClientEngine）を採用することで将来のKMP移行も容易。ビルド設定がシンプルになる。
  Date/Author: 2026-03-21

- Decision: 全エンドポイント網羅（POST /messagesとPOST /credits/coinbase除く）
  Rationale: ユーザーが「全エンドポイント網羅」を選択。ただしAnthropic Messages API互換（POST /messages）はOpenRouter固有でなくニッチなため除外。Coinbase決済も同様に除外。対象エンドポイント内のフィールド（trace等）は全て含める。
  Date/Author: 2026-03-21

- Decision: Responses API (beta) はL1のみ（L2 DSL対象外）
  Rationale: beta版で仕様変更リスクあり。型定義とエンドポイントのみL1に含め、L2 DSLは安定してから追加する。
  Date/Author: 2026-03-21

- Decision: 単一モジュール・パッケージ分離
  Rationale: 初期開発速度を優先。パッケージレベルで明確に分離し、必要に応じて後からマルチモジュール化可能。
  Date/Author: 2026-03-21

- Decision: L2 DSLスコープはChat Completions中心
  Rationale: 最も利用頻度の高いAPIに注力。stream/non-stream、Tools、Provider Routing DSLを含む。他のAPI（Models, Embeddings等）はL1直接利用。
  Date/Author: 2026-03-21

- Decision: TDD（テスト先行）+ Kotest 6.1
  Rationale: 品質重視。Kotest FunSpecはsuspend対応でKtor非同期テストに最適。MockEngineでHTTP呼び出しを完全にモック可能。
  Date/Author: 2026-03-21

- Decision: SSEテストはフロー変換ユニットテスト方式
  Rationale: Ktor既知問題KTOR-7910（testApplicationでSSEイベント一括配信）を回避。Flow<ServerSentEvent>→Flow<T>変換ロジックを手作りFlowで単体テストする。
  Date/Author: 2026-03-21

- Decision: @SerialNameを使用、JsonNamingStrategy.SnakeCaseは使わない
  Rationale: SnakeCaseは@Experimentalであり、カスタムシリアライザーとの相性が不安定。@SerialNameはstable APIで確実。
  Date/Author: 2026-03-21

- Decision: Maven公開は後回し
  Rationale: まず機能実装に集中。maven-publish、KDoc生成は別プランで対応。
  Date/Author: 2026-03-21

- Decision: expectSuccess=false + コールサイトエラー処理
  Rationale: HttpResponseValidatorはレスポンスボディを消費するためbody<T>()の再呼び出しが失敗する。in-bandエラー（HTTP 200 + choices[i].error）の検出にはコールサイトでの処理が安全。
  Date/Author: 2026-03-21

---

## Outcomes & Retrospective

（実装完了時に記載する）

---

## Context and Orientation

### プロジェクトの現状

本リポジトリ `openrouter-kotlin` はグリーンフィールドプロジェクトである。Kotlinソースファイルは1つも存在せず、`build.gradle.kts` のスケルトンのみがある。

主要ファイル:
- `build.gradle.kts`: Kotlin JVM 2.3.10、JVMツールチェイン25、group=dev.toliner、version=1.0-SNAPSHOT。現在の依存関係はkotlin("test")のみ。
- `settings.gradle.kts`: rootProject.name="openrouter-kotlin"、foojay-resolver 1.0.0プラグイン。
- `.agent/PLANS.md`: ExecPlanのテンプレートと規約を定義。

### 使用ライブラリ

- **Ktor 3.4.1**: HTTPクライアントフレームワーク。`HttpClient` に `HttpClientEngine` をコンストラクタ注入するパターンでテスト容易性とKMP移行性を確保する。SSE（Server-Sent Events）プラグインが `ktor-client-core` に組み込まれている。`incoming` プロパティで `Flow<ServerSentEvent>` を取得できる。
- **kotlinx.serialization 1.10.0**: JSON シリアライゼーション。`@Serializable` アノテーションでデータクラスをJSON対応にする。`@SerialName("snake_case_name")` でJSONフィールド名を指定する。Union型（`String | Array`等）にはカスタム `KSerializer` を実装する。
- **Kotest 6.1**: テストフレームワーク。`FunSpec` がsuspend対応でKtorの非同期テストに最適。`coroutineTestScope = true` で仮想時間テスト可能。`forAll`/`checkAll` でプロパティベーステスト可能。

### OpenRouter APIの概要

OpenRouterは200以上のAIモデル（OpenAI、Anthropic、Google等）に統一APIでアクセスできるサービスである。

認証には2種類のAPIキーがある:
- **Regular API Key**: 推論系エンドポイント（chat/completions, embeddings等）に使用。`Authorization: Bearer sk-or-v1-...` ヘッダーで送信。
- **Management API Key**: 管理系エンドポイント（keys CRUD, credits, analytics, guardrails）に使用。同じBearerトークン形式だがキー自体が異なる。

SSEストリーミングのプロトコル:
- 各イベントは `data: {JSON}\n\n` 形式で配信される
- コメント行（`: OPENROUTER PROCESSING`等）は無視する
- `data: [DONE]` がストリーム終了シグナル
- 最終チャンクの直前にusageチャンク（`choices: []`でusage情報のみ）が送信される
- `debug.echo_upstream_body = true` の場合、最初のチャンクに `choices: []` と `debug` フィールドが含まれる
- HTTPレベルエラー（4xx/5xx）はストリーム開始前に発生する
- ストリーム内エラーはHTTP 200で `finish_reason: "error"` として配信される
- in-bandエラー: HTTP 200レスポンスで `choices[i].error` が設定されている場合もエラーとして扱う

対象エンドポイント（33個、POST /messagesとPOST /credits/coinbase除外）:
1. POST /chat/completions — チャット補完（ストリーミング対応）
2. POST /responses — Responses API (beta)
3. POST /embeddings — テキスト埋め込み
4. GET /models — モデル一覧
5. GET /models/count — モデル数
6. GET /models/user — ユーザーモデル一覧
7. GET /models/{author}/{slug}/endpoints — モデルエンドポイント一覧
8. GET /endpoints/zdr — ZDR影響プレビュー
9. GET /embeddings/models — 埋め込みモデル一覧
10. GET /generation — 生成詳細取得
11. GET /providers — プロバイダー一覧
12. GET /key — 現在のキー情報
13. GET /credits — クレジット残高
14. GET /activity — アクティビティ（利用統計）
15-17. GET/POST /keys, GET/PATCH/DELETE /keys/{hash} — キー管理CRUD（5エンドポイント）
18-23. Guardrails CRUD + アサインメント（8エンドポイント）
24-25. POST /auth/keys, POST /auth/keys/code — OAuth PKCE（2エンドポイント）

### パッケージ構成

```
src/main/kotlin/dev/toliner/openrouter/
├── client/                     # OpenRouterClient, ClientConfig
├── l1/
│   ├── chat/                   # Chat Completions リクエスト/レスポンス型 + エンドポイント
│   ├── responses/              # Responses API (beta) 型 + エンドポイント
│   ├── embeddings/             # Embeddings型 + エンドポイント
│   ├── models/                 # Models型 + エンドポイント
│   ├── generation/             # Generation型 + エンドポイント
│   ├── account/                # Credits, Key info, Activity
│   ├── keys/                   # Key管理CRUD
│   ├── guardrails/             # Guardrails CRUD + アサインメント
│   ├── providers/              # Providers
│   └── auth/                   # OAuth PKCE
├── l2/
│   ├── chat/                   # chat { } DSLビルダー
│   ├── stream/                 # stream { } DSLビルダー
│   ├── tools/                  # Tool DSL
│   └── routing/                # Provider Routing DSL
├── error/                      # sealed例外階層
├── streaming/                  # SSE解析、Flow変換
└── serialization/              # カスタムシリアライザー（Union型等）

src/test/kotlin/dev/toliner/openrouter/
├── (上記と同じパッケージ構成でテスト)
└── testutil/                   # テストユーティリティ（MockEngineヘルパー等）
```

### 用語定義

- **L1 (Level 1)**: OpenRouter REST APIの1対1マッピング。各エンドポイントに対応する型付き関数を提供する。AWS CDKのL1 Constructに相当。
- **L2 (Level 2)**: L1を基盤としたKotlin DSLラッパー。ビルダーパターンで直感的にリクエストを構築できる。AWS CDKのL2 Constructに相当。
- **SSE (Server-Sent Events)**: サーバーからクライアントへの一方向ストリーミングプロトコル。OpenRouterのストリーミングレスポンスで使用。
- **Union型**: JSONフィールドが複数の型を取りうるもの。例: `content` は `String` または `Array<ContentPart>` のどちらか。
- **in-bandエラー**: HTTPステータスコードが200（成功）だが、レスポンスボディ内にエラー情報が含まれているもの。
- **MockEngine**: Ktor提供のテスト用HTTPエンジン。実際のHTTP通信を行わず、プログラムで定義したレスポンスを返す。
- **@DslMarker**: Kotlin DSLでスコープの混在を防ぐアノテーション。内側のビルダーから外側のビルダーのメソッドを誤って呼び出すことを防ぐ。
- **Flow<T>**: Kotlinコルーチンの非同期ストリーム型。SSEストリーミングの結果を逐次処理するために使用する。

---

## Verification Strategy

> **全ての検証はエージェント実行。人間の介入は不要。**

### テスト方針
- **インフラ**: Kotest 6.1 + Ktor MockEngine（テストインフラセットアップはMilestone 0に含む）
- **方式**: TDD（RED→GREEN→REFACTOR）
- **テストスタイル**: Kotest FunSpec（suspend対応、runBlocking不要）
- **SSEテスト**: フロー変換ユニットテスト（手作りFlow<ServerSentEvent>で変換ロジックをテスト）
- **プロパティテスト**: forAll/checkAllでシリアライゼーション往復テスト

### QAポリシー
各タスクにエージェント実行QAシナリオを含む。
- **ライブラリ/モジュール**: Bash (`./gradlew test`) でテスト実行、結果検証
- エビデンスは `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}` に保存

---

## Execution Strategy

### 並列実行ウェーブ

```
Wave 1 (即座に開始 — 基盤):
├── Task 1: build.gradle.kts依存関係セットアップ [quick]
├── Task 2: Json設定 + カスタムシリアライザー基盤 [quick]
└── Task 3: sealed例外階層 [quick]

Wave 2 (Wave 1完了後 — L1 Core Inference、最大並列):
├── Task 4: Chat Completionsリクエスト/レスポンス型 [unspecified-high]
├── Task 5: SSEストリーミング解析・Flow変換 [deep]
├── Task 6: Models型 + エンドポイント [unspecified-high]
└── Task 7: Embeddings型 + エンドポイント [quick]

Wave 3 (Wave 2完了後 — OpenRouterClient + L1管理系):
├── Task 8: OpenRouterClient基盤 + Chat Completionsエンドポイント統合 [deep]
├── Task 9: Generation + Account (credits, key info, activity) [unspecified-high]
├── Task 10: Key管理CRUD [unspecified-high]
├── Task 11: Guardrails CRUD + アサインメント [unspecified-high]
├── Task 12: Providers + OAuth [unspecified-high]
└── Task 13: Responses API (beta) L1 [unspecified-high]

Wave 4 (Wave 3完了後 — L2 DSL):
├── Task 14: Chat DSLビルダー (non-stream) [unspecified-high]
├── Task 15: Stream DSLビルダー [unspecified-high]
├── Task 16: Tool Calling DSL [unspecified-high]
└── Task 17: Provider Routing DSL [quick]

Wave FINAL (全タスク完了後 — 4並列レビュー → ユーザー承認):
├── Task F1: プラン準拠監査 (oracle)
├── Task F2: コード品質レビュー (unspecified-high)
├── Task F3: 実QA (unspecified-high)
└── Task F4: スコープ忠実性チェック (deep)
→ 結果提示 → ユーザーの明示的OK取得

クリティカルパス: Task 1 → Task 4 → Task 8 → Task 14 → F1-F4 → ユーザーOK
並列高速化: 順次実行比約60%高速
最大同時実行: 6 (Wave 3)
```

### 依存関係マトリクス

| Task | Depends On | Blocks |
|------|-----------|--------|
| 1 | — | 2, 3, 4, 5, 6, 7 |
| 2 | 1 | 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 |
| 3 | 1 | 8, 9, 10, 11, 12, 13 |
| 4 | 2 | 8, 14 |
| 5 | 2 | 8, 15 |
| 6 | 2 | 8 |
| 7 | 2 | 8 |
| 8 | 3, 4, 5, 6, 7 | 9, 10, 11, 12, 13, 14, 15, 16, 17 |
| 9 | 3, 8 | — |
| 10 | 3, 8 | — |
| 11 | 3, 8 | — |
| 12 | 3, 8 | — |
| 13 | 2, 8 | — |
| 14 | 4, 8 | — |
| 15 | 5, 8 | — |
| 16 | 4, 8 | — |
| 17 | 8 | — |

### エージェントディスパッチ

- **Wave 1**: 3タスク — T1→`quick`, T2→`quick`, T3→`quick`
- **Wave 2**: 4タスク — T4→`unspecified-high`, T5→`deep`, T6→`unspecified-high`, T7→`quick`
- **Wave 3**: 6タスク — T8→`deep`, T9→`unspecified-high`, T10→`unspecified-high`, T11→`unspecified-high`, T12→`unspecified-high`, T13→`unspecified-high`
- **Wave 4**: 4タスク — T14→`unspecified-high`, T15→`unspecified-high`, T16→`unspecified-high`, T17→`quick`
- **FINAL**: 4タスク — F1→`oracle`, F2→`unspecified-high`, F3→`unspecified-high`, F4→`deep`

---

## TODOs

- [x] 1. build.gradle.kts 依存関係セットアップ

  **What to do**:
  - `build.gradle.kts` にkotlinx.serialization、Ktor、Kotestの全依存関係を追加する
  - `kotlin("plugin.serialization")` プラグインを追加する
  - Kotest用の `tasks.withType<Test> { useJUnitPlatform() }` を設定する
  - `./gradlew build` が成功することを確認する
  - 最小限のテスト（空のテストクラス）を作成して `./gradlew test` が動作することを確認する

  追加する依存関係:
  ```kotlin
  plugins {
      kotlin("jvm") version "2.3.10"
      kotlin("plugin.serialization") version "2.3.10"
  }

  dependencies {
      implementation("io.ktor:ktor-client-core:3.4.1")
      implementation("io.ktor:ktor-client-cio:3.4.1")
      implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
      implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")
      implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

      testImplementation("io.kotest:kotest-runner-junit5:6.1.0")
      testImplementation("io.kotest:kotest-assertions-core:6.1.0")
      testImplementation("io.kotest:kotest-property:6.1.0")
      testImplementation("io.ktor:ktor-client-mock:3.4.1")
  }
  ```

  **Must NOT do**:
  - maven-publishプラグインを追加しない
  - KDocやDokka関連を追加しない
  - kotlin("multiplatform")を使わない

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 単一ファイル（build.gradle.kts）の編集とビルド確認のみ
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3)
  - **Blocks**: Tasks 2, 3, 4, 5, 6, 7
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References**:
  - `build.gradle.kts` — 現在のビルド設定。kotlin("jvm") 2.3.10、JVMツールチェイン25が既に設定済み。ここに依存関係を追加する。

  **External References**:
  - Ktor 3.4.1のMaven座標: `io.ktor:ktor-client-core:3.4.1` 等
  - kotlinx.serialization: `kotlin("plugin.serialization")` プラグインが必須。プラグインなしでは `@Serializable` アノテーションが動作しない。

  **WHY Each Reference Matters**:
  - `build.gradle.kts`: 既存のKotlinバージョン（2.3.10）とJVMツールチェイン（25）設定を壊さずに依存関係を追加する必要がある

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: ビルドが成功する
    Tool: Bash
    Preconditions: build.gradle.ktsに全依存関係が追加済み
    Steps:
      1. `./gradlew clean build` を実行
      2. 終了コードが0であることを確認
    Expected Result: BUILD SUCCESSFUL、終了コード0
    Failure Indicators: BUILD FAILED、依存関係解決エラー、コンパイルエラー
    Evidence: .sisyphus/evidence/task-1-build-success.txt

  Scenario: テストランナーが動作する
    Tool: Bash
    Preconditions: 最小限のKotestテストクラスが存在
    Steps:
      1. `./gradlew test` を実行
      2. テストレポートを確認
    Expected Result: テストが実行されBUILD SUCCESSFUL
    Failure Indicators: No tests found、JUnit Platform設定エラー
    Evidence: .sisyphus/evidence/task-1-test-runner.txt
  ```

  **Commit**: YES
  - Message: `build: add Ktor, kotlinx.serialization, Kotest dependencies`
  - Files: `build.gradle.kts`, テストファイル
  - Pre-commit: `./gradlew build`

- [x] 2. Json設定 + カスタムシリアライザー基盤

  **What to do**:
  - TDD: まずテストを書き、失敗を確認してから実装する

  RED phase（テストから書く）:
  - `src/test/kotlin/dev/toliner/openrouter/serialization/OpenRouterJsonTest.kt` を作成
    - Json設定のテスト: `ignoreUnknownKeys=true` の動作確認
    - `explicitNulls=false` の動作確認（nullフィールドがJSONに含まれないこと）
    - `coerceInputValues=true` の動作確認
  - `src/test/kotlin/dev/toliner/openrouter/serialization/UnionSerializerTest.kt` を作成
    - `StringOrArray` シリアライザーのテスト:
      - JSON文字列 `"hello"` → `StringOrArray.Single("hello")`
      - JSON配列 `["a","b"]` → `StringOrArray.Multiple(listOf("a","b"))`
      - 逆方向のシリアライズも確認
    - `ContentSerializer` のテスト:
      - JSON文字列 `"text content"` → `Content.Text("text content")`
      - JSON配列 `[{"type":"text","text":"hello"},{"type":"image_url","image_url":{"url":"..."}}]` → `Content.Parts(listOf(...))`
    - `ToolChoiceSerializer` のテスト:
      - JSON文字列 `"auto"` → `ToolChoice.Auto`
      - JSON文字列 `"none"` → `ToolChoice.None`
      - JSON文字列 `"required"` → `ToolChoice.Required`
      - JSONオブジェクト `{"type":"function","function":{"name":"get_weather"}}` → `ToolChoice.Function(...)`
    - プロパティベーステスト（forAll）でシリアライゼーション往復を検証

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/serialization/OpenRouterJson.kt` を作成
    ```kotlin
    val OpenRouterJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        isLenient = false
        encodeDefaults = false
    }
    ```
  - `src/main/kotlin/dev/toliner/openrouter/serialization/StringOrArraySerializer.kt` を作成
    - sealed class `StringOrArray` と対応する `KSerializer`
  - `src/main/kotlin/dev/toliner/openrouter/serialization/ContentSerializer.kt` を作成
    - sealed class `Content`（Text | Parts）と対応するシリアライザー
    - `ContentPart` sealed class: TextPart, ImageUrlPart
  - `src/main/kotlin/dev/toliner/openrouter/serialization/ToolChoiceSerializer.kt` を作成
    - sealed class `ToolChoice`（Auto | None | Required | Function）と対応するシリアライザー

  **Must NOT do**:
  - `JsonNamingStrategy.SnakeCase` を使わない（@Experimental）
  - `@OptIn(ExperimentalSerializationApi::class)` を最小限に抑える
  - 実装に不要な型まで先行して作らない

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: シリアライゼーション設定とユニオン型シリアライザーの実装。パターンが明確で複雑なロジックなし。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3)
  - **Blocks**: Tasks 4, 5, 6, 7, 8, 9, 10, 11, 12, 13
  - **Blocked By**: Task 1

  **References**:

  **External References**:
  - kotlinx.serialization公式: カスタムシリアライザーの実装方法。`KSerializer<T>` を実装し、`descriptor`、`serialize`、`deserialize` の3メソッドを定義する。`JsonDecoder` にキャストして `decodeJsonElement()` を呼び出すことでJSON要素を直接取得できる。
  - `JsonContentPolymorphicSerializer`: sealed class階層の多相シリアライゼーション用。JSONの構造を見て適切なサブクラスにデシリアライズする。ただし Union型（String | Array等）にはこれではなく手動のKSerializerが適切。

  **WHY Each Reference Matters**:
  - OpenRouter APIではUnion型フィールドが頻出する（content, tool_choice, stop等）。これらを正しくシリアライズ/デシリアライズできないとAPI通信が機能しない。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/serialization/OpenRouterJsonTest.kt`
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/serialization/UnionSerializerTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.serialization.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Json設定の動作確認
    Tool: Bash
    Preconditions: OpenRouterJson.kt が実装済み
    Steps:
      1. `./gradlew test --tests "dev.toliner.openrouter.serialization.OpenRouterJsonTest"` を実行
      2. 全テストがPASSすることを確認
    Expected Result: テスト全PASS（ignoreUnknownKeys, explicitNulls, coerceInputValues）
    Failure Indicators: デシリアライゼーション例外、予期しないnullフィールド
    Evidence: .sisyphus/evidence/task-2-json-config.txt

  Scenario: Union型シリアライザーの往復テスト
    Tool: Bash
    Preconditions: 全カスタムシリアライザーが実装済み
    Steps:
      1. `./gradlew test --tests "dev.toliner.openrouter.serialization.UnionSerializerTest"` を実行
      2. StringOrArray, Content, ToolChoiceの全バリアントがテストされていることを確認
    Expected Result: serialize → deserialize == original が全バリアントで成功
    Failure Indicators: シリアライゼーション往復で値が変わる、不明なJSON構造で例外
    Evidence: .sisyphus/evidence/task-2-union-serializers.txt

  Scenario: JsonNamingStrategy.SnakeCaseが使われていないことの確認
    Tool: Bash
    Preconditions: 全実装ファイルが存在
    Steps:
      1. `grep -r "SnakeCase\|JsonNamingStrategy" src/main/` を実行
      2. 結果が空であることを確認
    Expected Result: マッチなし（SnakeCaseが使用されていない）
    Failure Indicators: SnakeCaseまたはJsonNamingStrategyへの参照が見つかる
    Evidence: .sisyphus/evidence/task-2-no-snake-case.txt
  ```

  **Commit**: YES
  - Message: `feat(serialization): add Json config and custom serializers for union types`
  - Files: `src/main/kotlin/dev/toliner/openrouter/serialization/`, `src/test/kotlin/dev/toliner/openrouter/serialization/`
  - Pre-commit: `./gradlew test --tests "dev.toliner.openrouter.serialization.*"`

- [x] 3. sealed例外階層

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/error/OpenRouterExceptionTest.kt` を作成
    - 各HTTPステータスコード（400, 401, 402, 403, 408, 429, 502, 503）に対応する例外型のテスト
    - エラーレスポンスJSONからの例外生成テスト
    - when式での網羅性テスト（sealed classなので全サブクラスをカバー）
    - retryableかどうかの判定テスト（429, 502, 503はretryable）
    - StreamError、InBandErrorのテスト
  - `src/test/kotlin/dev/toliner/openrouter/error/ErrorBodySerializationTest.kt` を作成
    - ErrorBody JSONのデシリアライゼーションテスト
    - 不完全なエラーレスポンスの処理テスト

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/error/ErrorBody.kt` を作成
    ```kotlin
    @Serializable
    data class ErrorBody(
        val message: String,
        val code: Int? = null,
        @SerialName("provider_error") val providerError: ProviderError? = null,
    )

    @Serializable
    data class ProviderError(
        val message: String? = null,
        val code: Int? = null,
    )

    @Serializable
    data class ErrorResponse(
        val error: ErrorBody
    )
    ```
  - `src/main/kotlin/dev/toliner/openrouter/error/OpenRouterException.kt` を作成（上記Interfaces参照）
  - HTTPステータスコードから適切な例外サブクラスへの変換関数を作成:
    ```kotlin
    fun errorFromStatus(statusCode: Int, body: ErrorBody, retryAfter: Int? = null): OpenRouterException
    ```
  - retryable判定プロパティ:
    ```kotlin
    val OpenRouterException.isRetryable: Boolean
    ```

  **Must NOT do**:
  - OpenRouterの実際のエラーコードにない仮想的な例外型を作らない
  - 汎用的すぎるエラーハンドリング（全てをExceptionでキャッチ等）をしない

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: データクラスとsealed classの定義。パターンが明確。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2)
  - **Blocks**: Tasks 8, 9, 10, 11, 12, 13
  - **Blocked By**: Task 1

  **References**:

  **External References**:
  - OpenRouter APIのエラーコード: 400 Bad Request, 401 Unauthorized (無効なAPIキー), 402 Payment Required (クレジット不足), 403 Forbidden (コンテンツポリシー違反), 408 Request Timeout, 429 Rate Limit (retryable, Retry-Afterヘッダー付き), 502 Bad Gateway (retryable), 503 Service Unavailable (retryable)。これ以外のステータスコード（500, 504, 524, 529等）は `UnknownError` にマップする。
  - retryable判定: 429, 500, 502, 503, 504, 524, 529はリトライ可能

  **WHY Each Reference Matters**:
  - 各HTTPエラーコードに固有の意味があり（例: 402はクレジット不足、429はレート制限）、ユーザーがwhen式で適切に処理できる必要がある

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/error/OpenRouterExceptionTest.kt`
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/error/ErrorBodySerializationTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.error.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: 全HTTPステータスコードの例外マッピング
    Tool: Bash
    Preconditions: OpenRouterException階層とerrorFromStatus()が実装済み
    Steps:
      1. `./gradlew test --tests "dev.toliner.openrouter.error.OpenRouterExceptionTest"` を実行
      2. 400, 401, 402, 403, 408, 429, 502, 503, および未知のステータスコードの全テストがPASSすることを確認
    Expected Result: 全テストPASS
    Failure Indicators: when式で非網羅、retryable判定の誤り
    Evidence: .sisyphus/evidence/task-3-exception-hierarchy.txt

  Scenario: ErrorBody JSONデシリアライゼーション
    Tool: Bash
    Preconditions: ErrorBody, ErrorResponse型が@Serializable
    Steps:
      1. `./gradlew test --tests "dev.toliner.openrouter.error.ErrorBodySerializationTest"` を実行
      2. 完全なエラーJSON、provider_error付き、最小限のJSONの全パターンがPASS
    Expected Result: 全デシリアライゼーションパターンで正しい値が取得できる
    Failure Indicators: 不完全JSONで例外、フィールド欠損
    Evidence: .sisyphus/evidence/task-3-error-serialization.txt
  ```

  **Commit**: YES
  - Message: `feat(error): add sealed exception hierarchy`
  - Files: `src/main/kotlin/dev/toliner/openrouter/error/`, `src/test/kotlin/dev/toliner/openrouter/error/`
  - Pre-commit: `./gradlew test --tests "dev.toliner.openrouter.error.*"`

- [x] 4. Chat Completions リクエスト/レスポンス型

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/chat/ChatCompletionTypesTest.kt` を作成
    - `ChatCompletionRequest` のシリアライゼーションテスト:
      - 最小限リクエスト（model + messages のみ）
      - フルリクエスト（全フィールド指定: temperature, max_tokens, top_p, stream, stop, tools, tool_choice, response_format, provider, trace等）
      - messages配列: SystemMessage, UserMessage (text content), UserMessage (multipart content), AssistantMessage, ToolMessage
    - `ChatCompletionResponse` のデシリアライゼーションテスト:
      - 通常レスポンス（choices[0].message.content）
      - Tool Call付きレスポンス（choices[0].message.tool_calls）
      - usage情報の確認（prompt_tokens, completion_tokens, total_tokens）
    - `ChatCompletionChunk` のデシリアライゼーションテスト:
      - 通常チャンク（delta.content）
      - Tool Callチャンク（delta.tool_calls）
      - usageチャンク（choices: [], usage: {...}）
      - finish_reason付きチャンク
    - プロパティベーステスト: リクエスト型のシリアライゼーション往復

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/chat/` パッケージ以下に型を定義:
    - `ChatCompletionRequest.kt`: リクエスト型（@Serializable, 全フィールドに@SerialName）
    - `ChatCompletionResponse.kt`: レスポンス型
    - `ChatCompletionChunk.kt`: ストリーミングチャンク型
    - `Message.kt`: メッセージ型（sealed class: System, User, Assistant, Tool）
    - `Tool.kt`: ツール定義型（FunctionTool, FunctionCall）
    - `ResponseFormat.kt`: レスポンスフォーマット型
    - `ProviderPreferences.kt`: プロバイダールーティング設定型
    - `Usage.kt`: トークン使用量型

  主要なリクエストフィールド（全て含めること）:
  - model, messages, temperature, max_tokens, top_p, top_k, frequency_penalty, presence_penalty, repetition_penalty, seed
  - stop (StringOrArray), stream, tools, tool_choice (ToolChoice), response_format
  - provider (ProviderPreferences: order, allow_fallbacks, require_parameters, data_collection, sort, preferred_min_throughput, ignore)
  - trace (Trace: trace_id, span_name 等)
  - transforms (list), route, models (list — フォールバックモデル)

  **Must NOT do**:
  - エンドポイント呼び出し関数はこのタスクでは作らない（型定義のみ）
  - レスポンスのパース以外のロジックを含めない

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 多数のデータクラス定義とシリアライゼーションテスト。型定義の量が多い。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 7)
  - **Blocks**: Tasks 8, 14, 16
  - **Blocked By**: Task 2

  **References**:

  **Pattern References**:
  - Task 2で作成した `OpenRouterJson` — 全シリアライゼーションテストでこのJsonインスタンスを使用する
  - Task 2で作成した `StringOrArray`, `Content`, `ToolChoice` シリアライザー — リクエスト/レスポンス型のフィールドで使用する

  **External References**:
  - OpenRouter chat/completions APIドキュメント: リクエスト/レスポンスの全フィールド定義。特にproviderオブジェクトのフィールド（order: list of strings, allow_fallbacks: boolean, require_parameters: list of strings, data_collection: "deny"|"allow", sort: list of objects with model/order, preferred_min_throughput: number, ignore: list of strings）が重要。
  - traceオブジェクト: trace_id (string), span_name (string) 等のオブザーバビリティ用フィールド。
  - OpenAI互換のchat completionsフォーマット: messages配列の各ロール（system, user, assistant, tool）、tool_calls配列（id, type, function: {name, arguments}）、usage（prompt_tokens, completion_tokens, total_tokens, cost 等）。

  **WHY Each Reference Matters**:
  - Task 2のシリアライザーはこの型定義で実際にフィールドアノテーションとして使用される
  - OpenRouter固有フィールド（provider, trace, transforms, route, models）はOpenAI互換APIにないため見落としやすい

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/l1/chat/ChatCompletionTypesTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.chat.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: 最小限リクエストのシリアライゼーション
    Tool: Bash
    Preconditions: ChatCompletionRequest型が@Serializable
    Steps:
      1. テストで model="openai/gpt-4o", messages=[UserMessage("Hello")] のリクエストをJSON化
      2. JSONに "model":"openai/gpt-4o" と "messages" 配列が含まれることを確認
      3. temperature等のオプションフィールドがJSONに含まれないこと（explicitNulls=false）を確認
    Expected Result: 最小限のJSONが生成される
    Failure Indicators: 不要なnullフィールドがJSON出力に含まれる
    Evidence: .sisyphus/evidence/task-4-minimal-request.txt

  Scenario: フルレスポンスのデシリアライゼーション
    Tool: Bash
    Preconditions: ChatCompletionResponse型が@Serializable
    Steps:
      1. OpenRouter実レスポンス形式のJSON文字列をデシリアライズ
      2. choices[0].message.content の値を確認
      3. usage.prompt_tokens, usage.completion_tokens の値を確認
      4. 未知のフィールドが含まれていてもエラーにならないこと（ignoreUnknownKeys）を確認
    Expected Result: 全フィールドが正しくマッピングされ、未知フィールドは無視される
    Failure Indicators: デシリアライゼーション例外、フィールド値の不一致
    Evidence: .sisyphus/evidence/task-4-full-response.txt

  Scenario: ストリーミングチャンクのデシリアライゼーション
    Tool: Bash
    Preconditions: ChatCompletionChunk型が@Serializable
    Steps:
      1. 通常のdelta.contentチャンクをデシリアライズ
      2. usageチャンク（choices: [], usage: {...}）をデシリアライズ
      3. finish_reason付きチャンクをデシリアライズ
    Expected Result: 全チャンクパターンが正しくデシリアライズされる
    Failure Indicators: choices空配列でのパースエラー、delta.contentのnullハンドリング失敗
    Evidence: .sisyphus/evidence/task-4-chunk-deserialization.txt
  ```

  **Commit**: YES
  - Message: `feat(l1/chat): add Chat Completion request/response types`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/chat/`, `src/test/kotlin/dev/toliner/openrouter/l1/chat/`
  - Pre-commit: `./gradlew test --tests "dev.toliner.openrouter.l1.chat.*"`

- [x] 5. SSEストリーミング解析・Flow変換

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/streaming/SseParserTest.kt` を作成
    - 正常チャンク: `data: {"id":"gen-123","choices":[{"delta":{"content":"Hello"}}]}` → ChatCompletionChunk
    - コメント行: `: OPENROUTER PROCESSING` → 無視（Flowに何も出力しない）
    - `data: [DONE]` → Flow完了（onComplete）
    - usageチャンク: `data: {"id":"gen-123","choices":[],"usage":{...}}` → 正しくパース
    - debugチャンク: `choices: []` + `debug` フィールド → 無視またはスキップ
    - 空data行: `data: ` → 無視
    - mid-streamエラー: `finish_reason: "error"` + `choices[i].error` → StreamError例外をFlowにemit
    - 複数チャンクの連続処理テスト
    - in-bandエラー検出: 非ストリーミングレスポンスで HTTP 200 + `choices[i].error` → InBandError例外

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/streaming/SseParser.kt` を作成
    - `Flow<ServerSentEvent>` → `Flow<ChatCompletionChunk>` 変換関数:
      ```kotlin
      fun Flow<ServerSentEvent>.toChatCompletionChunks(json: Json): Flow<ChatCompletionChunk>
      ```
    - 内部処理:
      1. ServerSentEvent.data が null または空 → スキップ
      2. data が "[DONE]" → Flow完了
      3. data をJSONパース → ChatCompletionChunk
      4. chunk.choices が空でusageあり → usageチャンクとしてemit
      5. chunk.choices[i].finish_reason が "error" → StreamError投げる
      6. それ以外 → 通常チャンクとしてemit
  - `src/main/kotlin/dev/toliner/openrouter/streaming/InBandErrorDetector.kt` を作成
    - 非ストリーミングレスポンスのin-bandエラー検出:
      ```kotlin
      fun ChatCompletionResponse.checkInBandError(): ChatCompletionResponse
      ```
      choices[i].error が非nullの場合にInBandError例外を投げる

  **Must NOT do**:
  - MockEngineやtestApplicationでSSEのタイミングテストをしない（KTOR-7910回避）
  - Ktor SSEプラグインの設定はこのタスクでは行わない（Task 8で統合）
  - HTTPクライアント呼び出しを含めない（純粋なFlow変換ロジックのみ）

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: SSEプロトコルのエッジケースが多く（コメント、debug、mid-streamエラー等）、正確なパース処理が必要
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 6, 7)
  - **Blocks**: Tasks 8, 15
  - **Blocked By**: Task 2

  **References**:

  **Pattern References**:
  - Task 2で作成した `OpenRouterJson` — SSEデータのJSONパースに使用
  - Task 3で作成した `OpenRouterException.StreamError`, `OpenRouterException.InBandError` — エラー時に投げる例外型
  - Task 4で作成した `ChatCompletionChunk` — パース結果の型

  **External References**:
  - OpenRouter SSEプロトコル:
    - 各イベントは `data: {JSON}\n\n` 形式
    - コメント行は `: ` で始まる（例: `: OPENROUTER PROCESSING`）。SSE仕様でコメント行はクライアントに無視される。
    - `data: [DONE]` がストリーム終了シグナル
    - usageチャンク: `{"id":"gen-xxx","choices":[],"usage":{"prompt_tokens":10,"completion_tokens":20,"total_tokens":30}}` — choicesが空でusageのみ。ストリーム終了直前に送信される。
    - debugチャンク: `debug.echo_upstream_body = true` 設定時、最初のチャンクに `choices: []` と `debug` フィールドが含まれる。
    - mid-streamエラー: HTTP 200でストリーム開始後にエラーが発生した場合、`finish_reason: "error"` と `choices[i].error` オブジェクトが含まれるチャンクが送信される。
  - Ktor SSE: `ServerSentEvent` データクラスには `data: String?`, `event: String?`, `id: String?`, `comments: String?`, `retry: Long?` フィールドがある。

  **WHY Each Reference Matters**:
  - SSEパースは本ライブラリの中核機能の1つ。エッジケース（コメント、debug、mid-stream error、usage-only chunk）を正しく処理しないとストリーミング利用時に予期しないエラーやデータ欠損が発生する。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/streaming/SseParserTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.streaming.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: 正常なストリーミングフロー変換
    Tool: Bash
    Preconditions: SseParser.toChatCompletionChunks()が実装済み
    Steps:
      1. 手作りのFlow<ServerSentEvent>を作成（3つの通常チャンク + usageチャンク + [DONE]）
      2. toChatCompletionChunks()でFlow<ChatCompletionChunk>に変換
      3. toList()で収集し、4つのチャンク（3通常 + 1usage）が取得できることを確認
      4. [DONE]後にFlowが正常完了することを確認
    Expected Result: 4チャンクが正しい順序で取得され、Flowが正常完了
    Failure Indicators: チャンク数不一致、JSONパースエラー、Flowが完了しない
    Evidence: .sisyphus/evidence/task-5-normal-stream.txt

  Scenario: コメント行とdebugチャンクのスキップ
    Tool: Bash
    Preconditions: SseParser実装済み
    Steps:
      1. コメント行 `: OPENROUTER PROCESSING` を含むFlowを作成
      2. debugチャンク（choices:[] + debugフィールド）を含むFlowを作成
      3. 変換後のFlowにこれらのイベントが含まれないことを確認
    Expected Result: コメントとdebugチャンクはFlow出力に含まれない
    Failure Indicators: 余計なチャンクがemitされる、パースエラー
    Evidence: .sisyphus/evidence/task-5-skip-comments.txt

  Scenario: mid-streamエラーの検出
    Tool: Bash
    Preconditions: SseParser実装済み
    Steps:
      1. finish_reason:"error" と choices[0].error を含むSSEチャンクのFlowを作成
      2. toChatCompletionChunks()で変換
      3. Flow収集時にStreamError例外がスローされることを確認
    Expected Result: StreamError例外がスローされ、エラーメッセージが正しい
    Failure Indicators: 例外なしで通過、異なる例外型
    Evidence: .sisyphus/evidence/task-5-midstream-error.txt
  ```

  **Commit**: YES
  - Message: `feat(streaming): add SSE parsing and flow transformation`
  - Files: `src/main/kotlin/dev/toliner/openrouter/streaming/`, `src/test/kotlin/dev/toliner/openrouter/streaming/`
  - Pre-commit: `./gradlew test --tests "dev.toliner.openrouter.streaming.*"`

- [x] 6. Models型 + エンドポイント

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/models/ModelTypesTest.kt` を作成
    - `Model` のデシリアライゼーションテスト（id, name, description, pricing, context_length, architecture等）
    - `ModelList` レスポンスのデシリアライゼーション（data配列）
    - `ModelEndpoint` のデシリアライゼーション
    - `ModelsCount` のデシリアライゼーション
    - pricing内のフィールド（prompt, completion, image, request — 全て文字列型の数値）
    - architecture内のフィールド（modality, tokenizer, instruct_type）
    - top_provider内のフィールド（context_length, max_completion_tokens, is_moderated）

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/models/` パッケージ以下に型を定義:
    - `Model.kt`: モデル情報型（id, name, description, pricing, context_length, architecture, top_provider, per_request_limits等）
    - `ModelList.kt`: `data: List<Model>` レスポンス型
    - `ModelsCount.kt`: モデル数レスポンス型
    - `ModelEndpoint.kt`: モデルエンドポイント情報型
    - `ZdrEndpoint.kt`: ZDR影響プレビュー型
    - `EmbeddingModel.kt`: 埋め込みモデル情報型（GET /embeddings/models用）
  - 全フィールドに `@SerialName` を使用

  対象エンドポイントの型:
  1. GET /models → `ModelList` (data: List<Model>)
  2. GET /models/count → `ModelsCount`
  3. GET /models/user → `ModelList`
  4. GET /models/{author}/{slug}/endpoints → `List<ModelEndpoint>`
  5. GET /endpoints/zdr → `List<ZdrEndpoint>`
  6. GET /embeddings/models → レスポンス型

  **Must NOT do**:
  - HTTPクライアント呼び出し関数はこのタスクでは作らない（型定義のみ、エンドポイント関数はTask 8以降でクライアントに統合）
  - 不要なバリデーションロジックを含めない

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 多数のデータクラスとシリアライゼーションテスト
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5, 7)
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:

  **Pattern References**:
  - Task 2の `OpenRouterJson` — テストで使用

  **External References**:
  - OpenRouter GET /models レスポンス: `{ "data": [{ "id": "openai/gpt-4o", "name": "GPT-4o", "description": "...", "pricing": { "prompt": "0.000005", "completion": "0.000015", "image": "0.0", "request": "0.0" }, "context_length": 128000, "architecture": { "modality": "text+image->text", "tokenizer": "GPT", "instruct_type": "none" }, "top_provider": { "context_length": 128000, "max_completion_tokens": 16384, "is_moderated": true }, "per_request_limits": { "prompt_tokens": null, "completion_tokens": null } }] }`
  - pricing のフィールドは **文字列型の数値** ("0.000005") であることに注意。Doubleではない。

  **WHY Each Reference Matters**:
  - pricingが文字列型であることを見落とすとデシリアライゼーションが失敗する。これはOpenRouter APIの実際の仕様。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/l1/models/ModelTypesTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.models.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Model型の完全なデシリアライゼーション
    Tool: Bash
    Preconditions: Model型が@Serializable
    Steps:
      1. OpenRouter実レスポンス形式のJSON文字列をデシリアライズ
      2. id, name, pricing.prompt (文字列), context_length (整数) を確認
      3. architecture.modality, top_provider.is_moderated を確認
    Expected Result: 全フィールドが正しい型と値でマッピングされる
    Failure Indicators: pricing文字列のパースエラー、nullableフィールドの例外
    Evidence: .sisyphus/evidence/task-6-model-types.txt

  Scenario: 未知フィールドを含むレスポンスの処理
    Tool: Bash
    Preconditions: OpenRouterJson.ignoreUnknownKeys = true
    Steps:
      1. 将来追加されうる未知フィールドを含むJSON文字列をデシリアライズ
      2. 例外なく処理されることを確認
    Expected Result: 未知フィールドは無視され、既知フィールドは正しくパースされる
    Failure Indicators: UnknownKeyException
    Evidence: .sisyphus/evidence/task-6-unknown-fields.txt
  ```

  **Commit**: YES
  - Message: `feat(l1/models): add model types`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/models/`, `src/test/kotlin/dev/toliner/openrouter/l1/models/`
  - Pre-commit: `./gradlew test --tests "dev.toliner.openrouter.l1.models.*"`

- [x] 7. Embeddings型 + エンドポイント

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/embeddings/EmbeddingTypesTest.kt` を作成
    - `EmbeddingRequest` のシリアライゼーション（model, input: String|Array<String>）
    - `EmbeddingResponse` のデシリアライゼーション（data配列、各要素にembedding: List<Double>）
    - usage情報のデシリアライゼーション

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/embeddings/` パッケージ:
    - `EmbeddingRequest.kt`: model, input (StringOrArrayシリアライザー使用), encoding_format等
    - `EmbeddingResponse.kt`: data: List<EmbeddingData>, usage
    - `EmbeddingData.kt`: embedding: List<Double>, index: Int, object: String

  **Must NOT do**:
  - HTTPクライアント呼び出し関数はこのタスクでは作らない（型定義のみ）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 比較的少数の型定義。Embeddingsは単純なリクエスト/レスポンスペア。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 4, 5, 6)
  - **Blocks**: Task 8
  - **Blocked By**: Task 2

  **References**:

  **Pattern References**:
  - Task 2の `StringOrArray` シリアライザー — `input` フィールドが `String | Array<String>` のUnion型
  - Task 2の `OpenRouterJson` — テストで使用

  **External References**:
  - OpenRouter POST /embeddings: OpenAI互換API。リクエスト: `{ "model": "openai/text-embedding-3-small", "input": "Hello" }` または `{ "model": "...", "input": ["Hello", "World"] }`。レスポンス: `{ "data": [{ "embedding": [0.1, -0.2, ...], "index": 0, "object": "embedding" }], "model": "...", "usage": { "prompt_tokens": 2, "total_tokens": 2 } }`

  **WHY Each Reference Matters**:
  - inputフィールドのUnion型処理にTask 2のStringOrArrayシリアライザーを再利用できる

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/l1/embeddings/EmbeddingTypesTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.embeddings.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Embeddingリクエストのシリアライゼーション
    Tool: Bash
    Preconditions: EmbeddingRequest型が@Serializable
    Steps:
      1. 単一文字列inputでリクエストをJSON化
      2. 配列inputでリクエストをJSON化
      3. 両方のJSONが正しい形式であることを確認
    Expected Result: input がString/Arrayどちらでも正しくシリアライズされる
    Failure Indicators: StringOrArrayシリアライザーのエラー
    Evidence: .sisyphus/evidence/task-7-embedding-request.txt
  ```

  **Commit**: YES
  - Message: `feat(l1/embeddings): add embedding types`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/embeddings/`, `src/test/kotlin/dev/toliner/openrouter/l1/embeddings/`
  - Pre-commit: `./gradlew test --tests "dev.toliner.openrouter.l1.embeddings.*"`

- [ ] 8. OpenRouterClient基盤 + Chat Completionsエンドポイント統合

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/client/OpenRouterClientTest.kt` を作成
    - MockEngineを使ったChat Completions (non-stream) エンドポイントテスト:
      - 正しいURL (baseUrl + "/chat/completions") にPOSTされること
      - Authorizationヘッダーが "Bearer {apiKey}" であること
      - Content-Type が application/json であること
      - HTTP-Referer と X-Title ヘッダーが設定時に送信されること
      - リクエストボディが正しいJSON形式であること
      - レスポンスが正しくChatCompletionResponseにデシリアライズされること
    - MockEngineを使ったストリーミングエンドポイントテスト:
      - stream=true のリクエストが送信されること
      - （SSEの実際のFlow変換はTask 5でテスト済みなので、ここではHTTPレベルの設定確認のみ）
    - エラーレスポンスのテスト:
      - HTTP 401 → Unauthorized例外
      - HTTP 429 → TooManyRequests例外（Retry-Afterヘッダー取得）
      - HTTP 200 + in-bandエラー → InBandError例外
    - Models, Embeddings エンドポイントのMockEngineテスト
    - OpenRouterClient.close() でHttpClientが閉じられること

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/client/OpenRouterClient.kt` を作成
    - HttpClientEngine をコンストラクタ注入
    - 内部でHttpClientを構築:
      ```kotlin
      private val httpClient = HttpClient(engine) {
          install(ContentNegotiation) { json(OpenRouterJson) }
          expectSuccess = false  // エラーハンドリングはコールサイトで行う
      }
      ```
    - 各APIカテゴリをプロパティとして公開（chat, models, embeddings等）
    - 共通リクエスト処理:
      - Authorizationヘッダー設定
      - HTTP-Referer、X-Titleヘッダー設定（オプション）
      - レスポンスステータスコード確認 → 適切な例外変換
  - `src/main/kotlin/dev/toliner/openrouter/client/OpenRouterConfig.kt` を作成
  - `src/main/kotlin/dev/toliner/openrouter/client/ChatApi.kt` を作成
    - `suspend fun complete(request: ChatCompletionRequest): ChatCompletionResponse`
    - `fun stream(request: ChatCompletionRequest): Flow<ChatCompletionChunk>`
    - in-bandエラー検出をcomplete()に統合
  - `src/main/kotlin/dev/toliner/openrouter/client/ModelsApi.kt` を作成
    - `suspend fun list(): ModelList`
    - `suspend fun count(): ModelsCount`
    - `suspend fun userModels(): ModelList`
    - `suspend fun endpoints(author: String, slug: String): List<ModelEndpoint>`
    - `suspend fun zdrEndpoints(): List<ZdrEndpoint>`
    - `suspend fun embeddingModels(): List<EmbeddingModel>` (GET /embeddings/models)
  - `src/main/kotlin/dev/toliner/openrouter/client/EmbeddingsApi.kt` を作成
    - `suspend fun create(request: EmbeddingRequest): EmbeddingResponse`
  - テストユーティリティ:
    - `src/test/kotlin/dev/toliner/openrouter/testutil/MockEngineHelpers.kt` — MockEngine構築ヘルパー

  **Must NOT do**:
  - HttpResponseValidatorでin-bandエラーを検出しない（ボディ消費問題を回避）
  - CIOエンジンに依存しない（エンジン注入パターン）
  - リトライロジックはこのタスクでは実装しない
  - SSEプラグインの設定はストリーミング統合時に実装する

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: クライアント全体の設計と複数エンドポイントの統合。MockEngineテストの設計が重要。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (this is the first task, others depend on it)
  - **Blocks**: Tasks 9, 10, 11, 12, 13, 14, 15, 16, 17
  - **Blocked By**: Tasks 3, 4, 5, 6, 7

  **References**:

  **Pattern References**:
  - Task 2 `OpenRouterJson` — HttpClient の ContentNegotiation に渡す Json インスタンス
  - Task 3 `OpenRouterException` 階層、`errorFromStatus()` — HTTPステータスコードからの例外変換
  - Task 4 `ChatCompletionRequest`, `ChatCompletionResponse`, `ChatCompletionChunk` — Chat API型
  - Task 5 `toChatCompletionChunks()` — ストリーミング時のFlow変換
  - Task 5 `checkInBandError()` — non-stream時のin-bandエラー検出
  - Task 6 `Model`, `ModelList`, etc. — Models API型
  - Task 7 `EmbeddingRequest`, `EmbeddingResponse` — Embeddings API型

  **External References**:
  - Ktor HttpClient構築: `HttpClient(engine) { install(...) }` パターン。engine引数にHttpClientEngineを渡す。テスト時はMockEngine、本番時はCIOを渡す。
  - Ktor ContentNegotiation: `install(ContentNegotiation) { json(OpenRouterJson) }` でkotlinx.serialization Jsonインスタンスを登録。リクエスト/レスポンスの自動シリアライゼーション/デシリアライゼーションが有効になる。
  - Ktor SSEプラグイン: `install(SSE)` で有効化。`client.sse(url) { incoming.collect { ... } }` でSSEイベントを受信。`incoming` は `Flow<ServerSentEvent>`。
  - MockEngine使用法: `MockEngine { requestData -> respond(content, status, headers) }` でレスポンスをプログラム定義。requestDataでリクエストを検証できる。

  **WHY Each Reference Matters**:
  - 全Wave 1-2のタスク成果物がここで統合される。正しい依存関係の接続が重要。
  - MockEngineテストパターンはこの後の全管理系APIタスクの基盤となる。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/client/OpenRouterClientTest.kt`
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/testutil/MockEngineHelpers.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.client.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Chat Completions non-stream呼び出し
    Tool: Bash
    Preconditions: OpenRouterClient, ChatApi が実装済み、MockEngine設定済み
    Steps:
      1. MockEngineで POST /chat/completions に対して正常レスポンスを返すよう設定
      2. OpenRouterClient.chat.complete(request) を呼び出し
      3. MockEngineでリクエストを検証: URL, メソッド, ヘッダー（Authorization, Content-Type）
      4. レスポンスが正しくChatCompletionResponseにデシリアライズされることを確認
    Expected Result: 正しいURLにPOST、正しいヘッダー付き、レスポンスが型安全にデシリアライズ
    Failure Indicators: URL不一致、ヘッダー欠落、デシリアライゼーションエラー
    Evidence: .sisyphus/evidence/task-8-chat-completions.txt

  Scenario: HTTPエラーレスポンスの例外変換
    Tool: Bash
    Preconditions: errorFromStatus()が統合済み
    Steps:
      1. MockEngineでHTTP 401レスポンスを返すよう設定
      2. chat.complete()呼び出し時にUnauthorized例外がスローされることを確認
      3. MockEngineでHTTP 429 + Retry-After: 30 ヘッダーを返すよう設定
      4. TooManyRequests例外がスローされ、retryAfterが30であることを確認
    Expected Result: 各HTTPステータスコードが適切な例外サブクラスに変換される
    Failure Indicators: 汎用例外がスローされる、retryAfterがnull
    Evidence: .sisyphus/evidence/task-8-error-handling.txt

  Scenario: in-bandエラーの検出
    Tool: Bash
    Preconditions: checkInBandError()が統合済み
    Steps:
      1. MockEngineでHTTP 200 + choices[0].error付きレスポンスを返すよう設定
      2. chat.complete()呼び出し時にInBandError例外がスローされることを確認
    Expected Result: HTTP 200でもin-bandエラーが検出されて例外がスローされる
    Failure Indicators: エラーが無視されて正常レスポンスとして返される
    Evidence: .sisyphus/evidence/task-8-inband-error.txt

  Scenario: Models API呼び出し
    Tool: Bash
    Preconditions: ModelsApi が実装済み
    Steps:
      1. MockEngineでGET /models に対してモデルリストJSONを返すよう設定
      2. client.models.list() を呼び出し
      3. レスポンスのdata配列が正しくデシリアライズされることを確認
    Expected Result: ModelListが正しく取得される
    Failure Indicators: URL不一致、デシリアライゼーションエラー
    Evidence: .sisyphus/evidence/task-8-models-api.txt
  ```

  **Commit**: YES
  - Message: `feat(client): add OpenRouterClient with engine injection and core endpoints`
  - Files: `src/main/kotlin/dev/toliner/openrouter/client/`, `src/test/kotlin/dev/toliner/openrouter/client/`, `src/test/kotlin/dev/toliner/openrouter/testutil/`
  - Pre-commit: `./gradlew test`

- [ ] 9. Generation + Account (credits, key info, activity) 型 + エンドポイント

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/generation/GenerationTypesTest.kt`
    - `Generation` のデシリアライゼーション（id, model, usage, tokens, streamed, generation_time, native_tokens_prompt, native_tokens_completion等）
  - `src/test/kotlin/dev/toliner/openrouter/l1/account/AccountTypesTest.kt`
    - `Credits` のデシリアライゼーション（total_credits, total_usage等）
    - `KeyInfo` のデシリアライゼーション（label, usage, limit, is_free_tier, rate_limit等）
    - `Activity` のデシリアライゼーション（data配列: 日付ごとの利用統計）

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/generation/Generation.kt`
  - `src/main/kotlin/dev/toliner/openrouter/l1/account/Credits.kt`
  - `src/main/kotlin/dev/toliner/openrouter/l1/account/KeyInfo.kt`
  - `src/main/kotlin/dev/toliner/openrouter/l1/account/Activity.kt`
  - `src/main/kotlin/dev/toliner/openrouter/client/GenerationApi.kt`:
    - `suspend fun get(id: String): Generation`
  - `src/main/kotlin/dev/toliner/openrouter/client/AccountApi.kt`:
    - `suspend fun credits(): Credits`
    - `suspend fun keyInfo(): KeyInfo`
    - `suspend fun activity(): Activity`
  - MockEngineテストでエンドポイントURL（GET /generation?id={id}, GET /credits, GET /key, GET /activity）を検証

  **Must NOT do**:
  - リトライロジックを含めない
  - Management API Keyの検証ロジックは含めない（キーの種類はユーザー責任）

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 4つのエンドポイントの型定義とMockEngineテスト
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 10, 11, 12, 13)
  - **Blocks**: None
  - **Blocked By**: Tasks 3, 8

  **References**:

  **Pattern References**:
  - Task 8の `OpenRouterClient`, `MockEngineHelpers` — エンドポイント追加パターンとテストヘルパー
  - Task 3の例外階層 — エラーレスポンス処理

  **External References**:
  - GET /generation?id={id}: レスポンスにmodel, usage, tokens, streamed (boolean), generation_time (ms), created_at等が含まれる
  - GET /credits: `{ "total_credits": 10.0, "total_usage": 3.5 }` 等
  - GET /key: 現在のAPIキー情報（label, usage, limit, is_free_tier, rate_limit: {requests, interval}）
  - GET /activity: Management Key必要。日付ごとの利用統計配列。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.generation.*"` → PASS
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.account.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Generation取得
    Tool: Bash
    Steps:
      1. MockEngineでGET /generation?id=gen-123 にJSONレスポンスを設定
      2. client.generation.get("gen-123") を呼び出し
      3. idとmodel名が正しいことを確認
    Expected Result: Generation型が正しくデシリアライズされる
    Evidence: .sisyphus/evidence/task-9-generation.txt

  Scenario: Credits, KeyInfo, Activity取得
    Tool: Bash
    Steps:
      1. 各エンドポイントにMockEngineレスポンスを設定
      2. client.account.credits(), keyInfo(), activity() をそれぞれ呼び出し
      3. 各レスポンスの主要フィールドを検証
    Expected Result: 全アカウントAPIが正しく動作する
    Evidence: .sisyphus/evidence/task-9-account-apis.txt
  ```

  **Commit**: YES
  - Message: `feat(l1/account): add generation, credits, key info, activity endpoints`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/generation/`, `src/main/kotlin/dev/toliner/openrouter/l1/account/`, `src/main/kotlin/dev/toliner/openrouter/client/GenerationApi.kt`, `src/main/kotlin/dev/toliner/openrouter/client/AccountApi.kt`
  - Pre-commit: `./gradlew test`

- [ ] 10. Key管理CRUD

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/keys/KeyTypesTest.kt`
    - `ApiKey` のデシリアライゼーション（hash, name, label, usage, limit, disabled, created_at等）
    - `CreateKeyRequest` のシリアライゼーション（name, label, limit, disabled等）
    - `UpdateKeyRequest` のシリアライゼーション（PATCHリクエスト、部分更新）
    - API Key一覧レスポンスのデシリアライゼーション
  - `src/test/kotlin/dev/toliner/openrouter/client/KeysApiTest.kt`
    - MockEngineテスト: GET /keys, POST /keys, GET /keys/{hash}, PATCH /keys/{hash}, DELETE /keys/{hash}

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/keys/ApiKey.kt`: APIキー型
  - `src/main/kotlin/dev/toliner/openrouter/l1/keys/CreateKeyRequest.kt`: キー作成リクエスト
  - `src/main/kotlin/dev/toliner/openrouter/l1/keys/UpdateKeyRequest.kt`: キー更新リクエスト
  - `src/main/kotlin/dev/toliner/openrouter/client/KeysApi.kt`:
    - `suspend fun list(): List<ApiKey>`
    - `suspend fun create(request: CreateKeyRequest): ApiKey`
    - `suspend fun get(hash: String): ApiKey`
    - `suspend fun update(hash: String, request: UpdateKeyRequest): ApiKey`
    - `suspend fun delete(hash: String)`

  **Must NOT do**:
  - APIキーのバリデーションロジックを含めない

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 5つのCRUDエンドポイントとそのテスト
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 9, 11, 12, 13)
  - **Blocks**: None
  - **Blocked By**: Tasks 3, 8

  **References**:

  **Pattern References**:
  - Task 8の `OpenRouterClient`, `MockEngineHelpers` — CRUD エンドポイントのテストパターン

  **External References**:
  - GET /keys: Management Key必要。全APIキー一覧。
  - POST /keys: キー作成。リクエスト: `{ "name": "my-key", "label": "Production", "limit": 100 }`
  - PATCH /keys/{hash}: 部分更新。変更するフィールドのみ送信。
  - DELETE /keys/{hash}: キー削除。204 No Content。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.keys.*"` → PASS
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.client.KeysApiTest"` → PASS

  **QA Scenarios:**

  ```
  Scenario: キーCRUD全操作
    Tool: Bash
    Steps:
      1. MockEngineで各CRUDエンドポイントにレスポンスを設定
      2. list(), create(), get(), update(), delete() をそれぞれ呼び出し
      3. 各操作のHTTPメソッドとURLパスを検証
      4. POST/PATCHのリクエストボディJSONを検証
      5. DELETEの204レスポンスが例外なく処理されることを確認
    Expected Result: 全CRUD操作が正しいHTTPリクエストを生成し、レスポンスを処理する
    Failure Indicators: HTTPメソッド不一致、パスパラメータ誤り
    Evidence: .sisyphus/evidence/task-10-keys-crud.txt
  ```

  **Commit**: YES
  - Message: `feat(l1/keys): add key management CRUD`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/keys/`, `src/main/kotlin/dev/toliner/openrouter/client/KeysApi.kt`
  - Pre-commit: `./gradlew test`

- [ ] 11. Guardrails CRUD + アサインメント

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/guardrails/GuardrailTypesTest.kt`
    - `Guardrail` のシリアライゼーション/デシリアライゼーション
    - `GuardrailAssignment` の型テスト
  - `src/test/kotlin/dev/toliner/openrouter/client/GuardrailsApiTest.kt`
    - 8エンドポイントのMockEngineテスト:
      1. GET /guardrails — 一覧
      2. POST /guardrails — 作成
      3. GET /guardrails/{id} — 取得
      4. PATCH /guardrails/{id} — 更新
      5. DELETE /guardrails/{id} — 削除
      6. GET /guardrails/{id}/assignments — アサインメント一覧
      7. POST /guardrails/{id}/assignments — アサインメント追加
      8. DELETE /guardrails/{id}/assignments/{assignmentId} — アサインメント削除

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/guardrails/Guardrail.kt`: ガードレール型
  - `src/main/kotlin/dev/toliner/openrouter/l1/guardrails/GuardrailAssignment.kt`: アサインメント型
  - `src/main/kotlin/dev/toliner/openrouter/l1/guardrails/CreateGuardrailRequest.kt`
  - `src/main/kotlin/dev/toliner/openrouter/l1/guardrails/UpdateGuardrailRequest.kt`
  - `src/main/kotlin/dev/toliner/openrouter/client/GuardrailsApi.kt`:
    - CRUD: `list()`, `create()`, `get(id)`, `update(id)`, `delete(id)`
    - Assignments: `listAssignments(id)`, `addAssignment(id, request)`, `removeAssignment(id, assignmentId)`

  **Must NOT do**:
  - ガードレールの適用ロジック（推論時の自動適用等）は含めない

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 8エンドポイントの実装。CRUDパターンの繰り返しだが量が多い。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 9, 10, 12, 13)
  - **Blocks**: None
  - **Blocked By**: Tasks 3, 8

  **References**:

  **Pattern References**:
  - Task 10の `KeysApi` — 同じCRUDパターンを踏襲

  **External References**:
  - Guardrails API: Management Key必要。コンテンツポリシーの定義と、特定のAPIキーやモデルへの適用を管理するAPI。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.guardrails.*"` → PASS
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.client.GuardrailsApiTest"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Guardrails全8エンドポイント
    Tool: Bash
    Steps:
      1. MockEngineで各エンドポイントにレスポンスを設定
      2. 全8操作を実行し、HTTPメソッド、パス、リクエストボディを検証
    Expected Result: 全エンドポイントが正しいHTTPリクエストを生成する
    Evidence: .sisyphus/evidence/task-11-guardrails.txt
  ```

  **Commit**: YES
  - Message: `feat(l1/guardrails): add guardrails CRUD and assignments`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/guardrails/`, `src/main/kotlin/dev/toliner/openrouter/client/GuardrailsApi.kt`
  - Pre-commit: `./gradlew test`

- [ ] 12. Providers + OAuth

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/providers/ProviderTypesTest.kt`
    - `Provider` のデシリアライゼーション
  - `src/test/kotlin/dev/toliner/openrouter/l1/auth/OAuthTypesTest.kt`
    - `AuthKeyRequest` / `AuthKeyResponse` のシリアライゼーション
    - `AuthCodeRequest` / `AuthCodeResponse` のシリアライゼーション
  - MockEngineテスト: GET /providers, POST /auth/keys, POST /auth/keys/code

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/providers/Provider.kt`
  - `src/main/kotlin/dev/toliner/openrouter/l1/auth/AuthKey.kt`: OAuth PKCE型
  - `src/main/kotlin/dev/toliner/openrouter/client/ProvidersApi.kt`:
    - `suspend fun list(): List<Provider>`
  - `src/main/kotlin/dev/toliner/openrouter/client/AuthApi.kt`:
    - `suspend fun createKey(request: AuthKeyRequest): AuthKeyResponse`
    - `suspend fun exchangeCode(request: AuthCodeRequest): AuthCodeResponse`

  **Must NOT do**:
  - PKCE code_verifier/code_challengeの生成ヘルパーは含めない（L2スコープ外）

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 3エンドポイント + 型定義
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 9, 10, 11, 13)
  - **Blocks**: None
  - **Blocked By**: Tasks 3, 8

  **References**:

  **Pattern References**:
  - Task 8のクライアントパターン

  **External References**:
  - GET /providers: プロバイダー一覧（name, models等）
  - POST /auth/keys: OAuth PKCEフロー。code_challenge, code_challenge_method 送信 → authorization URL取得
  - POST /auth/keys/code: authorization code → API key 交換

  **Acceptance Criteria**:

  **TDD:**
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.providers.*"` → PASS
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.auth.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Providers + OAuth エンドポイント
    Tool: Bash
    Steps:
      1. MockEngineで各エンドポイントにレスポンスを設定
      2. providers.list(), auth.createKey(), auth.exchangeCode() を実行
      3. リクエスト/レスポンスを検証
    Expected Result: 全3エンドポイントが正しく動作する
    Evidence: .sisyphus/evidence/task-12-providers-oauth.txt
  ```

  **Commit**: YES
  - Message: `feat(l1): add providers and OAuth endpoints`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/providers/`, `src/main/kotlin/dev/toliner/openrouter/l1/auth/`, `src/main/kotlin/dev/toliner/openrouter/client/ProvidersApi.kt`, `src/main/kotlin/dev/toliner/openrouter/client/AuthApi.kt`
  - Pre-commit: `./gradlew test`

- [ ] 13. Responses API (beta) L1

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l1/responses/ResponsesTypesTest.kt`
    - `CreateResponseRequest` のシリアライゼーション（model, input: String|Array<InputItem>, instructions等）
    - `ResponseObject` のデシリアライゼーション
    - input の Union型テスト（String | Array<InputItem>）
    - InputItem の多様な型テスト（message, function_call_output等）

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l1/responses/` パッケージ:
    - `CreateResponseRequest.kt`: リクエスト型
    - `ResponseObject.kt`: レスポンス型
    - `InputItem.kt`: 入力アイテム型（sealed class）
    - `ResponseTool.kt`: ツール定義型
  - `src/main/kotlin/dev/toliner/openrouter/client/ResponsesApi.kt`:
    - `suspend fun create(request: CreateResponseRequest): ResponseObject`
  - `@Beta` または `@ExperimentalOpenRouterApi` アノテーションを定義し、全Responses API型に付与
  - Responses APIは独自のSSEイベント型（response.failed, response.error, error等）を持つが、ストリーミング対応はこのタスクでは行わない（non-streamのみ）

  **Must NOT do**:
  - L2 DSLを作らない
  - ストリーミング対応は含めない（Responses APIのSSEは独自フォーマットで複雑なため）
  - betaの不安定性を考慮し、他のパッケージへの依存を最小限にする

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: beta APIの型定義。Union型が多くシリアライザー実装が必要。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 9, 10, 11, 12)
  - **Blocks**: None
  - **Blocked By**: Tasks 2, 8

  **References**:

  **Pattern References**:
  - Task 2の Union型シリアライザーパターン — input フィールドの String | Array 処理
  - Task 8のクライアントパターン

  **External References**:
  - OpenRouter POST /responses (beta): OpenAI Responses API互換。input フィールドは String または InputItem配列。レスポンスはResponseObject型。SSEストリーミング時は response.created, response.in_progress, response.completed, response.failed, response.error 等のイベント型を使う（Chat CompletionsのSSEとは完全に異なる）。
  - input Union型: `String`（単純テキスト）または `Array<InputItem>`（構造化入力 — message, function_call_output等）

  **Acceptance Criteria**:

  **TDD:**
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l1.responses.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Responses API non-stream呼び出し
    Tool: Bash
    Steps:
      1. MockEngineで POST /responses にレスポンスを設定
      2. client.responses.create(request) を呼び出し
      3. レスポンスが正しくResponseObjectにデシリアライズされることを確認
      4. @ExperimentalOpenRouterApi アノテーションが全公開型に付いていることを確認
    Expected Result: Responses APIが正しく動作し、実験的APIとしてマークされている
    Evidence: .sisyphus/evidence/task-13-responses-api.txt
  ```

  **Commit**: YES
  - Message: `feat(l1/responses): add Responses API (beta) types and endpoint`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l1/responses/`, `src/main/kotlin/dev/toliner/openrouter/client/ResponsesApi.kt`
  - Pre-commit: `./gradlew test`

- [ ] 14. Chat DSLビルダー (non-stream)

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l2/chat/ChatDslTest.kt`
    - DSLビルダーで最小限のリクエストを構築:
      ```kotlin
      val request = chatRequest {
          model = "openai/gpt-4o"
          userMessage("Hello!")
      }
      ```
      → ChatCompletionRequest が model="openai/gpt-4o", messages=[UserMessage(content="Hello!")] であること
    - DSLビルダーでフルリクエストを構築:
      ```kotlin
      val request = chatRequest {
          model = "openai/gpt-4o"
          temperature = 0.7
          maxTokens = 1000
          systemMessage("You are a helpful assistant.")
          userMessage("What is Kotlin?")
      }
      ```
    - メッセージ追加テスト: systemMessage, userMessage, assistantMessage, toolMessage
    - オプションフィールドの設定テスト: topP, frequencyPenalty, presencePenalty, seed, stop, responseFormat
    - `client.chat { ... }` 拡張関数のMockEngineテスト:
      - DSLで構築 → L1 ChatApi.complete() 呼び出し → 正しいレスポンスが返る
    - @DslMarkerによるスコープ制限テスト

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l2/OpenRouterDslMarker.kt`:
    ```kotlin
    @DslMarker
    annotation class OpenRouterDslMarker
    ```
  - `src/main/kotlin/dev/toliner/openrouter/l2/chat/ChatRequestBuilder.kt`:
    - `@OpenRouterDslMarker` アノテーション付き
    - mutableプロパティ（model, temperature, maxTokens等）
    - メッセージ追加関数（systemMessage, userMessage, assistantMessage, toolMessage）
    - `build(): ChatCompletionRequest` でimmutableなリクエストオブジェクトを生成
  - `src/main/kotlin/dev/toliner/openrouter/l2/chat/ChatDsl.kt`:
    - `fun chatRequest(block: ChatRequestBuilder.() -> Unit): ChatCompletionRequest` — ビルダー関数
    - `suspend fun OpenRouterClient.chat(block: ChatRequestBuilder.() -> Unit): ChatCompletionResponse` — クライアント拡張関数

  **Must NOT do**:
  - ストリーミングDSLはこのタスクに含めない（Task 15）
  - Tool DSLはこのタスクに含めない（Task 16）
  - Provider Routing DSLはこのタスクに含めない（Task 17）

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: DSLビルダーパターンの設計と@DslMarker実装
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 15, 16, 17)
  - **Blocks**: None
  - **Blocked By**: Tasks 4, 8

  **References**:

  **Pattern References**:
  - Task 4の `ChatCompletionRequest`, `ChatCompletionResponse` — ビルダーが生成する型
  - Task 8の `OpenRouterClient`, `ChatApi` — 拡張関数の受信者と呼び出し先

  **External References**:
  - Kotlin DSLパターン: `@DslMarker` アノテーションを使ってDSLスコープを制限する。これにより、内側のビルダーから外側のビルダーのメソッドを誤って呼び出すことを防ぐ。
  - ビルダーパターン: mutableなBuilderクラスで値を設定し、`build()` でimmutableなデータクラスに変換する。

  **WHY Each Reference Matters**:
  - @DslMarkerなしではネストしたDSLブロックでスコープが混在し、意図しない呼び出しが可能になる。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/l2/chat/ChatDslTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l2.chat.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: DSLでの最小限リクエスト構築
    Tool: Bash
    Preconditions: ChatRequestBuilder, chatRequest()が実装済み
    Steps:
      1. chatRequest { model = "openai/gpt-4o"; userMessage("Hello!") } を実行
      2. 結果のChatCompletionRequestのmodelが"openai/gpt-4o"であること
      3. messagesが1つのUserMessage("Hello!")であること
      4. temperature等のオプションがnullであること
    Expected Result: 最小限のDSLからChatCompletionRequestが正しく生成される
    Failure Indicators: modelが未設定、messagesが空
    Evidence: .sisyphus/evidence/task-14-dsl-minimal.txt

  Scenario: client.chat { } 拡張関数の統合テスト
    Tool: Bash
    Preconditions: OpenRouterClient拡張関数が実装済み、MockEngine設定済み
    Steps:
      1. MockEngineで正常レスポンスを返すよう設定
      2. client.chat { model = "openai/gpt-4o"; userMessage("Hi") } を呼び出し
      3. ChatCompletionResponseが正しく返されることを確認
    Expected Result: DSL → L1 API呼び出し → レスポンスのフルパイプラインが動作する
    Failure Indicators: DSLからのリクエスト変換エラー
    Evidence: .sisyphus/evidence/task-14-dsl-integration.txt
  ```

  **Commit**: YES
  - Message: `feat(l2): add chat DSL builder`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l2/`, `src/test/kotlin/dev/toliner/openrouter/l2/chat/`
  - Pre-commit: `./gradlew test`

- [ ] 15. Stream DSLビルダー

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l2/stream/StreamDslTest.kt`
    - `client.chatStream { ... }` 拡張関数のテスト:
      - DSLで構築 → stream=true が自動設定されること
      - L1 ChatApi.stream() が呼び出されること
      - Flow<ChatCompletionChunk> が返されること
    - 便利な拡張関数のテスト:
      - `Flow<ChatCompletionChunk>.collectContent(): String` — 全チャンクのdelta.contentを連結
      - `Flow<ChatCompletionChunk>.collectContentAndUsage(): Pair<String, Usage>` — コンテンツ連結 + 最終usage

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l2/stream/StreamDsl.kt`:
    - `suspend fun OpenRouterClient.chatStream(block: ChatRequestBuilder.() -> Unit): Flow<ChatCompletionChunk>` — ストリーミング拡張関数（内部でstream=trueを設定）
  - `src/main/kotlin/dev/toliner/openrouter/l2/stream/FlowExtensions.kt`:
    - `suspend fun Flow<ChatCompletionChunk>.collectContent(): String`
    - `suspend fun Flow<ChatCompletionChunk>.collectContentAndUsage(): Pair<String, Usage>`

  **Must NOT do**:
  - SSEパースロジックの再実装（Task 5で完了済み）
  - ChatRequestBuilderの修正（Task 14で完了済み、ここでは再利用のみ）

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: ストリーミングDSLとFlow拡張関数の実装
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 14, 16, 17)
  - **Blocks**: None
  - **Blocked By**: Tasks 5, 8

  **References**:

  **Pattern References**:
  - Task 14の `ChatRequestBuilder`, `@OpenRouterDslMarker` — 同じビルダーを再利用
  - Task 5の `toChatCompletionChunks()` — ストリーミングFlow変換
  - Task 8の `ChatApi.stream()` — L1ストリーミングエンドポイント

  **External References**:
  - Kotlin Flow: `flow { }`, `map`, `onCompletion`, `fold` 等の操作。`collectContent()` は `fold("")` で delta.content を連結する。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/l2/stream/StreamDslTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l2.stream.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: chatStream DSL
    Tool: Bash
    Steps:
      1. client.chatStream { model = "openai/gpt-4o"; userMessage("Hi") } を呼び出し
      2. stream=true が自動設定されていることを確認（MockEngineでリクエストボディ検証）
      3. Flow<ChatCompletionChunk>が返されることを確認
    Expected Result: DSLからストリーミングFlowが正しく返される
    Evidence: .sisyphus/evidence/task-15-stream-dsl.txt

  Scenario: collectContent()拡張関数
    Tool: Bash
    Steps:
      1. 手作りのFlow<ChatCompletionChunk>（delta.content = "Hel", "lo", " Wor", "ld"）を作成
      2. collectContent()を呼び出し
      3. 結果が "Hello World" であることを確認
    Expected Result: 全チャンクのcontentが正しく連結される
    Evidence: .sisyphus/evidence/task-15-collect-content.txt
  ```

  **Commit**: YES
  - Message: `feat(l2): add streaming DSL builder`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l2/stream/`, `src/test/kotlin/dev/toliner/openrouter/l2/stream/`
  - Pre-commit: `./gradlew test`

- [ ] 16. Tool Calling DSL

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l2/tools/ToolDslTest.kt`
    - ツール定義DSLテスト:
      ```kotlin
      val request = chatRequest {
          model = "openai/gpt-4o"
          userMessage("What's the weather in Tokyo?")
          tools {
              function("get_weather") {
                  description = "Get the current weather"
                  parameters {
                      property("location", "string") {
                          description = "The city name"
                      }
                      required("location")
                  }
              }
          }
          toolChoice = ToolChoice.Auto
      }
      ```
      → tools配列に1つのFunctionToolが含まれ、parametersがJSON Schemaとして正しいこと
    - 複数ツール定義テスト
    - toolChoiceの各バリアント（Auto, None, Required, Function("name")）設定テスト
    - JSON Schema生成の正確性テスト（type, properties, required, description）

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l2/tools/ToolsBuilder.kt`:
    ```kotlin
    @OpenRouterDslMarker
    class ToolsBuilder {
        fun function(name: String, block: FunctionToolBuilder.() -> Unit)
        fun build(): List<Tool>
    }
    ```
  - `src/main/kotlin/dev/toliner/openrouter/l2/tools/FunctionToolBuilder.kt`:
    ```kotlin
    @OpenRouterDslMarker
    class FunctionToolBuilder {
        var description: String? = null
        fun parameters(block: JsonSchemaBuilder.() -> Unit)
        fun build(): FunctionTool
    }
    ```
  - `src/main/kotlin/dev/toliner/openrouter/l2/tools/JsonSchemaBuilder.kt`:
    ```kotlin
    @OpenRouterDslMarker
    class JsonSchemaBuilder {
        fun property(name: String, type: String, block: PropertyBuilder.() -> Unit = {})
        fun required(vararg names: String)
        fun build(): JsonObject  // JSON Schema as JsonObject
    }
    ```
  - ChatRequestBuilderに `tools(block: ToolsBuilder.() -> Unit)` メソッドを追加

  **Must NOT do**:
  - ツール呼び出し結果のパースロジックは含めない（それはL1のレスポンス型で対応済み）
  - エージェントループ（自動ツール呼び出しと結果送信の繰り返し）は含めない

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: ネストしたDSLビルダー（Tools → Function → Parameters → Property）の設計
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 14, 15, 17)
  - **Blocks**: None
  - **Blocked By**: Tasks 4, 8

  **References**:

  **Pattern References**:
  - Task 14の `ChatRequestBuilder`, `@OpenRouterDslMarker` — ToolsBuilderをChatRequestBuilderに統合
  - Task 4の `Tool`, `FunctionTool`, `FunctionCall` 型 — ビルダーが生成する型

  **External References**:
  - OpenRouter/OpenAI Tools API: tools配列の各要素は `{"type": "function", "function": {"name": "...", "description": "...", "parameters": {JSON Schema}}}` 形式。parametersはJSON Schema（type: "object", properties: {...}, required: [...]）。
  - JSON Schema: `{"type": "object", "properties": {"location": {"type": "string", "description": "The city name"}}, "required": ["location"]}` 形式。

  **WHY Each Reference Matters**:
  - Tool Callingは最も複雑なDSL部分。JSON Schemaの生成が正確でないとLLMがツールを正しく呼び出せない。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/l2/tools/ToolDslTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l2.tools.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: ツール定義DSLからJSON Schema生成
    Tool: Bash
    Steps:
      1. DSLでget_weatherツールを定義（location: string, required）
      2. build()でChatCompletionRequestを生成
      3. tools[0].function.parameters がJSON Schema形式であることを確認
      4. JSON Schemaの type="object", properties.location.type="string", required=["location"] を検証
    Expected Result: 正しいJSON Schemaが生成される
    Failure Indicators: JSON Schemaの構造不正、必須フィールド欠落
    Evidence: .sisyphus/evidence/task-16-tool-dsl.txt

  Scenario: toolChoiceの各バリアント
    Tool: Bash
    Steps:
      1. toolChoice = ToolChoice.Auto で "auto" がシリアライズされること
      2. toolChoice = ToolChoice.None で "none" がシリアライズされること
      3. toolChoice = ToolChoice.Required で "required" がシリアライズされること
      4. toolChoice = ToolChoice.Function("get_weather") でオブジェクト形式がシリアライズされること
    Expected Result: 全バリアントが正しいJSON形式にシリアライズされる
    Evidence: .sisyphus/evidence/task-16-tool-choice.txt
  ```

  **Commit**: YES
  - Message: `feat(l2): add tool calling DSL`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l2/tools/`, `src/test/kotlin/dev/toliner/openrouter/l2/tools/`
  - Pre-commit: `./gradlew test`

- [ ] 17. Provider Routing DSL

  **What to do**:
  - TDD: テストから書く

  RED phase:
  - `src/test/kotlin/dev/toliner/openrouter/l2/routing/RoutingDslTest.kt`
    - プロバイダールーティングDSLテスト:
      ```kotlin
      val request = chatRequest {
          model = "openai/gpt-4o"
          userMessage("Hello!")
          provider {
              order = listOf("OpenAI", "Azure")
              allowFallbacks = true
              requireParameters = listOf("temperature")
              dataCollection = "deny"
              ignore = listOf("Together")
          }
      }
      ```
      → ProviderPreferences が正しく設定されていること
    - 全フィールドのテスト: order, allow_fallbacks, require_parameters, data_collection, sort, preferred_min_throughput, ignore

  GREEN phase:
  - `src/main/kotlin/dev/toliner/openrouter/l2/routing/ProviderRoutingBuilder.kt`:
    ```kotlin
    @OpenRouterDslMarker
    class ProviderRoutingBuilder {
        var order: List<String>? = null
        var allowFallbacks: Boolean? = null
        var requireParameters: List<String>? = null
        var dataCollection: String? = null  // "deny" | "allow"
        var preferredMinThroughput: Double? = null
        var ignore: List<String>? = null
        // sort は複雑（model+order のリスト）なので必要に応じてDSLで
        fun build(): ProviderPreferences
    }
    ```
  - ChatRequestBuilderに `provider(block: ProviderRoutingBuilder.() -> Unit)` メソッドを追加

  **Must NOT do**:
  - ProviderPreferencesの型定義自体は変更しない（Task 4で定義済み）
  - バリデーションロジック（orderのプロバイダー名チェック等）は含めない

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 比較的シンプルなビルダー。フィールドマッピングのみ。
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 14, 15, 16)
  - **Blocks**: None
  - **Blocked By**: Task 8

  **References**:

  **Pattern References**:
  - Task 14の `ChatRequestBuilder`, `@OpenRouterDslMarker` — ProviderRoutingBuilderをChatRequestBuilderに統合
  - Task 4の `ProviderPreferences` 型 — ビルダーが生成する型

  **External References**:
  - OpenRouter Provider Routing: `provider` オブジェクトでルーティング制御。`order` はプロバイダー名リスト（優先順位）、`allow_fallbacks` は指定プロバイダーが全て失敗した場合に他のプロバイダーにフォールバックするかどうか、`require_parameters` はプロバイダーがサポートすべきパラメータのリスト、`data_collection` は "deny"（データ収集拒否）or "allow"、`preferred_min_throughput` は最小スループット要件。

  **Acceptance Criteria**:

  **TDD:**
  - [ ] テストファイル作成: `src/test/kotlin/dev/toliner/openrouter/l2/routing/RoutingDslTest.kt`
  - [ ] `./gradlew test --tests "dev.toliner.openrouter.l2.routing.*"` → PASS

  **QA Scenarios:**

  ```
  Scenario: Provider Routing DSL
    Tool: Bash
    Steps:
      1. DSLでprovider { order = listOf("OpenAI"); allowFallbacks = true; dataCollection = "deny" } を設定
      2. build()でChatCompletionRequestを生成
      3. provider.order, provider.allow_fallbacks, provider.data_collection を検証
      4. JSONシリアライゼーションで@SerialName("allow_fallbacks")等が正しいことを確認
    Expected Result: ProviderPreferencesが正しく構築され、JSONシリアライゼーションも正しい
    Evidence: .sisyphus/evidence/task-17-routing-dsl.txt
  ```

  **Commit**: YES
  - Message: `feat(l2): add provider routing DSL`
  - Files: `src/main/kotlin/dev/toliner/openrouter/l2/routing/`, `src/test/kotlin/dev/toliner/openrouter/l2/routing/`
  - Pre-commit: `./gradlew test`

- [ ] F1. **プラン準拠監査** — `oracle`
  プランを端から端まで読む。各「Must Have」について実装が存在することを確認（ファイル読み込み、コマンド実行）。各「Must NOT Have」についてコードベースを検索し、禁止パターンが見つかればfile:lineで報告。エビデンスファイルの存在を確認。成果物をプランと比較。
  出力: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **コード品質レビュー** — `unspecified-high`
  `./gradlew test` を実行。全変更ファイルを確認: `as Any`/`@Suppress`, 空catch, println in prod, コメントアウトコード, 未使用import。AIスロップ確認: 過剰コメント, 過剰抽象化, 汎用名(data/result/item/temp)。
  出力: `Build [PASS/FAIL] | Tests [N pass/N fail] | Files [N clean/N issues] | VERDICT`

- [ ] F3. **実QA** — `unspecified-high`
  クリーン状態から開始。全タスクの全QAシナリオを実行。クロスタスク統合テスト。エッジケース: 空状態、不正入力、高速操作。エビデンスを `.sisyphus/evidence/final-qa/` に保存。
  出力: `Scenarios [N/N pass] | Integration [N/N] | Edge Cases [N tested] | VERDICT`

- [ ] F4. **スコープ忠実性チェック** — `deep`
  各タスクについて: 「What to do」を読み、実際のdiff (git log/diff) を確認。1:1対応を検証 — 仕様通りに全て実装されている（不足なし）、仕様外のものは実装されていない（スコープクリープなし）。「Must NOT do」準拠を確認。タスク間汚染の検出。未計上の変更をフラグ。
  出力: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | Unaccounted [CLEAN/N files] | VERDICT`

---

## Commit Strategy

各マイルストーン完了時にコミット。各コミットは独立してコンパイル可能で全テストがパスすること。

1. `build: add Ktor, kotlinx.serialization, Kotest dependencies` — build.gradle.kts
2. `feat(serialization): add Json config and custom serializers for union types` — serialization/パッケージ + テスト
3. `feat(error): add sealed exception hierarchy` — error/パッケージ + テスト
4. `feat(l1/chat): add Chat Completion request/response types` — l1/chat/パッケージ + テスト
5. `feat(streaming): add SSE parsing and flow transformation` — streaming/パッケージ + テスト
6. `feat(l1/models): add model types and endpoints` — l1/models/パッケージ + テスト
7. `feat(l1/embeddings): add embedding types and endpoints` — l1/embeddings/パッケージ + テスト
8. `feat(client): add OpenRouterClient with engine injection and chat endpoints` — client/パッケージ + テスト
9. `feat(l1/account): add generation, credits, key info, activity endpoints` — l1/パッケージ群 + テスト
10. `feat(l1/keys): add key management CRUD` — l1/keys/パッケージ + テスト
11. `feat(l1/guardrails): add guardrails CRUD and assignments` — l1/guardrails/パッケージ + テスト
12. `feat(l1/providers): add providers and OAuth endpoints` — l1/providers/, l1/auth/パッケージ + テスト
13. `feat(l1/responses): add Responses API (beta) types and endpoint` — l1/responses/パッケージ + テスト
14. `feat(l2): add chat DSL builder` — l2/chat/パッケージ + テスト
15. `feat(l2): add streaming DSL builder` — l2/stream/パッケージ + テスト
16. `feat(l2): add tool calling DSL` — l2/tools/パッケージ + テスト
17. `feat(l2): add provider routing DSL` — l2/routing/パッケージ + テスト

---

## Success Criteria

### 検証コマンド
```bash
./gradlew test  # Expected: BUILD SUCCESSFUL, 0 failures
./gradlew build  # Expected: BUILD SUCCESSFUL
```

### 最終チェックリスト
- [ ] L1: 全33エンドポイントに対応する型付き関数が存在する
- [ ] L2: chat { }, stream { }, Tool DSL, Provider Routing DSLが機能する
- [ ] SSEストリーミングがFlow<ChatCompletionChunk>として正しく動作する
- [ ] 全Union型にカスタムシリアライザーが実装されている
- [ ] sealed例外階層で全HTTPエラーコードが型安全にハンドリングされる
- [ ] in-bandエラー（HTTP 200 + choices[i].error）が検出される
- [ ] MockEngineベースの全テストがパスする
- [ ] プロパティベーステスト（シリアライゼーション往復）がパスする
- [ ] `as Any` / `@Suppress("UNCHECKED_CAST")` が存在しない
- [ ] `JsonNamingStrategy.SnakeCase` が使用されていない
- [ ] 全ての `@SerialName` が正しいsnake_caseフィールド名を指定している

---

## Interfaces and Dependencies

### 主要インターフェース

`src/main/kotlin/dev/toliner/openrouter/client/OpenRouterClient.kt` に定義:

```kotlin
class OpenRouterClient(
    engine: HttpClientEngine,
    private val config: OpenRouterConfig
) : Closeable {
    // L1 エンドポイント
    val chat: ChatApi           // POST /chat/completions (non-stream + stream)
    val responses: ResponsesApi // POST /responses (beta)
    val embeddings: EmbeddingsApi
    val models: ModelsApi       // GET /models 等
    val generation: GenerationApi
    val account: AccountApi     // GET /credits, GET /key, GET /activity
    val keys: KeysApi           // GET/POST/PATCH/DELETE /keys
    val guardrails: GuardrailsApi
    val providers: ProvidersApi
    val auth: AuthApi           // OAuth PKCE

    fun close()
}
```

`src/main/kotlin/dev/toliner/openrouter/client/OpenRouterConfig.kt` に定義:

```kotlin
data class OpenRouterConfig(
    val apiKey: String,
    val baseUrl: String = "https://openrouter.ai/api/v1",
    val httpReferer: String? = null,
    val xTitle: String? = null,
)
```

`src/main/kotlin/dev/toliner/openrouter/error/OpenRouterException.kt` に定義:

```kotlin
sealed class OpenRouterException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class BadRequest(val error: ErrorBody) : OpenRouterException("400: ${error.message}")
    data class Unauthorized(val error: ErrorBody) : OpenRouterException("401: ${error.message}")
    data class PaymentRequired(val error: ErrorBody) : OpenRouterException("402: ${error.message}")
    data class Forbidden(val error: ErrorBody) : OpenRouterException("403: ${error.message}")
    data class RequestTimeout(val error: ErrorBody) : OpenRouterException("408: ${error.message}")
    data class TooManyRequests(val error: ErrorBody, val retryAfter: Int?) : OpenRouterException("429: ${error.message}")
    data class BadGateway(val error: ErrorBody) : OpenRouterException("502: ${error.message}")
    data class ServiceUnavailable(val error: ErrorBody) : OpenRouterException("503: ${error.message}")
    data class UnknownError(val statusCode: Int, val error: ErrorBody) : OpenRouterException("$statusCode: ${error.message}")
    data class StreamError(val error: ErrorBody) : OpenRouterException("Stream error: ${error.message}")
    data class InBandError(val error: ErrorBody) : OpenRouterException("In-band error: ${error.message}")
}
```

L2 DSLのエントリポイント（`src/main/kotlin/dev/toliner/openrouter/l2/chat/ChatDsl.kt`）:

```kotlin
@OpenRouterDslMarker
class ChatRequestBuilder {
    var model: String = ""
    var temperature: Double? = null
    var maxTokens: Int? = null
    var topP: Double? = null
    var stream: Boolean = false
    // ...
    fun message(role: String, content: String) { ... }
    fun systemMessage(content: String) { ... }
    fun userMessage(content: String) { ... }
    fun assistantMessage(content: String) { ... }
    fun tools(block: ToolsBuilder.() -> Unit) { ... }
    fun provider(block: ProviderRoutingBuilder.() -> Unit) { ... }
    // ...
    fun build(): ChatCompletionRequest { ... }
}

suspend fun OpenRouterClient.chat(block: ChatRequestBuilder.() -> Unit): ChatCompletionResponse { ... }
suspend fun OpenRouterClient.chatStream(block: ChatRequestBuilder.() -> Unit): Flow<ChatCompletionChunk> { ... }
```

### 依存ライブラリ（build.gradle.ktsに追加するもの）

```kotlin
dependencies {
    // Ktor Client
    implementation("io.ktor:ktor-client-core:3.4.1")
    implementation("io.ktor:ktor-client-cio:3.4.1")        // デフォルトエンジン
    implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")

    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // Test
    testImplementation("io.kotest:kotest-runner-junit5:6.1.0")
    testImplementation("io.kotest:kotest-assertions-core:6.1.0")
    testImplementation("io.kotest:kotest-property:6.1.0")
    testImplementation("io.ktor:ktor-client-mock:3.4.1")
}
```

---

## Idempotence and Recovery

全てのステップは繰り返し実行可能である。ソースコードの追加のみで、既存ファイルの破壊的変更は行わない。

テスト実行 (`./gradlew test`) は冪等で、副作用なし。MockEngineを使用するため外部APIへの依存もなし。

ビルドが壊れた場合: `./gradlew clean build` で再ビルド可能。

---

## Artifacts and Notes

（実装進行に伴い、テスト出力やdiff等のエビデンスをここに追記する）
