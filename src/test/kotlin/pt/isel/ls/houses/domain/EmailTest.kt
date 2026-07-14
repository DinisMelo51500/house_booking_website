package pt.isel.ls.houses.domain

import org.junit.Assert.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class EmailTest {
    @Test
    fun `creates email with valid value`() {
        val email = Email("aluno@alunos.isel.pt")
        assertEquals("aluno@alunos.isel.pt", email.value)
    }

    @Test
    fun `throws when email is blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            Email("")
        }
    }

    @Test
    fun `throws when email contains space`() {
        assertThrows(IllegalArgumentException::class.java) {
            Email("a luno@isel.pt")
        }
    }

    @Test
    fun `throws when email does not contain at`() {
        assertThrows(IllegalArgumentException::class.java) {
            Email("alunoisel.pt")
        }
    }

    @Test
    fun `throws when email does not contain dot`() {
        assertThrows(IllegalArgumentException::class.java) {
            Email("aluno@iselpt")
        }
    }
}
