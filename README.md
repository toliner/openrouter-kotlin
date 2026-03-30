# openrouter-kotlin

[OpenRouter API](https://openrouter.ai/) の Kotlin クライアントライブラリ。

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![JVM](https://img.shields.io/badge/JVM-25-orange.svg?style=flat-square)](https://openjdk.org/)
[![License](https://img.shields.io/github/license/toliner/openrouter-kotlin?style=flat-square)](LICENSE)
[![JitPack](https://img.shields.io/jitpack/version/com.github.toliner/openrouter-kotlin?style=flat-square)](https://jitpack.io/#toliner/openrouter-kotlin)

型安全な DSL と Kotlin Coroutines による OpenRouter API の完全なクライアント実装です。

---

**[English](#english)** | **[日本語](#日本語)**

---

## 日本語

### 特徴

- **型安全な DSL** — Kotlin DSL で直感的にチャットリクエストを構築
- **ストリーミング対応** — Kotlin Coroutines Flow によるストリーミングレスポンス
- **堅牢なエラー処理** — 全 HTTP/API エラーに対応する sealed exception 階層
- **完全な API カバレッジ** — Chat Completions, Models, Providers, Embeddings, Account 等
- **カスタムシリアライゼーション** — OpenRouter 固有の JSON ユニオン型に対応
- **Ktor ベース** — CIO エンジンによる効率的な HTTP 通信

### インストール

JitPack リポジトリを `build.gradle.kts` に追加:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.toliner:openrouter-kotlin:TAG")
}
```

`TAG` を最新バージョン（例: `v1.0.0`）に置き換えてください。

### クイックスタート

```kotlin
import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.client.OpenRouterConfig
import dev.toliner.openrouter.l2.chat.chat
import io.ktor.client.engine.cio.CIO

suspend fun main() {
    val config = OpenRouterConfig(apiKey = "sk-or-...")
    val client = OpenRouterClient(CIO.create(), config)

    client.use {
        val response = it.chat {
            model = "openai/gpt-4o-mini"
            systemMessage("You are a helpful assistant.")
            userMessage("Hello! Tell me about Kotlin.")
            maxTokens = 200
        }

        println(response.choices.first().message?.content)
    }
}
```

### ストリーミング

```kotlin
import dev.toliner.openrouter.l2.stream.chatStream

client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Write a short poem about Kotlin.")
}.collect { chunk ->
    print(chunk.choices.firstOrNull()?.delta?.content.orEmpty())
}
```

### ドキュメント

- 📖 [ユーザーガイド（日本語）](https://toliner.github.io/openrouter-kotlin/ja/)
- 📚 [API リファレンス（KDoc）](https://toliner.github.io/openrouter-kotlin/api/)

---

## English

### Features

- **Type-Safe DSL** — Build complex chat requests with an intuitive Kotlin DSL
- **Full Streaming Support** — Handle streaming responses with Kotlin Coroutines Flow
- **Robust Error Handling** — Exhaustive sealed exception hierarchy for all API and HTTP error states
- **Complete API Coverage** — Chat Completions, Models, Providers, Embeddings, Account, and more
- **Custom Serialization** — Built-in support for OpenRouter's flexible JSON union types
- **Ktor-Powered** — Built on the modern Ktor HTTP client with the efficient CIO engine

### Installation

Add the JitPack repository to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.toliner:openrouter-kotlin:TAG")
}
```

Replace `TAG` with the latest version (e.g., `v1.0.0`).

### Quick Start

```kotlin
import dev.toliner.openrouter.client.OpenRouterClient
import dev.toliner.openrouter.client.OpenRouterConfig
import dev.toliner.openrouter.l2.chat.chat
import io.ktor.client.engine.cio.CIO

suspend fun main() {
    val config = OpenRouterConfig(apiKey = "sk-or-...")
    val client = OpenRouterClient(CIO.create(), config)

    client.use {
        val response = it.chat {
            model = "openai/gpt-4o-mini"
            systemMessage("You are a helpful assistant.")
            userMessage("Hello! Tell me about Kotlin.")
            maxTokens = 200
        }

        println(response.choices.first().message?.content)
    }
}
```

### Streaming

```kotlin
import dev.toliner.openrouter.l2.stream.chatStream

client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Write a short poem about Kotlin.")
}.collect { chunk ->
    print(chunk.choices.firstOrNull()?.delta?.content.orEmpty())
}
```

### Documentation

- 📖 [User Guide (English)](https://toliner.github.io/openrouter-kotlin/en/)
- 📖 [ユーザーガイド（日本語）](https://toliner.github.io/openrouter-kotlin/ja/)
- 📚 [API Reference (KDoc)](https://toliner.github.io/openrouter-kotlin/api/)

---

## Architecture

```
dev.toliner.openrouter
├── l1/          # Low-level @Serializable data classes (mirrors OpenRouter JSON API)
├── l2/          # High-level DSL builders
├── client/      # Ktor-based HTTP client (OpenRouterClient, *Api classes)
├── serialization/ # Custom KSerializers for JSON union types
├── streaming/   # SSE parsing and in-band error detection
└── error/       # Sealed exception hierarchy
```

Dependencies flow downward: `client/` → `l1/`, `l2/` → `l1/`. The `l1/` and `l2/` layers never depend on `client/`.

## Requirements

- **JDK 25+**
- **Kotlin 2.3.10+**

## Building

```bash
./gradlew build          # Full build + tests
./gradlew test           # Tests only
./gradlew dokkaGeneratePublicationHtml  # Generate API docs
```

## License

See [LICENSE](LICENSE) for details.
