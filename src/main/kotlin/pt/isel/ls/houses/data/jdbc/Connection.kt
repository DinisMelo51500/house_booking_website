package pt.isel.ls.houses.data.jdbc

import org.postgresql.ds.PGSimpleDataSource
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource

private val dataSource: DataSource =
    PGSimpleDataSource().apply {
        setURL(System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/db_ls_2526_2_42d_g02")
        user = System.getenv("DB_USER") ?: "db_ls_2526_2_42d_g02_user"
        password = System.getenv("DB_PASSWORD") ?: "Y9vl1LqekFoFhRrN7DRZpgUIwXnjULCG"
    }

fun <T> withConnection(task: (Connection) -> T): T =
    try {
        dataSource.connection.use { connection ->
            task(connection)
        }
    } catch (e: SQLException) {
        throw RuntimeException("Database error: ${e.message}", e)
    }

fun withRollback(testBlock: (Connection) -> Unit) {
    withConnection { connection ->
        try {
            connection.autoCommit = false
            testBlock(connection)
        } finally {
            connection.rollback()
        }
    }
}
