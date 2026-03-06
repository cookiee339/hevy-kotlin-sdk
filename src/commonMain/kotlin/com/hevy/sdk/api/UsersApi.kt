package com.hevy.sdk.api

import com.hevy.sdk.model.user.UserInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Hevy Users API — provides access to user info endpoints.
 *
 * @param httpClient a configured Ktor [HttpClient] (created by [com.hevy.sdk.common.HttpClientFactory]).
 * @param baseUrl the API base URL (defaults via [com.hevy.sdk.common.ApiConstants]).
 */
class UsersApi internal constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    /**
     * GET /v1/user/info — returns the authenticated user's profile info.
     *
     * Note: The API wraps the response in `{"data": ...}` which is unwrapped automatically.
     */
    suspend fun getInfo(): UserInfo {
        return httpClient.get("$baseUrl/v1/user/info")
            .body<UserInfoResponse>()
            .data
    }
}

/** Internal wrapper for GET /v1/user/info which returns {"data": ...}. */
@Serializable
internal data class UserInfoResponse(
    @SerialName("data") val data: UserInfo,
)
