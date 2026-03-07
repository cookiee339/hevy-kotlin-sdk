package com.hevy.sdk.model.workout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A complete workout as returned by the Hevy API. */
@Serializable
data class Workout(
    val id: String,
    val title: String,
    @SerialName("routine_id") val routineId: String? = null,
    val description: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("created_at") val createdAt: String,
    val exercises: List<WorkoutExercise>,
)
