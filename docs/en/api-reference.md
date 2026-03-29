# API Reference

The full API reference for openrouter-kotlin is automatically generated using Dokka.

## [Browse Full API Documentation](../api/)

The generated documentation includes detailed information about every public class, function, and property in the library.

## Package Structure Overview

- `dev.toliner.openrouter.client`: Core client implementation, including `OpenRouterClient` and `OpenRouterConfig`.
- `dev.toliner.openrouter.l1`: Low-level data models mirroring the OpenRouter API JSON structure.
- `dev.toliner.openrouter.l2`: High-level DSL builders for creating requests in an idiomatic way.
- `dev.toliner.openrouter.error`: Exception hierarchy and error handling utilities.
- `dev.toliner.openrouter.streaming`: Utilities for handling SSE and streaming flows.
- `dev.toliner.openrouter.serialization`: Custom KSerializer implementations for JSON union types.

!!! note
    Experimental APIs are marked with the `@ExperimentalOpenRouterApi` annotation and may be subject to change in future versions.
