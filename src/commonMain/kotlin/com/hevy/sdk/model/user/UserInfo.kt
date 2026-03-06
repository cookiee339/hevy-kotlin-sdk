package com.hevy.sdk.model.user

import kotlinx.serialization.Serializable

/** Authenticated user profile info as returned by the Hevy API. */
@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    val url: String,
)
