package com.hevy.sdk.model.workout

import com.hevy.sdk.model.common.SetType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single set within a workout exercise (API response shape). */
@Serializable
data class WorkoutSet(
    val index: Int,
    val type: SetType,
    @SerialName("weight_kg") val weightKg: Double? = null,
    val reps: Double? = null,
    @SerialName("distance_meters") val distanceMeters: Double? = null,
    @SerialName("duration_seconds") val durationSeconds: Double? = null,
    val rpe: Double? = null,
    @SerialName("custom_metric") val customMetric: Double? = null,
)
