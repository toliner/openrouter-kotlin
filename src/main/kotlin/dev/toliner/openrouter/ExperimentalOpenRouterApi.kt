package dev.toliner.openrouter

/**
 * Marks declarations that are part of an experimental OpenRouter API.
 *
 * APIs marked with this annotation are unstable and may change in future versions without
 * maintaining backward compatibility. Use such APIs at your own risk and be prepared to
 * update your code when the API evolves.
 *
 * Currently experimental APIs include:
 * - **ResponsesApi** — The Responses API for streaming chat completions with manual control
 *
 * To use an experimental API, annotate your declaration with `@OptIn(ExperimentalOpenRouterApi::class)`
 * or enable the experimental feature at the module level.
 *
 * Example:
 * ```kotlin
 * @OptIn(ExperimentalOpenRouterApi::class)
 * fun useExperimentalApi() {
 *     val responsesApi = client.responses
 *     // Use experimental API
 * }
 * ```
 */
@RequiresOptIn(
    message = "This API is experimental and may change in future versions.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS
)
public annotation class ExperimentalOpenRouterApi
