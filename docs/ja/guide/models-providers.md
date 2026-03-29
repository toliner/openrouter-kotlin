# モデルとプロバイダー

OpenRouter では、多種多様な AI モデルと、それらを提供する複数のプロバイダーを管理するためのエンドポイントが提供されています。

## モデルの管理

OpenRouter で利用可能なモデルのリストを取得できます。

### モデル一覧の取得

```kotlin
val models = client.models.list()
models.forEach { model ->
    println("${model.id}: ${model.name}")
}
```

### モデル数の取得

```kotlin
val count = client.models.count()
println("Available models: $count")
```

### 特定のモデルの詳細

特定のモデルのエンドポイントを指定して、現在のステータスや設定を確認できます。

```kotlin
val model = client.models.get("openai/gpt-4o")
println("Context length: ${model.contextLength}")
```

## プロバイダーの管理

各モデルを提供しているプロバイダー（OpenAI, Anthropic, Google, Together, Fireworks など）の情報を取得できます。

### プロバイダー一覧の取得

```kotlin
val providers = client.providers.list()
providers.forEach { provider ->
    println("${provider.name}: ${provider.models.joinToString()}")
}
```

## アカウント情報の確認

現在の API キーに関連付けられたアカウントの状態、クレジット、利用状況を確認できます。

### クレジット残高の確認

```kotlin
val credits = client.account.credits()
println("Remaining credits: $credits USD")
```

### キー情報の取得

API キーの設定（制限設定、名前など）を取得します。

```kotlin
val keyInfo = client.account.keyInfo()
println("Key Name: ${keyInfo.name}")
println("Usage: ${keyInfo.usage}")
```

### 利用状況（アクティビティ）

最近のリクエスト履歴や料金の発生状況を取得します。

```kotlin
val activities = client.account.activity()
activities.forEach { activity ->
    println("${activity.id}: ${activity.model} - ${activity.totalCost} USD")
}
```
