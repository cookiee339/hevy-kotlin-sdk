package com.hevy.sdk.model.workout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single exercise within a workout (API response shape). */
@Serializable
data class WorkoutExercise(
    val index: Int,
    val title: String,
    val notes: String? = null,
    @SerialName("exercise_template_id") val exerciseTemplateId: String,
    @SerialName("supersets_id") val supersetsId: Int? = null,
    val sets: List<WorkoutSet>,
)
