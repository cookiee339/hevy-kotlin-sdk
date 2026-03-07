package com.hevy.sdk.model.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An exercise in a routine creation request (POST). */
@Serializable
data class CreateRoutineExercise(
    @SerialName("exercise_template_id") val exerciseTemplateId: String,
    @SerialName("superset_id") val supersetId: Int? = null,
    @SerialName("rest_seconds") val restSeconds: Int? = null,
    val notes: String? = null,
    val sets: List<CreateRoutineSet>,
)
