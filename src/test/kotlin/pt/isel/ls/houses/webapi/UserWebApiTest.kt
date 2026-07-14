package pt.isel.ls.houses.webapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import pt.isel.ls.houses.data.mem.MemUserRepository
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.services.UserService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserWebApiTest {
    private val repo = MemUserRepository()
    private val service = UserService(repo)
    private val app = UserWebApi(service).routes

    // --- POST /users ---

    @Test
    fun `postUser returns 201 with id and token`() {
        val response =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        assertEquals(CREATED, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertNotNull(json["id"])
        assertNotNull(json["token"])
    }

    @Test
    fun `postUser returns 400 when name is missing`() {
        val response =
            app(
                Request(POST, "/users")
                    .body("""{"email":"alice@example.com"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postUser returns 400 when email is missing`() {
        val response =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postUser returns 400 when email is duplicate`() {
        app(
            Request(POST, "/users")
                .body("""{"name":"Alice","email":"alice@example.com"}"""),
        )

        val response =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice2","email":"alice@example.com"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    // --- GET /users/{id} ---

    @Test
    fun `getUserById returns 200 with correct user`() {
        val created = repo.save(Name("Alice"), Email("alice@example.com"), "password")

        val response = app(Request(GET, "/users/${created.id}"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals("Alice", json["name"]?.jsonPrimitive?.content)
        assertEquals("alice@example.com", json["email"]?.jsonPrimitive?.content)
    }

    @Test
    fun `getUserById returns 404 when user does not exist`() {
        val response = app(Request(GET, "/users/999"))

        assertEquals(NOT_FOUND, response.status)
    }

    // --- GET /users ---

    @Test
    fun `getUsers returns all users`() {
        repo.save(Name("Alice"), Email("alice@example.com"), "password")
        repo.save(Name("Bob"), Email("bob@example.com"), "password")

        val response = app(Request(GET, "/users"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }

    @Test
    fun `getUsers returns empty list when no users exist`() {
        val response = app(Request(GET, "/users"))

        assertEquals(OK, response.status)
        assertEquals("[]", response.bodyString())
    }

    @Test
    fun `getUsers respects skip param`() {
        repo.save(Name("Alice"), Email("alice@example.com"), "password")
        repo.save(Name("Bob"), Email("bob@example.com"), "password")

        val response = app(Request(GET, "/users?skip=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `getUsers respects limit param`() {
        repo.save(Name("Alice"), Email("alice@example.com"), "password")
        repo.save(Name("Bob"), Email("bob@example.com"), "password")

        val response = app(Request(GET, "/users?limit=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `login returns 200 with id and token`() {
        repo.save(Name("Alice"), Email("alice@example.com"), "password")

        val response =
            app(
                Request(POST, "/users/login")
                    .body("""{"name":"Alice", "password":"password"}"""),
            )
        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertNotNull(json["id"])
        assertNotNull(json["token"])
    }

    @Test
    fun `login returns 400 when name is missing`() {
        repo.save(Name("Alice"), Email("alice@example.com"), "password")

        val response =
            app(
                Request(POST, "/users/login")
                    .body("""{"password":"password"}"""),
            )
        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `login returns 400 when password is missing`() {
        repo.save(Name("Alice"), Email("alice@example.com"), "password")

        val response =
            app(
                Request(POST, "/users/login")
                    .body("""{"name":"Alice"}"""),
            )
        assertEquals(BAD_REQUEST, response.status)
    }
}
