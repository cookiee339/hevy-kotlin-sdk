package com.hevy.sdk.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Type of exercise, determining which set fields are relevant. */
@Serializable
enum class ExerciseType {
    @SerialName("weight_reps")
    WEIGHT_REPS,

    @SerialName("reps_only")
    REPS_ONLY,

    @SerialName("bodyweight_reps")
    BODYWEIGHT_REPS,

    @SerialName("bodyweight_assisted_reps")
    BODYWEIGHT_ASSISTED_REPS,

    @SerialName("duration")
    DURATION,

    @SerialName("weight_duration")
    WEIGHT_DURATION,

    @SerialName("distance_duration")
    DISTANCE_DURATION,

    @SerialName("short_distance_weight")
    SHORT_DISTANCE_WEIGHT,
}
