package com.hevy.sdk.model.workout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for creating or updating a workout.
 *
 * Wraps the workout data in a `{"workout": {...}}` envelope
 * as required by the Hevy API.
 */
@Serializable
data class CreateWorkoutRequest(
    val workout: CreateWorkoutBody,
)

/** Inner workout object within a [CreateWorkoutRequest]. */
@Serializable
data class CreateWorkoutBody(
    val title: String,
    val description: String? = null,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("is_private") val isPrivate: Boolean = false,
    val exercises: List<CreateWorkoutExercise>,
)
