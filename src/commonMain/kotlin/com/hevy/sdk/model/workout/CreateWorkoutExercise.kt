package com.hevy.sdk.model.workout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An exercise in a workout creation/update request. */
@Serializable
data class CreateWorkoutExercise(
    @SerialName("exercise_template_id") val exerciseTemplateId: String,
    @SerialName("superset_id") val supersetId: Int? = null,
    val notes: String? = null,
    val sets: List<CreateWorkoutSet>,
)
