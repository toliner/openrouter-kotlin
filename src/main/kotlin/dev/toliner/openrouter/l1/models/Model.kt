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
 * @property canonicalSlug Canonical slug for the model.
 * @property huggingFaceId Hugging Face model identifier, if applicable.
 * @property name Human-readable display name of the model.
 * @property created Unix timestamp of when the model was created.
 * @property description Optional detailed description of the model's capabilities and use cases.
 * @property pricing Cost structure for using this model across different operations.
 * @property contextLength Maximum number of tokens the model can process in a single request.
 * @property architecture Technical details about the model's architecture and capabilities.
 * @property topProvider Information about the primary provider offering this model.
 * @property perRequestLimits Token limits that can be enforced per request, or null if none.
 * @property supportedParameters List of supported parameters for this model.
 * @property defaultParameters Default parameter values for this model, or null if none.
 * @property knowledgeCutoff The date up to which the model was trained on data (ISO 8601 YYYY-MM-DD), or null if unknown.
 * @property expirationDate The date after which the model may be removed (ISO 8601 YYYY-MM-DD), or null if no expiration.
 * @property links Related API endpoints and resources for this model.
 *
 * @see ModelList
 * @see Pricing
 * @see Architecture
 */
@Serializable
public data class Model(
    @SerialName("id")
    val id: String,
    @SerialName("canonical_slug")
    val canonicalSlug: String,
    @SerialName("hugging_face_id")
    val huggingFaceId: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("created")
    val created: Long,
    @SerialName("description")
    val description: String? = null,
    @SerialName("pricing")
    val pricing: Pricing,
    @SerialName("context_length")
    val contextLength: Int,
    @SerialName("architecture")
    val architecture: Architecture,
    @SerialName("top_provider")
    val topProvider: TopProvider,
    @SerialName("per_request_limits")
    val perRequestLimits: PerRequestLimits?,
    @SerialName("supported_parameters")
    val supportedParameters: List<String>,
    @SerialName("default_parameters")
    val defaultParameters: DefaultParameters?,
    @SerialName("knowledge_cutoff")
    val knowledgeCutoff: String? = null,
    @SerialName("expiration_date")
    val expirationDate: String? = null,
    @SerialName("links")
    val links: ModelLinks
)

/**
 * Pricing information for a model.
 *
 * All costs are represented as strings containing decimal values in USD per token/unit.
 * Only [prompt] and [completion] are required; all other fields are optional and depend
 * on the model's capabilities.
 *
 * @property prompt Cost per token for input/prompt tokens (e.g., "0.000001").
 * @property completion Cost per token for output/completion tokens.
 * @property request Cost per individual API request, regardless of token count.
 * @property image Cost per image processed, for multimodal models that support image inputs.
 * @property imageToken Cost per image token.
 * @property imageOutput Cost per output image.
 * @property audio Cost per audio input unit.
 * @property audioOutput Cost per audio output unit.
 * @property inputAudioCache Cost per cached audio input unit.
 * @property webSearch Cost per web search operation.
 * @property internalReasoning Cost per internal reasoning token.
 * @property inputCacheRead Cost per cached input token read.
 * @property inputCacheWrite Cost per cached input token write.
 * @property discount Discount multiplier applied to pricing.
 *
 * @see Model
 */
@Serializable
public data class Pricing(
    @SerialName("prompt")
    val prompt: String,
    @SerialName("completion")
    val completion: String,
    @SerialName("request")
    val request: String? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("image_token")
    val imageToken: String? = null,
    @SerialName("image_output")
    val imageOutput: String? = null,
    @SerialName("audio")
    val audio: String? = null,
    @SerialName("audio_output")
    val audioOutput: String? = null,
    @SerialName("input_audio_cache")
    val inputAudioCache: String? = null,
    @SerialName("web_search")
    val webSearch: String? = null,
    @SerialName("internal_reasoning")
    val internalReasoning: String? = null,
    @SerialName("input_cache_read")
    val inputCacheRead: String? = null,
    @SerialName("input_cache_write")
    val inputCacheWrite: String? = null,
    @SerialName("discount")
    val discount: Double? = null
)

/**
 * Technical architecture details of a model.
 *
 * Describes the model's capabilities and technical characteristics that affect how it should be used.
 *
 * @property tokenizer The tokenization method used by the model (e.g., "GPT", "Claude", "Llama").
 * @property instructType Optional instruction format the model expects (e.g., "alpaca", "vicuna", "none").
 * @property modality The primary modality of the model (e.g., "text->text", "text+image->text").
 * @property inputModalities List of supported input modalities (e.g., "text", "image", "audio").
 * @property outputModalities List of supported output modalities (e.g., "text", "image", "audio").
 *
 * @see Model
 */
@Serializable
public data class Architecture(
    @SerialName("tokenizer")
    val tokenizer: String? = null,
    @SerialName("instruct_type")
    val instructType: String? = null,
    @SerialName("modality")
    val modality: String?,
    @SerialName("input_modalities")
    val inputModalities: List<String>,
    @SerialName("output_modalities")
    val outputModalities: List<String>
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
    val contextLength: Int? = null,
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    @SerialName("is_moderated")
    val isModerated: Boolean
)

/**
 * Token limits that can be enforced per API request.
 *
 * These limits constrain the number of tokens that can be used in a single request,
 * providing additional control beyond the model's native context length.
 * The entire object may be null in the [Model] response when no limits are enforced.
 *
 * @property promptTokens Maximum number of tokens allowed in the input prompt.
 * @property completionTokens Maximum number of tokens allowed in the generated completion.
 *
 * @see Model
 */
@Serializable
public data class PerRequestLimits(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int
)

/**
 * Default parameter values for a model.
 *
 * When present, these values are used as defaults for the model if not explicitly
 * overridden in the request. The entire object may be null in the [Model] response.
 *
 * @property temperature Default sampling temperature.
 * @property topP Default nucleus sampling parameter.
 * @property topK Default top-k sampling parameter.
 * @property frequencyPenalty Default frequency penalty.
 * @property presencePenalty Default presence penalty.
 * @property repetitionPenalty Default repetition penalty.
 *
 * @see Model
 */
@Serializable
public data class DefaultParameters(
    @SerialName("temperature")
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("top_k")
    val topK: Int? = null,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Double? = null,
    @SerialName("presence_penalty")
    val presencePenalty: Double? = null,
    @SerialName("repetition_penalty")
    val repetitionPenalty: Double? = null
)

/**
 * Related API endpoints and resources for a model.
 *
 * @property details URL for the model details/endpoints API.
 *
 * @see Model
 */
@Serializable
public data class ModelLinks(
    @SerialName("details")
    val details: String
)
