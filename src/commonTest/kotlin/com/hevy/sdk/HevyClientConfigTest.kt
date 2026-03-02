package com.hevy.sdk

import com.hevy.sdk.common.ApiConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HevyClientConfigTest {
    @Test
    fun defaultConfigUsesBaseUrlAndNoCustomClient() {
        val config = HevyClientConfig(apiKey = "test-api-key")

        assertEquals("test-api-key", config.apiKey)
        assertEquals(ApiConstants.BASE_URL, config.baseUrl)
        assertNull(config.httpClient)
    }

    @Test
    fun customBaseUrl() {
        val config =
            HevyClientConfig(
                apiKey = "key",
                baseUrl = "https://custom.api.com",
            )

        assertEquals("https://custom.api.com", config.baseUrl)
    }

    @Test
    fun blankApiKeyThrows() {
        assertFailsWith<IllegalArgumentException> {
            HevyClientConfig(apiKey = "")
        }
    }

    @Test
    fun blankBaseUrlThrows() {
        assertFailsWith<IllegalArgumentException> {
            HevyClientConfig(apiKey = "key", baseUrl = "  ")
        }
    }

    @Test
    fun httpBaseUrlThrows() {
        assertFailsWith<IllegalArgumentException> {
            HevyClientConfig(apiKey = "key", baseUrl = "http://api.hevyapp.com")
        }
    }

    @Test
    fun httpsBaseUrlIsAccepted() {
        val config = HevyClientConfig(apiKey = "key", baseUrl = "https://api.example.com")

        assertEquals("https://api.example.com", config.baseUrl)
    }

    @Test
    fun toStringMasksApiKey() {
        val config = HevyClientConfig(apiKey = "super-secret-key-123")

        val str = config.toString()

        assertTrue(str.contains("***"))
        assertFalse(str.contains("super-secret-key-123"))
    }

    @Test
    fun equalityIgnoresHttpClient() {
        val a = HevyClientConfig(apiKey = "key", baseUrl = "https://a.com")
        val b = HevyClientConfig(apiKey = "key", baseUrl = "https://a.com")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
