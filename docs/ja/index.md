# openrouter-kotlin

**OpenRouter API 用 Kotlin クライアントライブラリ**

`openrouter-kotlin` は、OpenRouter API を Kotlin から簡単に利用するための型安全なクライアントライブラリです。Ktor をベースに構築されており、直感的な DSL を提供します。

## 主な機能

- **型安全な DSL**: リクエストを構築するための直感的な Kotlin DSL。
- **ストリーミング対応**: サーバー送信イベント（SSE）を Kotlin Coroutines の `Flow` で処理。
- **充実したエラーハンドリング**: HTTP ステータスコードに基づいた型安全な例外階層。
- **全エンドポイント対応**: チャット補完、モデル一覧、プロバイダー情報、アカウント情報など。
- **マルチプラットフォーム対応予定**: 現在は JVM を中心に JDK 25 をサポート。

## 基本的な使用例

```kotlin
val client = OpenRouterClient(CIO.create(), OpenRouterConfig(apiKey = "YOUR_API_KEY"))

val response = client.chat {
    model = "openai/gpt-4o-mini"
    userMessage("Kotlin クライアントライブラリの利点を教えてください。")
}

println(response.choices.first().message?.content)
```

## 次のステップ

- [インストール](getting-started/installation.md): プロジェクトへの導入方法。
- [クイックスタート](getting-started/quick-start.md): 最初の API リクエストを送信する。
- [チャット補完ガイド](guide/chat-completions.md): チャット機能の詳細。
- [ストリーミングガイド](guide/streaming.md): リアルタイムレスポンスの処理。

## 技術情報

- **Kotlin**: 2.3.10
- **JDK**: 25
- **kotlinx-serialization**: 1.10.0
- **Ktor**: 3.4.1 (CIO エンジン)
