package com.hevy.sdk.model.exercise

import kotlinx.serialization.Serializable

/**
 * Response from POST /v1/exercise_templates.
 *
 * Unlike other create endpoints, this only returns the ID of the created template.
 */
@Serializable
data class CreateExerciseTemplateResponse(
    val id: Int,
)
