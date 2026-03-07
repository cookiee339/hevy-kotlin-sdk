package com.hevy.cli.format

object TablePrinter {

    fun print(headers: List<String>, rows: List<List<String>>): String {
        if (rows.isEmpty()) return "No results found."

        val columnWidths = headers.mapIndexed { index, header ->
            val maxValueWidth = rows.maxOf { row -> row.getOrElse(index) { "" }.length }
            maxOf(header.length, maxValueWidth)
        }

        val header = formatRow(headers, columnWidths)
        val separator = columnWidths.joinToString("  ") { "-".repeat(it) }
        val body = rows.joinToString("\n") { formatRow(it, columnWidths) }
        return "$header\n$separator\n$body"
    }

    private fun formatRow(values: List<String>, widths: List<Int>): String =
        values.mapIndexed { index, value ->
            if (index < widths.lastIndex) {
                value.padEnd(widths[index])
            } else {
                value
            }
        }.joinToString("  ")
}
