package pt.isel.ls.houses.services

import pt.isel.ls.houses.data.mem.MemUserRepository
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Name
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi

class UserServiceTests {
    private lateinit var userService: UserService

    @BeforeTest
    fun setup() {
        userService = UserService(MemUserRepository())
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `createUser returns id and token`() {
        val (id, token) = userService.createUser(Name("Alice"), Email("alice@test.com"), "password")

        assertNotNull(id)
        assertNotNull(token)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `createUser generates unique ids`() {
        val (id1, _) = userService.createUser(Name("User1"), Email("u1@test.com"), "password")
        val (id2, _) = userService.createUser(Name("User2"), Email("u2@test.com"), "password")

        assertNotEquals(id1, id2)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `getUserById returns user`() {
        val (id, _) = userService.createUser(Name("Bob"), Email("bob@test.com"), "password")
        val user = userService.getUserById(id)

        assertNotNull(user)
        assertEquals("Bob", user.name.value)
    }

    @Test
    fun `getUserById returns null if not found`() {
        val user = userService.getUserById(9999U)
        assertNull(user)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `getAllUsers returns all users with paging`() {
        userService.createUser(Name("U1"), Email("u1@test.com"), "password")
        userService.createUser(Name("U2"), Email("u2@test.com"), "password")
        userService.createUser(Name("U3"), Email("u3@test.com"), "password")

        val allUsers = userService.getAllUsers()
        val pagedUsers = userService.getAllUsers(skip = 1, limit = 1)

        assertEquals(3, allUsers.size)
        assertEquals(1, pagedUsers.size)
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun `getUserByNameAndPassword returns user`() {
        userService.createUser(Name("Alice"), Email("alice@test.com"), "password")

        val (id, token) = userService.getUserByNameAndPassword(Name("Alice"), "password")

        assertNotNull(id)
        assertNotNull(token)
    }
}
