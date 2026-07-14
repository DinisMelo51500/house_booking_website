package pt.isel.ls.houses.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserTest {
    private val testId = Id(1)
    private val testName = Name("João")
    private val testEmail = Email("aluno@alunos.isel.pt")

    @Test
    fun `creates user with valid data`() {
        val user =
            User(
                id = testId,
                name = testName,
                email = testEmail,
                password = "password",
            )

        assertEquals(testId, user.id)
        assertEquals(testName, user.name)
        assertEquals(testEmail, user.email)
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    @Test
    fun `token is generated automatically`() {
        val user =
            User(
                id = testId,
                name = testName,
                email = testEmail,
                password = "password",
            )

        assertNotNull(user.token)
    }
}
