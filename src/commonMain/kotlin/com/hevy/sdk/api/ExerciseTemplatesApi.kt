package com.hevy.sdk.api

import com.hevy.sdk.common.ApiConstants
import com.hevy.sdk.common.Page
import com.hevy.sdk.common.Validation
import com.hevy.sdk.model.exercise.CreateExerciseTemplateRequest
import com.hevy.sdk.model.exercise.CreateExerciseTemplateResponse
import com.hevy.sdk.model.exercise.ExerciseTemplate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Hevy Exercise Templates API — provides access to exercise template endpoints.
 *
 * @param httpClient a configured Ktor [HttpClient] (created by [com.hevy.sdk.common.HttpClientFactory]).
 * @param baseUrl the API base URL (defaults via [com.hevy.sdk.common.ApiConstants]).
 */
class ExerciseTemplatesApi internal constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    /**
     * GET /v1/exercise_templates — paginated list of exercise templates.
     *
     * Note: This endpoint supports up to 100 items per page (vs 10 for most other endpoints).
     */
    suspend fun list(
        page: Int = 1,
        pageSize: Int = 5,
    ): Page<ExerciseTemplate> {
        Validation.validatePage(page)
        Validation.validatePageSize(pageSize, ApiConstants.MAX_EXERCISE_TEMPLATE_PAGE_SIZE)

        val response =
            httpClient.get("$baseUrl/v1/exercise_templates") {
                parameter("page", page)
                parameter("pageSize", pageSize)
            }.body<ExerciseTemplatesResponse>()

        return Page(
            page = response.page,
            pageCount = response.pageCount,
            items = response.exerciseTemplates,
        )
    }

    /**
     * POST /v1/exercise_templates — create a new custom exercise template.
     *
     * Note: Unlike other create endpoints, this returns only the ID of the created template.
     */
    suspend fun create(request: CreateExerciseTemplateRequest): CreateExerciseTemplateResponse {
        require(request.exercise.title.isNotBlank()) { "exercise title must not be blank" }

        return httpClient.post("$baseUrl/v1/exercise_templates") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /** GET /v1/exercise_templates/{exerciseTemplateId} — single template by ID. */
    suspend fun get(exerciseTemplateId: String): ExerciseTemplate {
        Validation.validateId(exerciseTemplateId, "exerciseTemplateId")

        return httpClient.get("$baseUrl/v1/exercise_templates/$exerciseTemplateId")
            .body()
    }
}

/** Internal response shape for GET /v1/exercise_templates. */
@Serializable
internal data class ExerciseTemplatesResponse(
    val page: Int,
    @SerialName("page_count") val pageCount: Int,
    @SerialName("exercise_templates") val exerciseTemplates: List<ExerciseTemplate>,
)
