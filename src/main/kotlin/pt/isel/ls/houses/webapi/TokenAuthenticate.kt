package pt.isel.ls.houses.webapi

import org.http4k.core.Request
import pt.isel.ls.houses.domain.User
import pt.isel.ls.houses.services.UserService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun authenticate(
    request: Request,
    userService: UserService,
): User? {
    val authHeader = request.header("Authorization") ?: return null
    if (!authHeader.startsWith("Bearer ")) return null
    val tokenString = authHeader.removePrefix("Bearer ").trim()
    val token =
        try {
            Uuid.parse(tokenString)
        } catch (e: IllegalArgumentException) {
            return null
        }
    return try {
        userService.getUserByToken(token)
    } catch (e: IllegalArgumentException) {
        null
    }
}
