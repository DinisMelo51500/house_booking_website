package pt.isel.ls.houses.domain
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class User(
    val id: Id,
    val name: Name,
    val email: Email,
    val token: Token = Token.random(),
    val password: String,
)

@JvmInline
value class Email(
    val value: String,
) {
    init {
        require(isValidEmail(value))
    }
}

fun isValidEmail(email: String): Boolean {
    val emailRegex =
        Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        )
    return email.matches(emailRegex)
}

@OptIn(ExperimentalUuidApi::class)
typealias Token = Uuid
