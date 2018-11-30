package eu.qrpth.sanctions.quartz

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Infrastructure
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle
import javax.inject.Inject

@Infrastructure
class MicronautJobFactory(@Inject val beanContext: BeanContext): JobFactory {
    override fun newJob(bundle: TriggerFiredBundle?, scheduler: Scheduler?): Job =
        beanContext.findBean(bundle!!.jobDetail.jobClass)
                .orElseThrow {
                    SchedulerException("failed to create an instance of ${bundle.jobDetail.jobClass.canonicalName}")
                }
}