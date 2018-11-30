package eu.qrpth.sanctions.cli.migrations

import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import picocli.CommandLine
import javax.inject.Inject
import javax.sql.DataSource

@CommandLine.Command(name = "update", mixinStandardHelpOptions = true)
class Update(@Inject private val dataSource: DataSource): Runnable {
    override fun run() {
        dataSource.connection.use {
            val liquibase = Liquibase("migrations.yml", ClassLoaderResourceAccessor(), JdbcConnection(it))
            liquibase.reportStatus(true, "", System.out.writer())
            liquibase.update("")
            println(liquibase.databaseChangeLog.changeSets)
        }
    }
}
