# DSL Builders Guide

openrouter-kotlin provides a rich, type-safe DSL for building complex API requests. This guide explains how to use the various builders.

## Top-Level Entry Points

There are three main ways to use the DSL:

- `client.chat { }`: Directly makes a non-streaming chat completion call.
- `client.chatStream { }`: Directly makes a streaming chat completion call.
- `chatRequest { }`: Creates a `ChatCompletionRequest` object that you can reuse or send later.

## Message Builder Methods

Inside a chat request builder, you can add messages using dedicated helper methods.

```kotlin
val request = chatRequest {
    systemMessage("You are a helpful assistant.")
    userMessage("Hello!")
    assistantMessage("Hi there! How can I help you?")
}
```

## Tool Definitions

Define tools that the model can call using the `tools { }` block.

```kotlin
client.chat {
    model = "openai/gpt-4o-mini"
    userMessage("What's the weather like in Tokyo?")
    
    tools {
        function(
            name = "get_weather",
            description = "Get current weather in a location"
        ) {
            parameters {
                property("location", type = "string", description = "The city and state, e.g. San Francisco, CA")
                required("location")
            }
        }
    }
}
```

### JSON Schema Builder

The `parameters { }` block allows you to define a JSON schema for tool parameters in a type-safe way:

- `property(name, type, description)`: Define a property.
- `required(name)`: Mark a property as required.
- `enum(values)`: Define a set of allowed values for a property.

## Provider Routing

OpenRouter allows you to control which providers are used for a request.

```kotlin
client.chat {
    model = "openai/gpt-4o"
    provider {
        requireParameters = true
        order = listOf("OpenAI", "Azure")
        dataCollection = "deny"
    }
}
```

## Nested Builders Overview

Most complex fields in the OpenRouter API have corresponding DSL builders.

- `responseFormat { }`: Define if the response should be in JSON mode.
- `logitBias { }`: Adjust the likelihood of specific tokens appearing.
- `tools { }`: Define available functions for the model.
- `provider { }`: Configure provider routing preferences.

All builders are marked with `@OpenRouterDslMarker` to prevent illegal nesting and ensure correct usage within the DSL.
