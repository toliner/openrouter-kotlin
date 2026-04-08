package dev.toliner.openrouter.l1.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Quantization level for filtering providers by model weight precision.
 *
 * @see ProviderPreferences.quantizations
 */
@Serializable
public enum class Quantization {
    @SerialName("int4")
    INT4,

    @SerialName("int8")
    INT8,

    @SerialName("fp4")
    FP4,

    @SerialName("fp6")
    FP6,

    @SerialName("fp8")
    FP8,

    @SerialName("fp16")
    FP16,

    @SerialName("bf16")
    BF16,

    @SerialName("fp32")
    FP32,

    @SerialName("unknown")
    UNKNOWN
}
