package eu.qrpth.sanctions.configuration

import io.ebean.EbeanServer
import io.ebean.EbeanServerFactory
import io.ebean.config.ServerConfig
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import javax.sql.DataSource

@Factory
class EbeanConfiguration {
    @Bean
    fun ebeanServer(dataSource: DataSource): EbeanServer {
        val config = ServerConfig()
        config.isDefaultServer = true
        config.dataSource = dataSource
        config.isAutoCommitMode = false

        return EbeanServerFactory.create(config)
    }
}
