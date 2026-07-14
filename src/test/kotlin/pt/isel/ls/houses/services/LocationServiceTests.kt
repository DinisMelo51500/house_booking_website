package pt.isel.ls.houses.services

import pt.isel.ls.houses.data.mem.MemLocationRepository
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class LocationServiceTests {
    private lateinit var locationService: LocationService

    @BeforeTest
    fun setup() {
        locationService = LocationService(MemLocationRepository())
    }

    @Test
    fun `createLocation returns id`() {
        val id = locationService.createLocation(Name("Portugal"), LocationType.COUNTRY)
        assertNotNull(id)
    }

    @Test
    fun `createLocation generates unique ids`() {
        val id1 = locationService.createLocation(Name("Country1"), LocationType.COUNTRY)
        val id2 = locationService.createLocation(Name("Country2"), LocationType.COUNTRY)
        assertNotEquals(id1, id2)
    }

    @Test
    fun `createLocation with parent`() {
        val countryId = locationService.createLocation(Name("Portugal"), LocationType.COUNTRY)
        val districtId = locationService.createLocation(Name("Lisboa"), LocationType.DISTRICT, countryId)
        val location = locationService.getLocationInfo(districtId)

        assertNotNull(location)
        assertEquals(countryId, location.parentId)
    }

    @Test
    fun `getChildrenLocations returns children with paging`() {
        val countryId = locationService.createLocation(Name("France"), LocationType.COUNTRY)
        locationService.createLocation(Name("Paris"), LocationType.REGION, countryId)
        locationService.createLocation(Name("Lyon"), LocationType.REGION, countryId)

        val allChildren = locationService.getChildrenLocations(countryId)
        val pagedChildren = locationService.getChildrenLocations(countryId, skip = 1, limit = 1)

        assertEquals(2, allChildren.size)
        assertEquals(1, pagedChildren.size)
    }

    @Test
    fun `getFullLocationPath returns correct hierarchy`() {
        val countryId = locationService.createLocation(Name("Portugal"), LocationType.COUNTRY)
        val districtId = locationService.createLocation(Name("Lisboa"), LocationType.DISTRICT, countryId)
        val municipalityId = locationService.createLocation(Name("Oeiras"), LocationType.MUNICIPALITY, districtId)

        val path = locationService.getFullLocationPath(municipalityId)

        assertEquals(3, path.size)
        assertEquals("Portugal", path[0].name.value)
        assertEquals("Lisboa", path[1].name.value)
        assertEquals("Oeiras", path[2].name.value)
    }
}
