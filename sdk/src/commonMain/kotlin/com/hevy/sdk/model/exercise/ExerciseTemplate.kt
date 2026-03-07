package com.hevy.sdk.model.exercise

import com.hevy.sdk.model.common.ExerciseType
import com.hevy.sdk.model.common.MuscleGroup
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** An exercise template as returned by the Hevy API. */
@Serializable
data class ExerciseTemplate(
    val id: String,
    val title: String,
    val type: ExerciseType,
    @SerialName("primary_muscle_group") val primaryMuscleGroup: MuscleGroup,
    @SerialName("secondary_muscle_groups") val secondaryMuscleGroups: List<MuscleGroup>,
    @SerialName("is_custom") val isCustom: Boolean,
)
