package pt.isel.ls.houses.data.mem

import org.junit.Assert.assertThrows
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MemUserRepositoryTest {
    private fun createRepository() = MemUserRepository()

    @Test
    fun `save creates a new user`() {
        val repo = createRepository()
        val user = repo.save(Name("João"), Email("joao@isel.pt"), "password")

        assertEquals(1U, user.id)
        assertEquals("João", user.name.value)
        assertEquals("joao@isel.pt", user.email.value)
    }

    @Test
    fun `save throws when email already exists`() {
        val repo = createRepository()
        repo.save(Name("João"), Email("joao@isel.pt"), "password")

        assertThrows(IllegalArgumentException::class.java) {
            repo.save(Name("Maria"), Email("joao@isel.pt"), "password")
        }
    }

    @Test
    fun `findById returns user if exists`() {
        val repo = createRepository()
        val user = repo.save(Name("João"), Email("joao@isel.pt"), "password")

        val found = repo.findById(user.id)
        assertNotNull(found)
        assertEquals(user.id, found.id)
        assertEquals(user.email, found.email)
    }

    @Test
    fun `findById returns null if user does not exist`() {
        val repo = createRepository()
        val found = repo.findById(Id(999))
        assertNull(found)
    }

    @Test
    fun `findByEmail returns user if exists`() {
        val repo = createRepository()
        val user = repo.save(Name("João"), Email("joao@isel.pt"), "password")

        val found = repo.findByEmail("joao@isel.pt")
        assertNotNull(found)
        assertEquals(user.id, found.id)
    }

    @Test
    fun `findByEmail returns null if user does not exist`() {
        val repo = createRepository()
        val found = repo.findByEmail("missing@isel.pt")
        assertNull(found)
    }

    @Test
    fun `findAll returns all users`() {
        val repo = createRepository()
        val user1 = repo.save(Name("João"), Email("joao@isel.pt"), "password")
        val user2 = repo.save(Name("Maria"), Email("maria@isel.pt"), "password")

        val all = repo.findAll()
        assertEquals(2, all.size)
        assert(all.contains(user1))
        assert(all.contains(user2))
    }

    @Test
    fun `findByNameAndPassword returns user if exists`() {
        val repo = createRepository()
        val user1 = repo.save(Name("João"), Email("joao@isel.pt"), "password")

        val found = repo.findByNameAndPassword(user1.name, "password")
        assertNotNull(found)
    }

    @Test
    fun `findByNameAndPassword returns null if user does not exist`() {
        val repo = createRepository()

        val found = repo.findByNameAndPassword(Name("name"), "password")
        assertNull(found)
    }

    @Test
    fun `findByNameAndPassword returns null if name is wrong`() {
        val repo = createRepository()
        repo.save(Name("João"), Email("joao@isel.pt"), "password")

        val found = repo.findByNameAndPassword(Name("name"), "password")
        assertNull(found)
    }

    @Test
    fun `findByNameAndPassword returns null if password is wrong`() {
        val repo = createRepository()
        val user1 = repo.save(Name("João"), Email("joao@isel.pt"), "password")

        val found = repo.findByNameAndPassword(user1.name, "wrong_password")
        assertNull(found)
    }
}
