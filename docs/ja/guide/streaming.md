# ストリーミングガイド

OpenRouter API では、生成された回答をリアルタイムで受け取るためのストリーミング機能をサポートしています。

## 基本的な使用方法

`client.chatStream { ... }` を使用すると、Kotlin Coroutines の `Flow<ChatCompletionChunk>` が返されます。これを `collect` することで、逐次データを受け取れます。

```kotlin
import dev.toliner.openrouter.l2.chat.chatStream
import kotlinx.coroutines.flow.collect

client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Kotlinについて俳句を書いてください")
}.collect { chunk ->
    // デルタ（差分）コンテンツを取得して表示
    print(chunk.choices.firstOrNull()?.delta?.content ?: "")
}
```

## 便利なヘルパー関数

ストリーミングデータを集約するための拡張関数が用意されています。

### `collectContent()`

ストリーミング中の全テキストコンテンツを連結して、最終的な文字列として取得します。

```kotlin
val fullText = client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Hello!")
}.collectContent()

println(fullText)
```

### `collectContentAndUsage()`

テキストコンテンツと最終的なトークン使用量（Usage）の両方をペアで取得します。

```kotlin
val (content, usage) = client.chatStream {
    model = "openai/gpt-4o-mini"
    userMessage("Explain quantum computing")
}.collectContentAndUsage()

println("Content: $content")
println("Total tokens: ${usage?.totalTokens}")
```

## プログレッシブ出力の例

チャット UI などで逐次文字を表示する場合の例です。

```kotlin
client.chatStream {
    model = "anthropic/claude-3.5-sonnet"
    userMessage("Tell me a story.")
}.collect { chunk ->
    val delta = chunk.choices.firstOrNull()?.delta?.content ?: ""
    if (delta.isNotEmpty()) {
        updateUi(delta) // UIを更新する架空の関数
    }
}
```

!!! warning "ストリーミング中のエラー"
    ストリーミング中にネットワークエラーや OpenRouter 側で問題が発生した場合、`OpenRouterException.StreamError` がスローされます。
    通常の try-catch ブロックで例外をキャッチしてください。
