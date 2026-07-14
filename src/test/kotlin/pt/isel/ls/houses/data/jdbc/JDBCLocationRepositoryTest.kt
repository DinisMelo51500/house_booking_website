package pt.isel.ls.houses.data.jdbc

import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JDBCLocationRepositoryTest {
    @Test
    fun `save creates a new location`() {
        withRollback { connection ->
            val repo = JDBCLocationRepository(connection)
            val country = repo.save(Name("Portugal"), LocationType.COUNTRY)

            assertNotNull(country.id)
            assertEquals("Portugal", country.name.value)
            assertEquals(LocationType.COUNTRY, country.type)
            assertNull(country.parentId)
        }
    }

    @Test
    fun `findById returns location if exists`() {
        withRollback { connection ->
            val repo = JDBCLocationRepository(connection)
            val country = repo.save(Name("Portugal"), LocationType.COUNTRY)

            val found = repo.findById(country.id)
            assertNotNull(found)
            assertEquals(country.id, found.id)
            assertEquals(country.name, found.name)
        }
    }

    @Test
    fun `findById returns null if location does not exist`() {
        withRollback { connection ->
            val repo = JDBCLocationRepository(connection)
            val country = repo.save(Name("Portugal"), LocationType.COUNTRY)
            val nonExistentId = country.id + 999999U

            assertNull(repo.findById(nonExistentId))
        }
    }

    @Test
    fun `findChildren returns correct children`() {
        withRollback { connection ->
            val repo = JDBCLocationRepository(connection)
            val country = repo.save(Name("Portugal"), LocationType.COUNTRY)
            val region1 = repo.save(Name("Lisbon Region"), LocationType.REGION, parentId = country.id)
            val region2 = repo.save(Name("Porto Region"), LocationType.REGION, parentId = country.id)
            val district = repo.save(Name("Lisbon District"), LocationType.DISTRICT, parentId = region1.id)

            // Safe to use exact counts here — fresh parent IDs mean no pre-existing children
            val childrenOfCountry = repo.findChildren(country.id)
            assertEquals(2, childrenOfCountry.size)
            assertTrue(childrenOfCountry.contains(region1))
            assertTrue(childrenOfCountry.contains(region2))

            val childrenOfRegion1 = repo.findChildren(region1.id)
            assertEquals(1, childrenOfRegion1.size)
            assertEquals(district, childrenOfRegion1.first())

            val childrenOfRegion2 = repo.findChildren(region2.id)
            assertTrue(childrenOfRegion2.isEmpty())
        }
    }

    @Test
    fun `getFullPath returns correct path`() {
        withRollback { connection ->
            val repo = JDBCLocationRepository(connection)
            val country = repo.save(Name("Portugal"), LocationType.COUNTRY)
            val region = repo.save(Name("Lisbon Region"), LocationType.REGION, parentId = country.id)
            val district = repo.save(Name("Lisbon District"), LocationType.DISTRICT, parentId = region.id)
            val locality = repo.save(Name("Some Locality"), LocationType.LOCALITY, parentId = district.id)

            val path = repo.getFullPath(locality.id)
            assertEquals(4, path.size)
            assertEquals(country, path[0])
            assertEquals(region, path[1])
            assertEquals(district, path[2])
            assertEquals(locality, path[3])
        }
    }

    @Test
    fun `getFullPath returns single element for root`() {
        withRollback { connection ->
            val repo = JDBCLocationRepository(connection)
            val country = repo.save(Name("Portugal"), LocationType.COUNTRY)

            val path = repo.getFullPath(country.id)
            assertEquals(1, path.size)
            assertEquals(country, path[0])
        }
    }

    @Test
    fun `getFullPath returns empty for non-existing location`() {
        withRollback { connection ->
            val repo = JDBCLocationRepository(connection)
            val country = repo.save(Name("Portugal"), LocationType.COUNTRY)
            val nonExistentId = country.id + 999999U

            assertTrue(repo.getFullPath(nonExistentId).isEmpty())
        }
    }
}
