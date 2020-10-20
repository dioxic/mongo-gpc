package uk.dioxic.mongo.gpc

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import uk.dioxic.mongo.gpc.cli.LoadCsv
import uk.dioxic.mongo.gpc.cli.LoadCustom
import uk.dioxic.mongo.gpc.cli.LoadXml

class Cli : CliktCommand() {
    override fun run() = Unit
}

@ExperimentalStdlibApi
fun main(args: Array<String>) = Cli()
    .subcommands(LoadXml(), LoadCsv(), LoadCustom())
    .main(args)