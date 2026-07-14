package pt.isel.ls.houses.data.jdbc

import pt.isel.ls.houses.data.mem.UserRepository
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.Token
import pt.isel.ls.houses.domain.User
import java.sql.Connection
import java.sql.ResultSet
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class JDBCUserRepository(
    private val connection: Connection? = null,
) : UserRepository {
    private fun <T> execute(block: (Connection) -> T): T =
        if (connection != null) {
            block(connection)
        } else {
            withConnection { block(it) }
        }

    override fun save(
        name: Name,
        email: Email,
        password: String,
    ): User =
        execute { connection ->
            val sql =
                """
                INSERT INTO users (user_name, email, token, password)
                VALUES (?, ?, ?::uuid, ?)
                RETURNING id_user
                """.trimIndent()

            val token = Token.random()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, name.value)
                stmt.setString(2, email.value)
                stmt.setString(3, token.toString())
                stmt.setString(4, password)

                val rs = stmt.executeQuery()
                if (rs.next()) {
                    User(
                        id = rs.getInt("id_user").toUInt(),
                        name = name,
                        email = email,
                        token = token,
                        password = password,
                    )
                } else {
                    throw Exception("Failed to insert user")
                }
            }
        }

    override fun findById(id: Id): User? =
        execute { connection ->
            val sql = "SELECT * FROM users WHERE id_user = ?"

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id.toInt())

                val rs = stmt.executeQuery()
                if (rs.next()) rs.toUser() else null
            }
        }

    override fun findByEmail(email: String): User? =
        execute { connection ->
            val sql = "SELECT * FROM users WHERE email = ?"

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, email)

                val rs = stmt.executeQuery()
                if (rs.next()) rs.toUser() else null
            }
        }

    override fun findAll(
        skip: Int,
        limit: Int,
    ): List<User> =
        execute { connection ->
            val sql = "SELECT * FROM users OFFSET ? LIMIT ?"

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, skip)
                stmt.setInt(2, limit)

                val rs = stmt.executeQuery()
                val users = mutableListOf<User>()
                while (rs.next()) users.add(rs.toUser())
                users
            }
        }

    override fun findByToken(token: Token): User? =
        execute { connection ->
            val sql = "SELECT * FROM users WHERE token = ?::uuid"

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, token.toString())

                val rs = stmt.executeQuery()
                if (rs.next()) rs.toUser() else null
            }
        }

    override fun findByNameAndPassword(
        name: Name,
        password: String,
    ): User? =
        execute { connection ->
            val sql = "SELECT * FROM users WHERE user_name = ? AND password = ?"

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, name.value)
                stmt.setString(2, password)

                val rs = stmt.executeQuery()
                if (rs.next()) rs.toUser() else null
            }
        }
}

@OptIn(ExperimentalUuidApi::class)
private fun ResultSet.toUser() =
    User(
        id = getInt("id_user").toUInt(),
        name = Name(getString("user_name")),
        email = Email(getString("email")),
        token = Uuid.parse(getString("token")),
        password = getString("password"),
    )
