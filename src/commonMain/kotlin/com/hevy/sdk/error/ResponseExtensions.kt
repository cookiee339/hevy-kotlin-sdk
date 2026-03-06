package com.hevy.sdk.error

import com.hevy.sdk.common.SdkJson
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

/**
 * Throws the appropriate [HevyException] if this response is not successful.
 *
 * Maps HTTP status codes to the sealed exception hierarchy:
 * - 400 → [HevyException.BadRequest]
 * - 401 → [HevyException.Unauthorized]
 * - 404 → [HevyException.NotFound]
 * - 429 → [HevyException.RateLimited]
 * - 5xx → [HevyException.ServerError]
 * - other → [HevyException.Unknown]
 */
suspend fun HttpResponse.ensureSuccess() {
    if (status.isSuccess()) return

    val body = bodyAsText()
    val errorMessage = parseErrorMessage(body)

    throw when (status.value) {
        400 -> HevyException.BadRequest(errorMessage ?: "Bad request")
        401 -> HevyException.Unauthorized(errorMessage ?: "Unauthorized")
        404 -> HevyException.NotFound(errorMessage ?: "Not found")
        429 -> {
            val retryAfter = headers["Retry-After"]?.toIntOrNull()
            HevyException.RateLimited(retryAfter)
        }
        in 500..599 -> HevyException.ServerError(status.value, errorMessage ?: "Server error")
        else -> HevyException.Unknown(status.value, errorMessage ?: "HTTP ${status.value}")
    }
}

/** Attempts to extract the `error` field from a JSON error body. */
private fun parseErrorMessage(body: String): String? =
    if (body.isBlank()) {
        null
    } else {
        runCatching {
            SdkJson.instance.decodeFromString<ErrorResponse>(body).error
        }.getOrNull()?.let(::sanitizeMessage)
    }

/** Strips control characters and truncates to prevent log injection from server-supplied error strings. */
private fun sanitizeMessage(raw: String): String = raw.replace(Regex("[\r\n\t]"), " ").take(500)
