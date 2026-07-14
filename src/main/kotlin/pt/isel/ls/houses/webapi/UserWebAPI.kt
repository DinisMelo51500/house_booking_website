package pt.isel.ls.houses.webapi

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.Token
import pt.isel.ls.houses.services.UserService
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class UserResponse(
    val id: UInt,
    val name: String,
    val email: String,
    val password: String? = null,
)

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class IdTokenResponse(
    val id: Id,
    val token: Token,
)

class UserWebApi(
    private val userService: UserService = UserService(),
) {
    @OptIn(ExperimentalUuidApi::class)
    fun postUser(request: Request): Response =
        try {
            val body = Json.decodeFromString<Map<String, String>>(request.bodyString())
            val (id, token) =
                userService.createUser(
                    Name(body["name"] ?: error("Missing name")),
                    Email(body["email"] ?: error("Missing email")),
                    body["password"] ?: error("Missing password"),
                )
            Response(CREATED)
                .header("Content-Type", "application/json")
                .body(Json.encodeToString(IdTokenResponse(id, token)))
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }

    fun getUserById(request: Request): Response {
        val user = userService.getUserById(request.path("id")!!.toUInt())

        return if (user == null) {
            Response(NOT_FOUND).body("User not found")
        } else {
            Response(OK)
                .header("Content-Type", "application/json")
                .body(
                    Json.encodeToString(
                        UserResponse(user.id, user.name.value, user.email.value),
                    ),
                )
        }
    }

    fun getUsers(request: Request): Response {
        val skip = request.query("skip")?.toInt() ?: 0
        val limit = request.query("limit")?.toInt() ?: Int.MAX_VALUE
        val users =
            userService
                .getAllUsers(skip, limit)
                .map { UserResponse(it.id, it.name.value, it.email.value) }

        return Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(users))
    }

    @OptIn(ExperimentalUuidApi::class)
    fun login(request: Request): Response =
        try {
            val body = Json.decodeFromString<Map<String, String>>(request.bodyString())
            val (id, token) =
                userService.getUserByNameAndPassword(
                    Name(body["name"] ?: error("Missing name")),
                    body["password"] ?: error("Missing password"),
                )

            Response(OK)
                .header("Content-Type", "application/json")
                .body(Json.encodeToString(IdTokenResponse(id, token)))
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }

    @OptIn(ExperimentalUuidApi::class)
    val routes =
        routes(
            "/users" bind POST to { req: Request ->
                postUser(req)
            },
            "/users/login" bind POST to { req: Request ->
                login(req)
            },
            "/users/{id}" bind GET to { req: Request ->
                getUserById(req)
            },
            "/users" bind GET to { req: Request ->
                getUsers(req)
            },
        )
}
