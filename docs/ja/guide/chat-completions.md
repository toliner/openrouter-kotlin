# チャット補完ガイド

チャット補完は、OpenRouter API のメインとなる機能です。

## 基本的なリクエスト

`client.chat { ... }` を使って、リクエストを構築します。

```kotlin
val response = client.chat {
    model = "openai/gpt-4o-mini"
    systemMessage("あなたは優秀な翻訳家です。")
    userMessage("OpenRouter is a great service!")
}
```

## メッセージの種類

DSL 内では、以下のメソッドを使用してメッセージを追加できます。

- `systemMessage(content)`: システムプロンプトを設定。
- `userMessage(content)`: ユーザーからの入力を設定。
- `assistantMessage(content)`: AI モデルからの過去の応答を設定。
- `toolMessage(content, toolCallId)`: ツール（関数）の実行結果を設定。

## リクエストパラメータ

DSL では、よく使われる以下のパラメータを簡単に設定できます。

- `model`: 使用するモデルの ID。
- `temperature`: 出力のランダム性（0.0 〜 2.0）。
- `maxTokens`: 生成されるトークンの最大数。
- `topP`: 核サンプリング（Top-p）の閾値。
- `stop`: 生成を停止する文字列のリスト。

## レスポンス構造

API から返ってくる `ChatCompletionResponse` には、以下の情報が含まれています。

- `choices`: 生成された回答のリスト（通常は要素が1つ）。
    - `message`: 回答内容。
    - `finishReason`: 生成が終了した理由。
- `usage`: トークン使用量（プロンプト、生成、合計）。
- `id`: リクエストのユニーク ID。
- `model`: 実際に使用されたモデル。

```kotlin
val message = response.choices.first().message?.content
val promptTokens = response.usage?.promptTokens
```

## l1 型の直接使用

DSL を使わずに、`l1` パッケージのデータクラスを直接使うこともできます。複雑なロジックでメッセージを組み立てる場合に便利です。

```kotlin
import dev.toliner.openrouter.l1.chat.ChatCompletionRequest
import dev.toliner.openrouter.l1.chat.ChatMessage

val request = ChatCompletionRequest(
    model = "openai/gpt-4o-mini",
    messages = listOf(
        ChatMessage.System(content = "System instruction"),
        ChatMessage.User(content = "Hello")
    ),
    temperature = 0.7
)
val response = client.chat(request)
```
