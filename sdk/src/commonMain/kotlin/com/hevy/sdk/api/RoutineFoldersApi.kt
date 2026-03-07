package com.hevy.sdk.api

import com.hevy.sdk.common.Page
import com.hevy.sdk.common.Validation
import com.hevy.sdk.model.folder.CreateRoutineFolderRequest
import com.hevy.sdk.model.folder.RoutineFolder
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
 * Hevy Routine Folders API — provides access to routine folder endpoints.
 *
 * @param httpClient a configured Ktor [HttpClient] (created by [com.hevy.sdk.common.HttpClientFactory]).
 * @param baseUrl the API base URL (defaults via [com.hevy.sdk.common.ApiConstants]).
 */
class RoutineFoldersApi internal constructor(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    /** GET /v1/routine_folders — paginated list of routine folders. */
    suspend fun list(
        page: Int = 1,
        pageSize: Int = 5,
    ): Page<RoutineFolder> {
        Validation.validatePage(page)
        Validation.validatePageSize(pageSize)

        val response =
            httpClient.get("$baseUrl/v1/routine_folders") {
                parameter("page", page)
                parameter("pageSize", pageSize)
            }.body<RoutineFoldersResponse>()

        return Page(
            page = response.page,
            pageCount = response.pageCount,
            items = response.routineFolders,
        )
    }

    /** POST /v1/routine_folders — create a new routine folder. */
    suspend fun create(request: CreateRoutineFolderRequest): RoutineFolder {
        require(request.routineFolder.title.isNotBlank()) { "folder title must not be blank" }

        return httpClient.post("$baseUrl/v1/routine_folders") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /** GET /v1/routine_folders/{folderId} — single routine folder by ID. */
    suspend fun get(folderId: Int): RoutineFolder {
        Validation.validateIntId(folderId, "folderId")

        return httpClient.get("$baseUrl/v1/routine_folders/$folderId")
            .body()
    }
}

/** Internal response shape for GET /v1/routine_folders. */
@Serializable
internal data class RoutineFoldersResponse(
    val page: Int,
    @SerialName("page_count") val pageCount: Int,
    @SerialName("routine_folders") val routineFolders: List<RoutineFolder>,
)
