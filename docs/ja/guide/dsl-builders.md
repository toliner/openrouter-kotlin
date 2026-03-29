# DSL ビルダーガイド

本ライブラリは、複雑なリクエストを簡単に記述できる型安全な DSL ビルダーを提供しています。

## チャット DSL

`client.chat { ... }` または `chatRequest { ... }` を使用して、リクエストを構築できます。

```kotlin
val request = chatRequest {
    model = "openai/gpt-4o-mini"
    systemMessage("Assistant instruction")
    userMessage("Hello!")
}
```

## メッセージビルダー

メッセージは、コンテンツを指定するだけで簡単に追加できます。

```kotlin
client.chat {
    systemMessage("You are an assistant.")
    userMessage("Hi!")
    assistantMessage("Hello! How can I help you today?")
    userMessage("Tell me a joke.")
}
```

## ツールと関数の定義

OpenRouter (OpenAI 互換) の関数呼び出し（Tool Calling）も DSL で簡単に定義できます。

```kotlin
client.chat {
    model = "openai/gpt-4o-mini"
    userMessage("東京の天気を教えて。")
    
    tools {
        function(
            name = "get_weather",
            description = "指定された場所の現在の天気を取得します。"
        ) {
            parameters {
                property("location", "string") {
                    description = "都市名（例: 東京）"
                }
                required("location")
            }
        }
    }
}
```

### JSON Schema ビルダー

`parameters { ... }` ブロック内では、JSON Schema を直感的に構築できます。

- `property(name, type) { ... }`: プロパティの追加。
- `required(names...)`: 必須項目の指定。
- `enum(values...)`: 列挙型の値の設定。

## プロバイダールーティング

特定のプロバイダーを優先したり、除外したりする設定も DSL で行えます。

```kotlin
client.chat {
    model = "openai/gpt-4o-mini"
    
    provider {
        order = listOf("OpenAI", "Azure")
        allowFallbacks = true
        requireParameters = true
    }
}
```

## 階層構造

DSL は以下の階層構造を持っています。

- `ChatCompletionRequestBuilder` (chatRequest { ... })
    - `ChatMessageBuilder` (messages { ... })
    - `ToolBuilder` (tools { ... })
        - `FunctionBuilder` (function { ... })
            - `JsonSchemaBuilder` (parameters { ... })
    - `ProviderSettingsBuilder` (provider { ... })
