package com.hevy.sdk.model.routine

import kotlinx.serialization.Serializable

/** A rep range with optional start and end values. */
@Serializable
data class RepRange(
    val start: Int? = null,
    val end: Int? = null,
)
