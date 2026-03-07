package com.hevy.cli

import com.hevy.cli.format.TablePrinter

sealed interface OutputFormatter {

    fun format(
        headers: List<String>,
        rows: List<List<String>>,
        jsonOutput: () -> String,
    ): String

    data object Json : OutputFormatter {
        override fun format(
            headers: List<String>,
            rows: List<List<String>>,
            jsonOutput: () -> String,
        ): String = jsonOutput()
    }

    data object Table : OutputFormatter {
        override fun format(
            headers: List<String>,
            rows: List<List<String>>,
            jsonOutput: () -> String,
        ): String = TablePrinter.print(headers, rows)
    }
}
