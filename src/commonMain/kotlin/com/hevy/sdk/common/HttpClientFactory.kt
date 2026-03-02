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
     * Returns the user-provided [HevyClientConfig.httpClient] if present,
     * otherwise creates a new default-engine client with SDK defaults.
     *
     * **Security note:** A caller-provided [HttpClient] is used as-is.
     * It will **not** have the SDK's `api-key` header, JSON content negotiation,
     * or error-to-[com.hevy.sdk.error.HevyException] mapping installed.
     * Callers must configure their own client accordingly.
     */
    fun createOrUse(config: HevyClientConfig): HttpClient =
        config.httpClient ?: HttpClient {
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
