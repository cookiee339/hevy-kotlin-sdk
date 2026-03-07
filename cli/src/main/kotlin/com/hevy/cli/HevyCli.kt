package com.hevy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption

class CliContext(
    var apiKeyFlag: String? = null,
    var outputFormatter: OutputFormatter = OutputFormatter.Table,
)

class HevyCli : CliktCommand(name = "hevy") {

    private val apiKey by option("--api-key", help = "Hevy API key (overrides HEVY_API_KEY env var)")
    private val json by option("--json", help = "Output raw JSON instead of formatted table").flag()

    val cliContext by findOrSetObject { CliContext() }

    init {
        versionOption(VERSION)
    }

    override fun run() {
        cliContext.apiKeyFlag = apiKey
        cliContext.outputFormatter = if (json) OutputFormatter.Json else OutputFormatter.Table
    }

    companion object {
        const val VERSION = "0.1.0"
    }
}
