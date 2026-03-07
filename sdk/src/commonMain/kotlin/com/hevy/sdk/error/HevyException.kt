package com.hevy.sdk.error

/**
 * Sealed exception hierarchy for Hevy API errors.
 *
 * Each subclass maps to a specific HTTP failure mode, carrying
 * enough context for callers to handle programmatically.
 */
sealed class HevyException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** HTTP status code, or null for non-HTTP errors (e.g. network). */
    abstract val statusCode: Int?

    /** 401 — invalid or missing API key. */
    class Unauthorized(message: String) : HevyException(message) {
        override val statusCode: Int = 401
    }

    /** 400 — malformed request (invalid page size, bad body, etc.). */
    class BadRequest(message: String) : HevyException(message) {
        override val statusCode: Int = 400
    }

    /** 404 — requested resource does not exist. */
    class NotFound(message: String) : HevyException(message) {
        override val statusCode: Int = 404
    }

    /** 429 — rate limit exceeded. */
    class RateLimited(
        val retryAfterSeconds: Int? = null,
    ) : HevyException(
            buildString {
                append("Rate limited.")
                if (retryAfterSeconds != null) append(" Retry after $retryAfterSeconds seconds.")
            },
        ) {
        override val statusCode: Int = 429
    }

    /** 5xx — server-side failure. */
    class ServerError(
        override val statusCode: Int,
        message: String,
    ) : HevyException(message)

    /** Network-level failure (DNS, timeout, connection refused, etc.). */
    class NetworkError(cause: Throwable) : HevyException(
        message = "Network error: unable to reach the server",
        cause = cause,
    ) {
        override val statusCode: Int? = null
    }

    /** Catch-all for unexpected HTTP status codes. */
    class Unknown(
        override val statusCode: Int,
        message: String,
    ) : HevyException(message)
}
