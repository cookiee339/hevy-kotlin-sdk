package com.hevy.sdk.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HevyExceptionTest {
    @Test
    fun unauthorizedCarriesMessage() {
        val ex = HevyException.Unauthorized("Invalid API key")

        assertIs<HevyException>(ex)
        assertEquals("Invalid API key", ex.message)
        assertEquals(401, ex.statusCode)
    }

    @Test
    fun badRequestCarriesMessage() {
        val ex = HevyException.BadRequest("Invalid page size")

        assertEquals("Invalid page size", ex.message)
        assertEquals(400, ex.statusCode)
    }

    @Test
    fun notFoundCarriesMessage() {
        val ex = HevyException.NotFound("Workout not found")

        assertEquals("Workout not found", ex.message)
        assertEquals(404, ex.statusCode)
    }

    @Test
    fun rateLimitedCarriesRetryAfter() {
        val ex = HevyException.RateLimited(retryAfterSeconds = 30)

        assertEquals(429, ex.statusCode)
        assertEquals(30, ex.retryAfterSeconds)
        assertEquals("Rate limited. Retry after 30 seconds.", ex.message)
    }

    @Test
    fun rateLimitedDefaultRetryAfter() {
        val ex = HevyException.RateLimited()

        assertIs<HevyException>(ex)
        assertEquals(null, ex.retryAfterSeconds)
    }

    @Test
    fun serverErrorCarriesStatusCode() {
        val ex = HevyException.ServerError(502, "Bad Gateway")

        assertEquals(502, ex.statusCode)
        assertEquals("Bad Gateway", ex.message)
    }

    @Test
    fun networkErrorWrapsCauseWithSafeMessage() {
        val cause = RuntimeException("Connection refused to /var/run/internal.sock")
        val ex = HevyException.NetworkError(cause)

        assertEquals(null, ex.statusCode)
        assertEquals("Network error: unable to reach the server", ex.message)
        assertEquals(cause, ex.cause)
    }

    @Test
    fun unknownErrorCarriesStatusAndBody() {
        val ex = HevyException.Unknown(418, "I'm a teapot")

        assertEquals(418, ex.statusCode)
        assertEquals("I'm a teapot", ex.message)
    }
}
