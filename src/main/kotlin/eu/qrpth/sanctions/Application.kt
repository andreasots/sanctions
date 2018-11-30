package eu.qrpth.sanctions

import eu.qrpth.sanctions.cli.ManualDownload
import eu.qrpth.sanctions.cli.Migrations
import eu.qrpth.sanctions.jobs.DownloadLists
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.runtime.Micronaut
import picocli.CommandLine.Command

@Command(name = "sanctions", mixinStandardHelpOptions = true, subcommands = [Migrations::class, ManualDownload::class])
object Application: Runnable {
    @JvmStatic
    fun main(args: Array<String>) {
        // TODO: determine if there's a better way to do this
        if (args.isNotEmpty()) {
            PicocliRunner.run(Application.javaClass, *args)
        } else {
            Micronaut.build()
                    .packages("eu.qrpth.sanctions")
                    .mainClass(Application.javaClass)
                    .start()
        }
    }

    override fun run() {
    }
}
