package pt.isel.ls.houses.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class IdTest {
    @Test
    fun `creates id with valid value`() {
        val id = Id(10)
        assertEquals(10u, id)
    }
}
