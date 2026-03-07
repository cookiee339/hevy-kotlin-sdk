package com.hevy.cli

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertIs

class HevyCliTest {

    @Test
    fun `--help shows usage and command name`() {
        val result = HevyCli().test("--help")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "hevy")
        assertContains(result.stdout, "Options")
    }

    @Test
    fun `--help shows api-key option`() {
        val result = HevyCli().test("--help")
        assertContains(result.stdout, "--api-key")
    }

    @Test
    fun `--help shows json option`() {
        val result = HevyCli().test("--help")
        assertContains(result.stdout, "--json")
    }

    @Test
    fun `--version shows version string`() {
        val result = HevyCli().test("--version")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "hevy version")
    }

    @Test
    fun `no arguments succeeds with Table formatter`() {
        val cli = HevyCli()
        val result = cli.test("")
        assertEquals(0, result.statusCode)
        assertIs<OutputFormatter.Table>(cli.cliContext.outputFormatter)
    }

    @Test
    fun `--json sets Json formatter`() {
        val cli = HevyCli()
        cli.test("--json")
        assertIs<OutputFormatter.Json>(cli.cliContext.outputFormatter)
    }

    @Test
    fun `--api-key stores key in context`() {
        val cli = HevyCli()
        cli.test("--api-key my-test-key")
        assertEquals("my-test-key", cli.cliContext.apiKeyFlag)
    }

    @Test
    fun `no api-key flag stores null in context`() {
        val cli = HevyCli()
        cli.test("")
        assertEquals(null, cli.cliContext.apiKeyFlag)
    }
}
