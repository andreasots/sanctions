package eu.qrpth.sanctions.cli

import eu.qrpth.sanctions.jobs.DownloadLists
import picocli.CommandLine
import javax.inject.Inject

@CommandLine.Command(name = "download", mixinStandardHelpOptions = true)
class ManualDownload(@Inject private val job: DownloadLists): Runnable {
    override fun run() {
        job.execute(null)
    }
}
