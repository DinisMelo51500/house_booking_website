package pt.isel.ls.houses.data.mem

import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.Token
import pt.isel.ls.houses.domain.User
import kotlin.uuid.ExperimentalUuidApi

const val DEFAULT_SKIP = 0
const val DEFAULT_LIMIT = 50

interface UserRepository {
    fun save(
        name: Name,
        email: Email,
        password: String,
    ): User

    fun findById(id: Id): User?

    fun findByEmail(email: String): User?

    fun findAll(
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<User>

    @OptIn(ExperimentalUuidApi::class)
    fun findByToken(token: Token): User?

    fun findByNameAndPassword(
        name: Name,
        password: String,
    ): User?
}

class MemUserRepository : UserRepository {
    private val users = HashMap<UInt, User>()
    private var idGenerator = 1

    override fun save(
        name: Name,
        email: Email,
        password: String,
    ): User {
        if (users.values.any { it.email == email }) {
            throw IllegalArgumentException("User with email $email already exists")
        }
        val user =
            User(
                id = Id(idGenerator),
                name = name,
                email = email,
                password = password,
            )
        idGenerator++
        users[user.id] = user
        return user
    }

    override fun findById(id: Id): User? = users[id]

    override fun findByEmail(email: String): User? = users.values.find { it.email.value == email }

    override fun findAll(
        skip: Int,
        limit: Int,
    ): List<User> =
        users.values
            .toList()
            .drop(skip)
            .take(limit)

    @OptIn(ExperimentalUuidApi::class)
    override fun findByToken(token: Token): User? = users.values.find { it.token == token }

    fun clear() {
        users.clear()
        idGenerator = 1
    }

    override fun findByNameAndPassword(
        name: Name,
        password: String,
    ): User? = users.values.find { it.name == name && it.password == password }
}
