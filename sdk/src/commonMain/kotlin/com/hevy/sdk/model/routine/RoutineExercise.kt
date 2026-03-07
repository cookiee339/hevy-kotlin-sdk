package com.hevy.sdk.model.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A single exercise within a routine (API response shape). */
@Serializable
data class RoutineExercise(
    val index: Int,
    val title: String,
    /**
     * Rest duration in seconds, returned by the Hevy API as a JSON string (e.g. "90").
     * Note the intentional String type here — the API serializes this differently
     * from the numeric `rest_seconds` in create/update request bodies.
     */
    @SerialName("rest_seconds") val restSeconds: String,
    val notes: String? = null,
    @SerialName("exercise_template_id") val exerciseTemplateId: String,
    @SerialName("supersets_id") val supersetsId: Int? = null,
    val sets: List<RoutineSet>,
)
