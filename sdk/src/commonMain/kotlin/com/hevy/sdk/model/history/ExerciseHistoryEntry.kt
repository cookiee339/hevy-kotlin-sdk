package com.hevy.sdk.model.history

import com.hevy.sdk.model.common.SetType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single exercise history entry as returned by the Hevy API. */
@Serializable
data class ExerciseHistoryEntry(
    @SerialName("workout_id") val workoutId: String,
    @SerialName("workout_title") val workoutTitle: String,
    @SerialName("workout_start_time") val workoutStartTime: String,
    @SerialName("workout_end_time") val workoutEndTime: String,
    @SerialName("exercise_template_id") val exerciseTemplateId: String,
    @SerialName("weight_kg") val weightKg: Double? = null,
    val reps: Int? = null,
    @SerialName("distance_meters") val distanceMeters: Int? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    val rpe: Double? = null,
    @SerialName("custom_metric") val customMetric: Double? = null,
    @SerialName("set_type") val setType: SetType,
)
