package com.hevy.sdk.api

import com.hevy.sdk.common.Page
import com.hevy.sdk.common.Validation
import com.hevy.sdk.model.workout.CreateWorkoutRequest
import com.hevy.sdk.model.workout.Workout
import com.hevy.sdk.model.workout.WorkoutEvent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Hevy Workouts API — provides access to all workout-related endpoints.
 *
 * @param httpClient a configured Ktor [HttpClient] (created by [com.hevy.sdk.common.HttpClientFactory]).
 * @param baseUrl the API base URL (defaults via [com.hevy.sdk.common.ApiConstants]).
 */
class WorkoutsApi internal constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    /** GET /v1/workouts — paginated list of workouts. */
    suspend fun list(
        page: Int = 1,
        pageSize: Int = 5,
    ): Page<Workout> {
        Validation.validatePage(page)
        Validation.validatePageSize(pageSize)

        val response =
            httpClient.get("$baseUrl/v1/workouts") {
                parameter("page", page)
                parameter("pageSize", pageSize)
            }.body<WorkoutsResponse>()

        return Page(
            page = response.page,
            pageCount = response.pageCount,
            items = response.workouts,
        )
    }

    /** POST /v1/workouts — create a new workout. */
    suspend fun create(request: CreateWorkoutRequest): Workout =
        httpClient.post("$baseUrl/v1/workouts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    /** GET /v1/workouts/count — total number of workouts. */
    suspend fun count(): Int =
        httpClient.get("$baseUrl/v1/workouts/count")
            .body<WorkoutCountResponse>()
            .workoutCount

    /** GET /v1/workouts/events — paginated workout events since a timestamp. */
    suspend fun events(
        page: Int = 1,
        pageSize: Int = 5,
        since: String = "1970-01-01T00:00:00Z",
    ): Page<WorkoutEvent> {
        Validation.validatePage(page)
        Validation.validatePageSize(pageSize)
        Validation.validateTimestamp(since, "since")

        val response =
            httpClient.get("$baseUrl/v1/workouts/events") {
                parameter("page", page)
                parameter("pageSize", pageSize)
                parameter("since", since)
            }.body<WorkoutEventsResponse>()

        return Page(
            page = response.page,
            pageCount = response.pageCount,
            items = response.events,
        )
    }

    /** GET /v1/workouts/{workoutId} — single workout by ID. */
    suspend fun get(workoutId: String): Workout {
        Validation.validateId(workoutId, "workoutId")

        return httpClient.get("$baseUrl/v1/workouts/$workoutId").body()
    }

    /** PUT /v1/workouts/{workoutId} — update an existing workout. */
    suspend fun update(
        workoutId: String,
        request: CreateWorkoutRequest,
    ): Workout {
        Validation.validateId(workoutId, "workoutId")

        return httpClient.put("$baseUrl/v1/workouts/$workoutId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

/** Internal response shape for GET /v1/workouts. */
@Serializable
internal data class WorkoutsResponse(
    val page: Int,
    @SerialName("page_count") val pageCount: Int,
    val workouts: List<Workout>,
)

/** Internal response shape for GET /v1/workouts/count. */
@Serializable
internal data class WorkoutCountResponse(
    @SerialName("workout_count") val workoutCount: Int,
)

/** Internal response shape for GET /v1/workouts/events. */
@Serializable
internal data class WorkoutEventsResponse(
    val page: Int,
    @SerialName("page_count") val pageCount: Int,
    val events: List<WorkoutEvent>,
)
