package com.hevy.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

class ClientProviderTest {

    @Test
    fun `flag value takes priority over env var`() {
        val key = ClientProvider.resolveApiKey(flagValue = "flag-key", envValue = "env-key")
        assertEquals("flag-key", key)
    }

    @Test
    fun `env var used when no flag provided`() {
        val key = ClientProvider.resolveApiKey(flagValue = null, envValue = "env-key")
        assertEquals("env-key", key)
    }

    @Test
    fun `blank flag falls back to env var`() {
        val key = ClientProvider.resolveApiKey(flagValue = "  ", envValue = "env-key")
        assertEquals("env-key", key)
    }

    @Test
    fun `missing key throws with helpful message`() {
        val exception = assertFailsWith<MissingApiKeyException> {
            ClientProvider.resolveApiKey(flagValue = null, envValue = null)
        }
        assertContains(exception.message!!, "HEVY_API_KEY")
        assertContains(exception.message!!, "--api-key")
    }

    @Test
    fun `blank flag and blank env throws`() {
        assertFailsWith<MissingApiKeyException> {
            ClientProvider.resolveApiKey(flagValue = "", envValue = "  ")
        }
    }
}
