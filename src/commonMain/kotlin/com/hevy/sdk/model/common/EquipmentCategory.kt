package com.hevy.sdk.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Category of equipment used for an exercise. */
@Serializable
enum class EquipmentCategory {
    @SerialName("none")
    NONE,

    @SerialName("barbell")
    BARBELL,

    @SerialName("dumbbell")
    DUMBBELL,

    @SerialName("kettlebell")
    KETTLEBELL,

    @SerialName("machine")
    MACHINE,

    @SerialName("plate")
    PLATE,

    @SerialName("resistance_band")
    RESISTANCE_BAND,

    @SerialName("suspension")
    SUSPENSION,

    @SerialName("other")
    OTHER,
}
