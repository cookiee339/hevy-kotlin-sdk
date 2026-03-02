package com.hevy.sdk.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Muscle group targeted by an exercise. */
@Serializable
enum class MuscleGroup {
    @SerialName("abdominals")
    ABDOMINALS,

    @SerialName("shoulders")
    SHOULDERS,

    @SerialName("biceps")
    BICEPS,

    @SerialName("triceps")
    TRICEPS,

    @SerialName("forearms")
    FOREARMS,

    @SerialName("quadriceps")
    QUADRICEPS,

    @SerialName("hamstrings")
    HAMSTRINGS,

    @SerialName("calves")
    CALVES,

    @SerialName("glutes")
    GLUTES,

    @SerialName("abductors")
    ABDUCTORS,

    @SerialName("adductors")
    ADDUCTORS,

    @SerialName("lats")
    LATS,

    @SerialName("upper_back")
    UPPER_BACK,

    @SerialName("traps")
    TRAPS,

    @SerialName("lower_back")
    LOWER_BACK,

    @SerialName("chest")
    CHEST,

    @SerialName("cardio")
    CARDIO,

    @SerialName("neck")
    NECK,

    @SerialName("full_body")
    FULL_BODY,

    @SerialName("other")
    OTHER,
}
