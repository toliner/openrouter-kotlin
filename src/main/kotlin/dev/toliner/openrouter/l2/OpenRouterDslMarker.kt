package dev.toliner.openrouter.l2

/**
 * DSL marker annotation to prevent scope leakage in nested OpenRouter DSL builders.
 * 
 * This annotation is applied to DSL builder classes to ensure that implicit receivers
 * from outer scopes are not accessible within inner scopes, preventing accidental
 * property access from wrong scopes.
 */
@DslMarker
public annotation class OpenRouterDslMarker
