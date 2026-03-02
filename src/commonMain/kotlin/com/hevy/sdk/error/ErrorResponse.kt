package com.hevy.sdk.error

import kotlinx.serialization.Serializable

/**
 * Matches the Hevy API error body shape: `{"error": "message"}`.
 */
@Serializable
data class ErrorResponse(
    val error: String,
)
