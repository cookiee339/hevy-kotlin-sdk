package com.hevy.sdk.model.routine

import kotlinx.serialization.Serializable

/**
 * Request body for updating a routine (PUT /v1/routines/{routineId}).
 *
 * Wraps the routine data in a `{"routine": {...}}` envelope.
 * Note: Unlike [CreateRoutineRequest], the update request has no folder_id field.
 */
@Serializable
data class UpdateRoutineRequest(
    val routine: UpdateRoutineBody,
)

/** Inner routine object within an [UpdateRoutineRequest]. */
@Serializable
data class UpdateRoutineBody(
    val title: String,
    val notes: String? = null,
    val exercises: List<CreateRoutineExercise>,
)
