package com.hevy.sdk.api

import com.hevy.sdk.common.Page
import com.hevy.sdk.common.Validation
import com.hevy.sdk.model.routine.CreateRoutineRequest
import com.hevy.sdk.model.routine.Routine
import com.hevy.sdk.model.routine.UpdateRoutineRequest
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
 * Hevy Routines API — provides access to all routine-related endpoints.
 *
 * @param httpClient a configured Ktor [HttpClient] (created by [com.hevy.sdk.common.HttpClientFactory]).
 * @param baseUrl the API base URL (defaults via [com.hevy.sdk.common.ApiConstants]).
 */
class RoutinesApi internal constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    /** GET /v1/routines — paginated list of routines. */
    suspend fun list(
        page: Int = 1,
        pageSize: Int = 5,
    ): Page<Routine> {
        Validation.validatePage(page)
        Validation.validatePageSize(pageSize)

        val response =
            httpClient.get("$baseUrl/v1/routines") {
                parameter("page", page)
                parameter("pageSize", pageSize)
            }.body<RoutinesResponse>()

        return Page(
            page = response.page,
            pageCount = response.pageCount,
            items = response.routines,
        )
    }

    /** POST /v1/routines — create a new routine. */
    suspend fun create(request: CreateRoutineRequest): Routine {
        require(request.routine.title.isNotBlank()) { "routine title must not be blank" }

        return httpClient.post("$baseUrl/v1/routines") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * GET /v1/routines/{routineId} — single routine by ID.
     *
     * Note: This endpoint wraps the response in `{"routine": ...}`,
     * unlike other GET-by-ID endpoints.
     */
    suspend fun get(routineId: String): Routine {
        Validation.validateId(routineId, "routineId")

        return httpClient.get("$baseUrl/v1/routines/$routineId")
            .body<RoutineWrapper>()
            .routine
    }

    /** PUT /v1/routines/{routineId} — update an existing routine. */
    suspend fun update(
        routineId: String,
        request: UpdateRoutineRequest,
    ): Routine {
        Validation.validateId(routineId, "routineId")
        require(request.routine.title.isNotBlank()) { "routine title must not be blank" }

        return httpClient.put("$baseUrl/v1/routines/$routineId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

/** Internal response shape for GET /v1/routines. */
@Serializable
internal data class RoutinesResponse(
    val page: Int,
    @SerialName("page_count") val pageCount: Int,
    val routines: List<Routine>,
)

/** Internal wrapper for GET /v1/routines/{id} which returns {"routine": ...}. */
@Serializable
internal data class RoutineWrapper(
    val routine: Routine,
)
