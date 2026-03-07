package com.hevy.sdk.model.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for creating a routine (POST /v1/routines).
 *
 * Wraps the routine data in a `{"routine": {...}}` envelope
 * as required by the Hevy API.
 */
@Serializable
data class CreateRoutineRequest(
    val routine: CreateRoutineBody,
)

/** Inner routine object within a [CreateRoutineRequest]. */
@Serializable
data class CreateRoutineBody(
    val title: String,
    @SerialName("folder_id") val folderId: Int? = null,
    val notes: String? = null,
    val exercises: List<CreateRoutineExercise>,
)
