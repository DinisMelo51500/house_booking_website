package pt.isel.ls.houses.data.mem

import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemLocationRepositoryTest {
    private fun createRepo() = MemLocationRepository()

    @Test
    fun `save creates a new location`() {
        val repo = createRepo()
        val country = repo.save(Name("Portugal"), LocationType.COUNTRY)

        assertEquals(1U, country.id)
        assertEquals("Portugal", country.name.value)
        assertEquals(LocationType.COUNTRY, country.type)
        assertEquals(null, country.parentId)
    }

    @Test
    fun `findById returns location if exists`() {
        val repo = createRepo()
        val country = repo.save(Name("Portugal"), LocationType.COUNTRY)

        val found = repo.findById(country.id)
        assertNotNull(found)
        assertEquals(country.id, found.id)
    }

    @Test
    fun `findById returns null if location does not exist`() {
        val repo = createRepo()
        val found = repo.findById(999U)
        assertNull(found)
    }

    @Test
    fun `findChildren returns correct children`() {
        val repo = createRepo()
        val country = repo.save(Name("Portugal"), LocationType.COUNTRY)
        val region1 = repo.save(Name("Lisbon Region"), LocationType.REGION, parentId = country.id)
        val region2 = repo.save(Name("Porto Region"), LocationType.REGION, parentId = country.id)
        val district = repo.save(Name("Lisbon District"), LocationType.DISTRICT, parentId = region1.id)

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

    @Test
    fun `getFullPath returns correct path`() {
        val repo = createRepo()
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

    @Test
    fun `getFullPath returns single element for root`() {
        val repo = createRepo()
        val country = repo.save(Name("Portugal"), LocationType.COUNTRY)

        val path = repo.getFullPath(country.id)
        assertEquals(1, path.size)
        assertEquals(country, path[0])
    }

    @Test
    fun `getFullPath returns empty for non-existing location`() {
        val repo = createRepo()
        val path = repo.getFullPath(999U)
        assertTrue(path.isEmpty())
    }
}
