package com.hevy.sdk.model.workout

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * A workout event from the events endpoint.
 *
 * Discriminated by the `type` field:
 * - `"updated"` → [Updated] containing the full [Workout]
 * - `"deleted"` → [Deleted] containing the workout ID and deletion timestamp
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class WorkoutEvent {
    @Serializable
    @SerialName("updated")
    data class Updated(val workout: Workout) : WorkoutEvent()

    @Serializable
    @SerialName("deleted")
    data class Deleted(
        val id: String,
        @SerialName("deleted_at") val deletedAt: String,
    ) : WorkoutEvent()
}
