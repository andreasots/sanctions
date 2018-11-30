package eu.qrpth.sanctions.quartz

import eu.qrpth.sanctions.jobs.DownloadLists
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.discovery.event.ServiceShutdownEvent
import io.micronaut.discovery.event.ServiceStartedEvent
import io.micronaut.runtime.event.annotation.EventListener
import org.quartz.*
import java.util.*
import javax.inject.Inject

@Context
@Requires(notEnv = [Environment.CLI])
class QuartzContext(@Inject private val scheduler: Scheduler) {
    init {
        scheduleDownloadLists()
    }

    private fun scheduleDownloadLists() {
        val detail = JobBuilder.newJob(DownloadLists::class.java)
                .withIdentity("DownloadLists")
                .build()

        val trigger = TriggerBuilder.newTrigger()
                .forJob(detail)
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?").inTimeZone(TimeZone.getTimeZone("Europe/Tallinn")))
                .withIdentity("DownloadLists")
                .build()

        scheduler.scheduleJob(detail, setOf(trigger), true)
    }

    @EventListener
    fun startup(@Suppress("UNUSED_PARAMETER") e: ServiceStartedEvent) = scheduler.start()

    @EventListener
    fun shutdown(@Suppress("UNUSED_PARAMETER") e: ServiceShutdownEvent) = scheduler.shutdown()
}
