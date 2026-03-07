package com.hevy.cli.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class TablePrinterTest {

    @Test
    fun `empty rows prints no results message`() {
        val result = TablePrinter.print(listOf("ID", "Name"), emptyList())
        assertEquals("No results found.", result)
    }

    @Test
    fun `single row renders with header and separator`() {
        val result = TablePrinter.print(
            headers = listOf("ID", "Name"),
            rows = listOf(listOf("1", "Test")),
        )
        val lines = result.lines()
        assertEquals(3, lines.size)
        assertEquals("ID  Name", lines[0])
        assertEquals("--  ----", lines[1])
        assertEquals("1   Test", lines[2])
    }

    @Test
    fun `column widths adapt to longest value`() {
        val result = TablePrinter.print(
            headers = listOf("ID", "Name"),
            rows = listOf(
                listOf("1", "Short"),
                listOf("100", "A Longer Name"),
            ),
        )
        val lines = result.lines()
        assertEquals(4, lines.size)
        assertEquals("ID   Name", lines[0].trimEnd())
        assertEquals("---  -------------", lines[1].trimEnd())
        assertEquals("1    Short", lines[2].trimEnd())
        assertEquals("100  A Longer Name", lines[3].trimEnd())
    }

    @Test
    fun `header width used when wider than all values`() {
        val result = TablePrinter.print(
            headers = listOf("Identifier", "N"),
            rows = listOf(listOf("1", "A")),
        )
        val lines = result.lines()
        assertEquals("Identifier  N", lines[0].trimEnd())
        assertEquals("----------  -", lines[1].trimEnd())
        assertEquals("1           A", lines[2].trimEnd())
    }

    @Test
    fun `empty headers and rows returns no results`() {
        val result = TablePrinter.print(emptyList(), emptyList())
        assertEquals("No results found.", result)
    }

    @Test
    fun `single column renders correctly`() {
        val result = TablePrinter.print(
            headers = listOf("Name"),
            rows = listOf(listOf("Alice"), listOf("Bob")),
        )
        val lines = result.lines()
        assertEquals("Name", lines[0].trimEnd())
        assertEquals("-----", lines[1].trimEnd())
        assertEquals("Alice", lines[2].trimEnd())
        assertEquals("Bob", lines[3].trimEnd())
    }
}
