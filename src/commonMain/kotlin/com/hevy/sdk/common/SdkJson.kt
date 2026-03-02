package com.hevy.sdk.common

import kotlinx.serialization.json.Json

/** Single shared [Json] configuration used across the SDK. */
internal object SdkJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}
