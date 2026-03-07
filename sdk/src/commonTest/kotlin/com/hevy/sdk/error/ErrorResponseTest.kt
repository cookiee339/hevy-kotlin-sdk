package com.hevy.sdk.error

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorResponseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun deserializesErrorField() {
        val body = """{"error": "Invalid request body"}"""

        val response = json.decodeFromString<ErrorResponse>(body)

        assertEquals("Invalid request body", response.error)
    }

    @Test
    fun serializesRoundTrip() {
        val original = ErrorResponse(error = "Something went wrong")

        val encoded = json.encodeToString(ErrorResponse.serializer(), original)
        val decoded = json.decodeFromString<ErrorResponse>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun deserializesWithExtraFields() {
        val body = """{"error": "Bad request", "details": "extra info"}"""

        val response = json.decodeFromString<ErrorResponse>(body)

        assertEquals("Bad request", response.error)
    }
}
