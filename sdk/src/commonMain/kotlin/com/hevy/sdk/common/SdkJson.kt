package com.hevy.sdk.common

import kotlinx.serialization.json.Json

/**
 * Single shared [Json] configuration used across the SDK.
 *
 * - `ignoreUnknownKeys`: allows the API to add new fields without breaking consumers.
 * - `coerceInputValues`: coerces unrecognised enum values to the field's default (typically `UNKNOWN`).
 *   This makes the SDK forward-compatible but silently masks API contract changes — callers should
 *   check for `UNKNOWN` enum values when precise handling of new API values is needed.
 */
internal object SdkJson {
    val instance: Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
}
