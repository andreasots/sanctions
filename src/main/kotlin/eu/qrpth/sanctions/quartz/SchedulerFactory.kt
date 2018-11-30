package eu.qrpth.sanctions.quartz

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import java.util.*
import javax.inject.Inject

@Factory
class SchedulerFactory(@Inject private val ctx: ApplicationContext, @Inject private val jobFactory: MicronautJobFactory) {
    private val factory: StdSchedulerFactory = StdSchedulerFactory()

    init {
        val properties = Properties()
        properties.load(javaClass.classLoader.getResourceAsStream("quartz.properties"))
        for (name in properties.stringPropertyNames()) {
            val value = ctx.resolvePlaceholders(properties.getProperty(name))
                    .orElseThrow { RuntimeException("failed to resolve placeholders in $name") }
            properties.setProperty(name, value)
        }
        factory.initialize(properties)
    }

    @Bean
    fun scheduler(): Scheduler = factory.scheduler.apply {
        setJobFactory(jobFactory)
    }
}
