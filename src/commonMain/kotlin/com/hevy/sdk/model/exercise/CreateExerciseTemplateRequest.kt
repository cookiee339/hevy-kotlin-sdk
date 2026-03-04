package com.hevy.sdk.model.exercise

import com.hevy.sdk.model.common.EquipmentCategory
import com.hevy.sdk.model.common.ExerciseType
import com.hevy.sdk.model.common.MuscleGroup
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wrapper for POST /v1/exercise_templates request body. */
@Serializable
data class CreateExerciseTemplateRequest(
    val exercise: CreateExerciseTemplateBody,
)

/** Body of the exercise template creation request. */
@Serializable
data class CreateExerciseTemplateBody(
    val title: String,
    @SerialName("exercise_type") val exerciseType: ExerciseType,
    @SerialName("equipment_category") val equipmentCategory: EquipmentCategory,
    @SerialName("muscle_group") val muscleGroup: MuscleGroup,
    @SerialName("other_muscles") val otherMuscles: List<MuscleGroup> = emptyList(),
)
