package com.hevy.sdk.model.folder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for POST /v1/routine_folders. */
@Serializable
data class CreateRoutineFolderRequest(
    @SerialName("routine_folder") val routineFolder: CreateRoutineFolderBody,
)

/** Inner body for creating a routine folder. */
@Serializable
data class CreateRoutineFolderBody(
    val title: String,
)
