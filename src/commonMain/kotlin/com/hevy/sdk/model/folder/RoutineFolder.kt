package com.hevy.sdk.model.folder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A routine folder as returned by the Hevy API. */
@Serializable
data class RoutineFolder(
    val id: Int,
    val index: Int,
    val title: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("created_at") val createdAt: String,
)
