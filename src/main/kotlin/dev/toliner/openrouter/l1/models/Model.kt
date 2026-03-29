package dev.toliner.openrouter.l1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a language model available through the OpenRouter API.
 *
 * This data class contains comprehensive metadata about an AI model including its identifier,
 * pricing information, capabilities, and technical specifications. Models are retrieved from
 * the OpenRouter `/models` endpoint and represent available AI models from various providers.
 *
 * @property id Unique identifier for the model (e.g., "openai/gpt-4", "anthropic/claude-3-opus").
 * @property name Human-readable display name of the model.
 * @property description Optional detailed description of the model's capabilities and use cases.
 * @property pricing Cost structure for using this model across different operations.
 * @property contextLength Maximum number of tokens the model can process in a single request.
 * @property architecture Technical details about the model's architecture and capabilities.
 * @property topProvider Information about the primary provider offering this model.
 * @property perRequestLimits Optional token limits that can be enforced per request.
 *
 * @see ModelList
 * @see Pricing
 * @see Architecture
 */
@Serializable
public data class Model(
    val id: String,
    val name: String,
    val description: String? = null,
    val pricing: Pricing,
    @SerialName("context_length")
    val contextLength: Int,
    val architecture: Architecture,
    @SerialName("top_provider")
    val topProvider: TopProvider,
    @SerialName("per_request_limits")
    val perRequestLimits: PerRequestLimits? = null
)

/**
 * Pricing information for a model.
 *
 * All costs are represented as strings containing decimal values in USD.
 * Pricing varies by operation type to accurately reflect model usage costs.
 *
 * @property prompt Cost per token for input/prompt tokens, typically formatted as dollars per token (e.g., "0.000001").
 * @property completion Cost per token for output/completion tokens.
 * @property request Cost per individual API request, regardless of token count.
 * @property image Cost per image processed, for multimodal models that support image inputs.
 *
 * @see Model
 */
@Serializable
public data class Pricing(
    val prompt: String,
    val completion: String,
    val request: String,
    val image: String
)

/**
 * Technical architecture details of a model.
 *
 * Describes the model's capabilities and technical characteristics that affect how it should be used.
 *
 * @property modality The type(s) of input the model supports (e.g., "text", "text+image", "multimodal").
 * @property tokenizer The tokenization method used by the model (e.g., "GPT", "Claude", "Llama").
 * @property instructType Optional instruction format the model expects (e.g., "alpaca", "vicuna", "none").
 *
 * @see Model
 */
@Serializable
public data class Architecture(
    val modality: String,
    val tokenizer: String,
    @SerialName("instruct_type")
    val instructType: String? = null
)

/**
 * Information about the primary provider offering this model.
 *
 * OpenRouter aggregates models from multiple providers. This class describes the characteristics
 * of the top (primary) provider's implementation of this model.
 *
 * @property contextLength Maximum context length supported by this provider's implementation.
 * @property maxCompletionTokens Optional maximum number of tokens that can be generated in a single completion.
 * @property isModerated Whether this provider applies content moderation to inputs and outputs.
 *
 * @see Model
 */
@Serializable
public data class TopProvider(
    @SerialName("context_length")
    val contextLength: Int,
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    @SerialName("is_moderated")
    val isModerated: Boolean
)

/**
 * Optional token limits that can be enforced per API request.
 *
 * These limits constrain the number of tokens that can be used in a single request,
 * providing additional control beyond the model's native context length.
 *
 * @property promptTokens Optional maximum number of tokens allowed in the input prompt.
 * @property completionTokens Optional maximum number of tokens allowed in the generated completion.
 *
 * @see Model
 */
@Serializable
public data class PerRequestLimits(
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerialName("completion_tokens")
    val completionTokens: Int? = null
)
