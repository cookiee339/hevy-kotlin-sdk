package com.hevy.sdk.common

import com.hevy.sdk.HevyClientConfig
import com.hevy.sdk.error.ensureSuccess
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json

/**
 * Creates configured [HttpClient] instances for Hevy API communication.
 *
 * Sets up:
 * - `api-key` header on every request
 * - JSON content negotiation
 * - Automatic error response → [com.hevy.sdk.error.HevyException] mapping
 */
internal object HttpClientFactory {
    /**
     * Creates a new [HttpClient] using the given [engine] with SDK defaults installed.
     *
     * Primarily used for testing (pass [io.ktor.client.engine.mock.MockEngine]).
     */
    fun create(
        config: HevyClientConfig,
        engine: HttpClientEngine,
    ): HttpClient =
        HttpClient(engine) {
            install(config)
        }

    /**
     * Returns a new default-engine client with SDK defaults,
     * or wraps the user-provided [HevyClientConfig.httpClient] with SDK plugins.
     *
     * Even when a caller provides their own [HttpClient], the SDK installs
     * the `api-key` header, JSON content negotiation, and error mapping
     * on top of it via [io.ktor.client.HttpClient.config].
     */
    fun createOrUse(config: HevyClientConfig): HttpClient =
        config.httpClient?.config {
            install(config)
        } ?: HttpClient {
            install(config)
        }

    private fun io.ktor.client.HttpClientConfig<*>.install(config: HevyClientConfig) {
        install(ContentNegotiation) {
            json(SdkJson.instance)
        }

        defaultRequest {
            headers.append(ApiConstants.API_KEY_HEADER, config.apiKey)
        }

        HttpResponseValidator {
            validateResponse { response ->
                response.ensureSuccess()
            }
        }
    }
}
