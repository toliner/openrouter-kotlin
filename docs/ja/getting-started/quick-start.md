# クイックスタート

`openrouter-kotlin` を使って、最初のチャットリクエストを送信してみましょう。

## 1. 設定の作成

まず、API キーを指定して `OpenRouterConfig` を作成します。

```kotlin
import dev.toliner.openrouter.client.OpenRouterConfig

val config = OpenRouterConfig(apiKey = "sk-or-...")
```

## 2. クライアントの初期化

`OpenRouterClient` に HTTP エンジン（ここでは CIO）と設定を渡して初期化します。`use` ブロックを使うことで、処理終了後にリソース（HTTP クライアント）を確実に解放できます。

```kotlin
import dev.toliner.openrouter.client.OpenRouterClient
import io.ktor.client.engine.cio.CIO

OpenRouterClient(CIO.create(), config).use { client ->
    // ここで API を呼び出す
}
```

## 3. チャットリクエストの送信

直感的な DSL を使ってリクエストを構築します。

```kotlin
import dev.toliner.openrouter.l2.chat.chat

OpenRouterClient(CIO.create(), config).use { client ->
    val response = client.chat {
        model = "openai/gpt-4o-mini"
        systemMessage("You are a helpful assistant")
        userMessage("Hello!")
        maxTokens = 100
    }
    
    // レスポンスの表示
    println(response.choices.first().message?.content)
}
```

## DSL を使用しない場合

DSL を使わずに、`l1` レイヤーのデータクラスを直接使うことも可能です。

```kotlin
import dev.toliner.openrouter.l1.chat.ChatCompletionRequest
import dev.toliner.openrouter.l1.chat.ChatMessage

val request = ChatCompletionRequest(
    model = "openai/gpt-4o-mini",
    messages = listOf(
        ChatMessage.User(content = "Hello!")
    )
)
val response = client.chat(request)
```
