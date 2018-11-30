package eu.qrpth.sanctions.cli

import eu.qrpth.sanctions.cli.migrations.Rollback
import eu.qrpth.sanctions.cli.migrations.Tag
import eu.qrpth.sanctions.cli.migrations.Update
import picocli.CommandLine

@CommandLine.Command(name = "migrations", subcommands = [Update::class, Rollback::class, Tag::class], mixinStandardHelpOptions = true)
class Migrations : Runnable {
    override fun run() {
        TODO("not implemented")
    }
}
