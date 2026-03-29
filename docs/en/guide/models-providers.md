# Models & Providers Guide

Besides chat completions, openrouter-kotlin allows you to list available models, providers, and retrieve account information.

## Models API

Retrieve information about models supported by OpenRouter.

### List All Models

```kotlin
val models = client.models.list()
models.forEach { model ->
    println("${model.id}: ${model.name}")
}
```

### Get Model Count

```kotlin
val count = client.models.count()
println("There are $count models available.")
```

## Providers API

Retrieve information about the model providers used by OpenRouter.

### List All Providers

```kotlin
val providers = client.providers.list()
providers.forEach { provider ->
    println("${provider.id}: ${provider.name}")
}
```

## Account API

Retrieve your OpenRouter account information, including usage and credits.

### Get Credits

```kotlin
val credits = client.account.credits()
println("Remaining credits: ${credits.totalCredits}")
```

### Key Information

Get details about the API key currently being used.

```kotlin
val keyInfo = client.account.keyInfo()
println("Key label: ${keyInfo.label}")
println("Usage: ${keyInfo.usage} tokens")
```

### Account Activity

Get recent activity for your account.

```kotlin
val activity = client.account.activity()
activity.data.forEach { item ->
    println("${item.id}: ${item.model} - ${item.totalTokens} tokens")
}
```

!!! note
    Account methods require a valid API key. Public methods like listing models do not strictly require a key, but having one may provide more detailed information.
