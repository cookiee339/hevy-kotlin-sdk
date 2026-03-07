package com.hevy.cli

import com.hevy.sdk.HevyClient
import com.hevy.sdk.HevyClientConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

object ClientProvider {

    fun resolveApiKey(
        flagValue: String?,
        envValue: String? = System.getenv("HEVY_API_KEY"),
    ): String {
        val fromFlag = flagValue?.takeIf { it.isNotBlank() }
        val fromEnv = envValue?.takeIf { it.isNotBlank() }
        return fromFlag
            ?: fromEnv
            ?: throw MissingApiKeyException()
    }

    fun createClient(apiKey: String): HevyClient {
        val cioClient = HttpClient(CIO)
        return HevyClient(HevyClientConfig(apiKey = apiKey, httpClient = cioClient))
    }
}

class MissingApiKeyException : RuntimeException(
    "API key required. Set HEVY_API_KEY or use --api-key.",
)
