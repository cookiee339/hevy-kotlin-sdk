package com.hevy.sdk.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Type of a workout or routine set. */
@Serializable
enum class SetType {
    @SerialName("warmup")
    WARMUP,

    @SerialName("normal")
    NORMAL,

    @SerialName("failure")
    FAILURE,

    @SerialName("dropset")
    DROPSET,

    /** Fallback for unrecognised values from the API. */
    UNKNOWN,
}
