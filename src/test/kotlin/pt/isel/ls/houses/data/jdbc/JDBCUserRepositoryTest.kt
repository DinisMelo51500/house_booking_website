package pt.isel.ls.houses.data.jdbc

import org.junit.Assert.assertThrows
import org.postgresql.util.PSQLException
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JDBCUserRepositoryTest {
    @Test
    fun `save creates a new user`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)
            val user = repo.save(Name("João"), Email("joao@isel.pt"), "password")

            assertTrue(user.id > 0U)
            assertEquals("João", user.name.value)
            assertEquals("joao@isel.pt", user.email.value)
        }
    }

    @Test
    fun `save throws when email already exists`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)
            repo.save(Name("João"), Email("joao@isel.pt"), "password")

            assertThrows(PSQLException::class.java) {
                repo.save(Name("Maria"), Email("joao@isel.pt"), "password")
            }
        }
    }

    @Test
    fun `findById returns user if exists`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)
            val user = repo.save(Name("João"), Email("joao@isel.pt"), "password")

            val found = repo.findById(user.id)
            assertNotNull(found)
            assertEquals(user.id, found.id)
            assertEquals(user.email, found.email)
        }
    }

    @Test
    fun `findById returns null if user does not exist`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)
            val user = repo.save(Name("Ghost"), Email("ghost@isel.pt"), "password")

            val nonExistentId = user.id + 999999U
            assertNull(repo.findById(nonExistentId))
        }
    }

    @Test
    fun `findByEmail returns user if exists`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)
            val user = repo.save(Name("João"), Email("joao@isel.pt"), "password")

            val found = repo.findByEmail("joao@isel.pt")
            assertNotNull(found)
            assertEquals(user.id, found.id)
        }
    }

    @Test
    fun `findByEmail returns null if user does not exist`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)

            val uniqueEmail = "nonexistent-${java.util.UUID.randomUUID()}@isel.pt"
            assertNull(repo.findByEmail(uniqueEmail))
        }
    }

    @Test
    fun `findAll returns all users`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)
            val before = repo.findAll().size

            val user1 = repo.save(Name("João"), Email("joao@isel.pt"), "password")
            val user2 = repo.save(Name("Maria"), Email("maria@isel.pt"), "password")

            val all = repo.findAll()
            assertEquals(before + 2, all.size)
            assertTrue(all.contains(user1))
            assertTrue(all.contains(user2))
        }
    }

    @Test
    fun `findByNameAndPassword returns user if exists`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)

            val user1 = repo.save(Name("João"), Email("joao@isel.pt"), "password")

            val found = repo.findByNameAndPassword(user1.name, "password")
            assertNotNull(found)
        }
    }

    @Test
    fun `findByNameAndPassword returns null if user does not exist`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)

            val found = repo.findByNameAndPassword(Name("name"), "password")
            assertNull(found)
        }
    }

    @Test
    fun `findByNameAndPassword returns null if name is wrong`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)

            repo.save(Name("João"), Email("joao@isel.pt"), "password")

            val found = repo.findByNameAndPassword(Name("name"), "password")
            assertNull(found)
        }
    }

    @Test
    fun `findByNameAndPassword returns null if password is wrong`() {
        withRollback { connection ->
            val repo = JDBCUserRepository(connection)

            val user1 = repo.save(Name("João"), Email("joao@isel.pt"), "password")

            val found = repo.findByNameAndPassword(user1.name, "wrong_password")
            assertNull(found)
        }
    }
}
