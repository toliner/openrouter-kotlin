package dev.toliner.openrouter.l1.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Code challenge method for OAuth PKCE flow.
 *
 * PKCE (Proof Key for Code Exchange) is an OAuth 2.0 security extension
 * that prevents authorization code interception attacks.
 *
 * @see AuthCodeRequest
 * @see AuthKeyRequest
 */
@Serializable
public enum class CodeChallengeMethod {
    /**
     * SHA-256 hashing method (recommended for security).
     */
    @SerialName("S256") S256,
    
    /**
     * Plain text method (less secure, use only when S256 is not available).
     */
    @SerialName("plain") PLAIN
}

/**
 * Request to create authorization code for OAuth PKCE flow.
 *
 * This request initiates the OAuth authorization flow by generating an
 * authorization URL. The user should be redirected to this URL to complete
 * the authorization process.
 *
 * @property callbackUrl The callback URL to redirect to after authorization
 * @property codeChallenge PKCE code challenge for enhanced security (derived from code_verifier)
 * @property codeChallengeMethod Method used to generate the code challenge (S256 recommended)
 * @property limit Optional credit limit for the API key (in USD)
 * @see AuthCodeResponse
 * @see CodeChallengeMethod
 */
@Serializable
public data class AuthCodeRequest(
    @SerialName("callback_url") val callbackUrl: String,
    @SerialName("code_challenge") val codeChallenge: String? = null,
    @SerialName("code_challenge_method") val codeChallengeMethod: CodeChallengeMethod? = null,
    @SerialName("limit") val limit: Double? = null
)

/**
 * Response containing authorization URL.
 *
 * After creating an authorization code request, this response contains the URL
 * where the user should be redirected to complete the OAuth authorization flow.
 *
 * @property data Authorization URL data
 * @see AuthCodeRequest
 * @see AuthCodeResponseData
 */
@Serializable
public data class AuthCodeResponse(
    @SerialName("data") val data: AuthCodeResponseData
)

/**
 * Authorization URL data.
 *
 * Contains the actual URL for the OAuth authorization page.
 *
 * @property authorizationUrl The URL to redirect the user to for authorization
 * @see AuthCodeResponse
 */
@Serializable
public data class AuthCodeResponseData(
    @SerialName("authorization_url") val authorizationUrl: String
)

/**
 * Request to exchange authorization code for API key.
 *
 * After the user completes authorization and is redirected back to your callback URL
 * with an authorization code, use this request to exchange the code for an API key.
 *
 * @property code The authorization code received from the OAuth redirect
 * @property codeVerifier The code verifier (used if code_challenge was provided in AuthCodeRequest)
 * @property codeChallengeMethod Method used to generate the code challenge (must match AuthCodeRequest)
 * @see AuthKeyResponse
 * @see AuthCodeRequest
 * @see CodeChallengeMethod
 */
@Serializable
public data class AuthKeyRequest(
    @SerialName("code") val code: String,
    @SerialName("code_verifier") val codeVerifier: String? = null,
    @SerialName("code_challenge_method") val codeChallengeMethod: CodeChallengeMethod? = null
)

/**
 * Response containing the exchanged API key.
 *
 * After successfully exchanging an authorization code, this response contains
 * the API key that can be used to authenticate OpenRouter API requests.
 *
 * @property key The API key to use for OpenRouter requests (format: sk-or-v1-...)
 * @property userId User ID associated with the API key
 * @see AuthKeyRequest
 */
@Serializable
public data class AuthKeyResponse(
    @SerialName("key") val key: String,
    @SerialName("user_id") val userId: String? = null
)
