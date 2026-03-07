package com.hevy.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import kotlin.test.assertFails

class OutputFormatterTest {

    @Test
    fun `Json format returns json block result`() {
        val formatter: OutputFormatter = OutputFormatter.Json
        val result = formatter.format(
            headers = listOf("ID"),
            rows = listOf(listOf("1")),
            jsonOutput = { """{"id": 1}""" },
        )
        assertEquals("""{"id": 1}""", result)
    }

    @Test
    fun `Json format does not evaluate table data`() {
        val formatter: OutputFormatter = OutputFormatter.Json
        val result = formatter.format(
            headers = emptyList(),
            rows = emptyList(),
            jsonOutput = { "json-output" },
        )
        assertEquals("json-output", result)
    }

    @Test
    fun `Table format returns table rendering`() {
        val formatter: OutputFormatter = OutputFormatter.Table
        val result = formatter.format(
            headers = listOf("ID", "Name"),
            rows = listOf(listOf("1", "Test")),
            jsonOutput = { throw AssertionError("should not be called") },
        )
        assertContains(result, "ID")
        assertContains(result, "Name")
        assertContains(result, "Test")
    }

    @Test
    fun `Table format with empty rows shows no results`() {
        val formatter: OutputFormatter = OutputFormatter.Table
        val result = formatter.format(
            headers = listOf("ID"),
            rows = emptyList(),
            jsonOutput = { throw AssertionError("should not be called") },
        )
        assertEquals("No results found.", result)
    }
}
