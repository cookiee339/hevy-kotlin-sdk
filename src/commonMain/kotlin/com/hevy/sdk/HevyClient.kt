package com.hevy.sdk

import com.hevy.sdk.api.ExerciseHistoryApi
import com.hevy.sdk.api.ExerciseTemplatesApi
import com.hevy.sdk.api.RoutineFoldersApi
import com.hevy.sdk.api.RoutinesApi
import com.hevy.sdk.api.UsersApi
import com.hevy.sdk.api.WorkoutsApi
import com.hevy.sdk.common.HttpClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

/**
 * Main entry point for the Hevy API SDK.
 *
 * ```kotlin
 * val hevy = HevyClient(apiKey = "your-uuid-key")
 * val workouts = hevy.workouts.list(page = 1, pageSize = 5)
 * hevy.close()
 * ```
 *
 * Use [HevyClientConfig] to customize the base URL or provide your own [HttpClient].
 */
class HevyClient private constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : AutoCloseable {
    val workouts: WorkoutsApi = WorkoutsApi(httpClient, baseUrl)
    val routines: RoutinesApi = RoutinesApi(httpClient, baseUrl)
    val exerciseTemplates: ExerciseTemplatesApi = ExerciseTemplatesApi(httpClient, baseUrl)
    val routineFolders: RoutineFoldersApi = RoutineFoldersApi(httpClient, baseUrl)
    val exerciseHistory: ExerciseHistoryApi = ExerciseHistoryApi(httpClient, baseUrl)
    val users: UsersApi = UsersApi(httpClient, baseUrl)

    constructor(config: HevyClientConfig) : this(
        httpClient = HttpClientFactory.createOrUse(config),
        baseUrl = config.baseUrl,
    )

    constructor(apiKey: String) : this(HevyClientConfig(apiKey = apiKey))

    /**
     * Closes the underlying [HttpClient], releasing resources.
     * Safe to call multiple times.
     */
    override fun close() {
        httpClient.close()
    }

    companion object {
        /**
         * Creates a [HevyClient] with a specific [HttpClientEngine] — primarily for testing.
         */
        internal fun create(
            apiKey: String,
            engine: HttpClientEngine,
        ): HevyClient {
            val config = HevyClientConfig(apiKey = apiKey)
            val httpClient = HttpClientFactory.create(config, engine)
            return HevyClient(httpClient = httpClient, baseUrl = config.baseUrl)
        }
    }
}
