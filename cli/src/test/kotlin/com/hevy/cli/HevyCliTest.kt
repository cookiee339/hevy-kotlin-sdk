package com.hevy.cli

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class HevyCliTest {

    @Test
    fun `--help shows usage and command name`() {
        val result = HevyCli().test("--help")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "hevy")
        assertContains(result.stdout, "Options")
    }

    @Test
    fun `--version shows version string`() {
        val result = HevyCli().test("--version")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "hevy version")
    }

    @Test
    fun `no arguments succeeds`() {
        val result = HevyCli().test("")
        assertEquals(0, result.statusCode)
    }
}
