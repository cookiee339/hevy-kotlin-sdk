package com.hevy.sdk

import com.hevy.sdk.common.ApiConstants
import io.ktor.client.HttpClient

/**
 * Configuration for [HevyClient].
 *
 * Not a `data class` because [httpClient] is a mutable, stateful resource
 * that breaks `equals`/`copy` semantics, and [apiKey] must not leak in `toString`.
 *
 * @property apiKey Hevy API key (UUID). Required.
 * @property baseUrl API base URL. Defaults to the Hevy production URL.
 * @property httpClient Internal. Optional pre-configured Ktor [HttpClient]. When null, the SDK creates one.
 */
class HevyClientConfig(
    internal val apiKey: String,
    val baseUrl: String = ApiConstants.BASE_URL,
    internal val httpClient: HttpClient? = null,
) {
    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank" }
        require(baseUrl.isNotBlank()) { "baseUrl must not be blank" }
        require(baseUrl.startsWith("https://")) {
            "baseUrl must use HTTPS to protect the API key in transit"
        }
    }

    /** Masks [apiKey] to prevent accidental leakage in logs. */
    override fun toString(): String =
        "HevyClientConfig(apiKey=***, baseUrl=$baseUrl, httpClient=${if (httpClient != null) "<provided>" else "null"})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HevyClientConfig) return false
        return apiKey == other.apiKey && baseUrl == other.baseUrl
    }

    // apiKey participates in hashCode for correct multi-key HashMap behaviour.
    // hashCode is not reversible, so the leakage risk is negligible.
    override fun hashCode(): Int {
        var result = apiKey.hashCode()
        result = 31 * result + baseUrl.hashCode()
        return result
    }
}
