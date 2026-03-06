package com.hevy.sdk.api

import com.hevy.sdk.common.Validation
import com.hevy.sdk.model.history.ExerciseHistoryEntry
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Hevy Exercise History API — provides access to exercise history endpoints.
 *
 * @param httpClient a configured Ktor [HttpClient] (created by [com.hevy.sdk.common.HttpClientFactory]).
 * @param baseUrl the API base URL (defaults via [com.hevy.sdk.common.ApiConstants]).
 */
class ExerciseHistoryApi internal constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    /**
     * GET /v1/exercise_history/{exerciseTemplateId} — exercise history for a template.
     *
     * @param exerciseTemplateId the exercise template to fetch history for.
     * @param startDate optional ISO 8601 start date filter (e.g. "2024-01-01T00:00:00Z").
     * @param endDate optional ISO 8601 end date filter (e.g. "2024-12-31T23:59:59Z").
     */
    suspend fun getByTemplateId(
        exerciseTemplateId: String,
        startDate: String? = null,
        endDate: String? = null,
    ): List<ExerciseHistoryEntry> {
        Validation.validateId(exerciseTemplateId, "exerciseTemplateId")
        startDate?.let { require(it.isNotBlank()) { "startDate must not be blank" } }
        endDate?.let { require(it.isNotBlank()) { "endDate must not be blank" } }

        val response =
            httpClient.get("$baseUrl/v1/exercise_history/$exerciseTemplateId") {
                startDate?.let { parameter("start_date", it) }
                endDate?.let { parameter("end_date", it) }
            }.body<ExerciseHistoryResponse>()

        return response.exerciseHistory
    }
}

/** Internal response shape for GET /v1/exercise_history/{id}. */
@Serializable
internal data class ExerciseHistoryResponse(
    @SerialName("exercise_history") val exerciseHistory: List<ExerciseHistoryEntry>,
)
