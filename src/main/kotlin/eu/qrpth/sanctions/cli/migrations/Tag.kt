package eu.qrpth.sanctions.cli.migrations

import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import picocli.CommandLine
import javax.inject.Inject
import javax.sql.DataSource

@CommandLine.Command(name = "tag", mixinStandardHelpOptions = true)
class Tag(@Inject private val dataSource: DataSource) : Runnable {
    @CommandLine.Parameters(index = "0", paramLabel = "TAG")
    private var tag: String = ""

    override fun run() {
        dataSource.connection.use {
            Liquibase("migrations.yml", ClassLoaderResourceAccessor(), JdbcConnection(it))
                    .tag(tag)
        }
    }
}