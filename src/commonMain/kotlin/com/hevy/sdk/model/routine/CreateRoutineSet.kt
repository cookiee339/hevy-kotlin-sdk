package com.hevy.sdk.model.routine

import com.hevy.sdk.model.common.SetType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A set in a routine creation request (POST). */
@Serializable
data class CreateRoutineSet(
    val type: SetType,
    @SerialName("weight_kg") val weightKg: Double? = null,
    val reps: Int? = null,
    @SerialName("distance_meters") val distanceMeters: Int? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("custom_metric") val customMetric: Double? = null,
    @SerialName("rep_range") val repRange: RepRange? = null,
)
