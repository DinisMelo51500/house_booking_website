package pt.isel.ls.houses.services

import pt.isel.ls.houses.data.jdbc.JDBCUserRepository
import pt.isel.ls.houses.data.mem.UserRepository
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.Token
import pt.isel.ls.houses.domain.User
import kotlin.uuid.ExperimentalUuidApi

const val DEFAULT_SKIP = 0
const val DEFAULT_LIMIT = 50

class UserService(
    private val repo: UserRepository = JDBCUserRepository(),
) {
    @OptIn(ExperimentalUuidApi::class)
    fun createUser(
        name: Name,
        email: Email,
        password: String,
    ): Pair<Id, Token> {
        val user = repo.save(name, email, password)
        return user.id to user.token
    }

    fun getUserById(id: Id): User? = repo.findById(id)

    fun getAllUsers(
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<User> = repo.findAll(skip, limit)

    @OptIn(ExperimentalUuidApi::class)
    fun getUserByToken(token: Token): User =
        repo.findByToken(token)
            ?: throw IllegalArgumentException("Invalid token")

    @OptIn(ExperimentalUuidApi::class)
    fun getUserByNameAndPassword(
        name: Name,
        password: String,
    ): Pair<Id, Token> {
        val user =
            repo.findByNameAndPassword(name, password)
                ?: throw IllegalArgumentException("User not found")
        return user.id to user.token
    }
}
