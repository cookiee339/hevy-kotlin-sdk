package com.hevy.sdk.model.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A complete routine as returned by the Hevy API. */
@Serializable
data class Routine(
    val id: String,
    val title: String,
    @SerialName("folder_id") val folderId: Int? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("created_at") val createdAt: String,
    val exercises: List<RoutineExercise>,
)
