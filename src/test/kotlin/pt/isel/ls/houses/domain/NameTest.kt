package pt.isel.ls.houses.domain

import org.junit.Assert.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class NameTest {
    @Test
    fun `creates name with valid value`() {
        val name = Name("João")
        assertEquals("João", name.value)
    }

    @Test
    fun `throws when name is blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            Name(" ")
        }
    }

    @Test
    fun `throws when name is empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            Name("")
        }
    }

    @Test
    fun `throws when name is too long`() {
        val longName = "a".repeat(101)

        assertThrows(IllegalArgumentException::class.java) {
            Name(longName)
        }
    }
}
