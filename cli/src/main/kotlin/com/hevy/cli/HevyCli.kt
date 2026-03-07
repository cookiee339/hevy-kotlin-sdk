package com.hevy.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.versionOption

class HevyCli : CliktCommand(name = "hevy") {
    init {
        versionOption(VERSION)
    }

    override fun run() = Unit

    companion object {
        const val VERSION = "0.1.0"
    }
}
