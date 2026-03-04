package com.hevy.sdk.model.workout

import com.hevy.sdk.model.common.SetType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A set in a workout creation/update request. */
@Serializable
data class CreateWorkoutSet(
    val type: SetType,
    @SerialName("weight_kg") val weightKg: Double? = null,
    val reps: Int? = null,
    @SerialName("distance_meters") val distanceMeters: Int? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("custom_metric") val customMetric: Double? = null,
    val rpe: Double? = null,
)
