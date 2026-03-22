package dev.toliner.openrouter.l1.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Code challenge method for OAuth PKCE flow.
 */
@Serializable
enum class CodeChallengeMethod {
    @SerialName("S256") S256,
    @SerialName("plain") PLAIN
}

/**
 * Request to create authorization code for OAuth PKCE flow.
 *
 * @property callbackUrl The callback URL to redirect to after authorization
 * @property codeChallenge PKCE code challenge for enhanced security
 * @property codeChallengeMethod Method used to generate the code challenge
 * @property limit Credit limit for the API key
 */
@Serializable
data class AuthCodeRequest(
    @SerialName("callback_url") val callbackUrl: String,
    @SerialName("code_challenge") val codeChallenge: String? = null,
    @SerialName("code_challenge_method") val codeChallengeMethod: CodeChallengeMethod? = null,
    @SerialName("limit") val limit: Double? = null
)

/**
 * Response containing authorization URL.
 *
 * @property data Authorization URL data
 */
@Serializable
data class AuthCodeResponse(
    @SerialName("data") val data: AuthCodeResponseData
)

/**
 * Authorization URL data.
 *
 * @property authorizationUrl The URL to redirect the user to for authorization
 */
@Serializable
data class AuthCodeResponseData(
    @SerialName("authorization_url") val authorizationUrl: String
)

/**
 * Request to exchange authorization code for API key.
 *
 * @property code The authorization code received from the OAuth redirect
 * @property codeVerifier The code verifier if code_challenge was used in the authorization request
 * @property codeChallengeMethod Method used to generate the code challenge
 */
@Serializable
data class AuthKeyRequest(
    @SerialName("code") val code: String,
    @SerialName("code_verifier") val codeVerifier: String? = null,
    @SerialName("code_challenge_method") val codeChallengeMethod: CodeChallengeMethod? = null
)

/**
 * Response containing the exchanged API key.
 *
 * @property key The API key to use for OpenRouter requests
 * @property userId User ID associated with the API key
 */
@Serializable
data class AuthKeyResponse(
    @SerialName("key") val key: String,
    @SerialName("user_id") val userId: String? = null
)
