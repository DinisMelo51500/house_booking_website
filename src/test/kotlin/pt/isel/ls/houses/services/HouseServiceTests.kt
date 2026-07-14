package pt.isel.ls.houses.services

import pt.isel.ls.houses.data.mem.MemHouseRepository
import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HouseServiceTests {
    private lateinit var houseService: HouseService

    private val minArea = 0u
    private val maxPrice = PricePerNight(Double.MAX_VALUE)

    @BeforeTest
    fun setup() {
        houseService = HouseService(MemHouseRepository())
    }

    @Test
    fun `createHouse returns id`() {
        val id =
            houseService.createHouse(
                Name("Nice House"),
                Id(1),
                AreaSqMt(100),
                PricePerNight(80.0),
                Description("Good location"),
                Id(1),
            )
        assertNotNull(id)
    }

    @Test
    fun `createHouse generates unique ids`() {
        val id1 =
            houseService.createHouse(
                Name("House1"),
                Id(1),
                AreaSqMt(100),
                PricePerNight(80.0),
                Description("Desc1"),
                Id(1),
            )
        val id2 =
            houseService.createHouse(
                Name("House2"),
                Id(2),
                AreaSqMt(120),
                PricePerNight(90.0),
                Description("Desc2"),
                Id(1),
            )
        assertNotEquals(id1, id2)
    }

    @Test
    fun `getHouseInfo returns house`() {
        val id =
            houseService.createHouse(
                Name("City House"),
                Id(1),
                AreaSqMt(80),
                PricePerNight(70.0),
                Description("Center"),
                Id(1),
            )
        val house = houseService.getHouseInfo(id)
        assertNotNull(house)
    }

    @Test
    fun `getHouseInfo returns null when not found`() {
        val house = houseService.getHouseInfo(9999U)
        assertNull(house)
    }

    @Test
    fun `getAllHouses returns houses with paging`() {
        houseService.createHouse(Name("H1"), Id(1), AreaSqMt(50), PricePerNight(40.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H2"), Id(1), AreaSqMt(60), PricePerNight(50.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H3"), Id(1), AreaSqMt(70), PricePerNight(60.0), Description("Desc"), Id(1))

        val allHouses = houseService.getAllHouses(minArea = minArea, maxPrice = maxPrice)
        val pagedHouses = houseService.getAllHouses(skip = 1, limit = 1, minArea = minArea, maxPrice = maxPrice)

        assertEquals(3, allHouses.size)
        assertEquals(1, pagedHouses.size)
    }

    @Test
    fun `getAllHouses returns houses with at least minimum area`() {
        houseService.createHouse(Name("H1"), Id(1), AreaSqMt(50), PricePerNight(40.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H2"), Id(1), AreaSqMt(60), PricePerNight(50.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H3"), Id(1), AreaSqMt(70), PricePerNight(60.0), Description("Desc"), Id(1))

        val allHouses = houseService.getAllHouses(minArea = minArea, maxPrice = maxPrice)
        val correctHouses = houseService.getAllHouses(minArea = 60u, maxPrice = maxPrice)

        assertEquals(3, allHouses.size)
        assertEquals(2, correctHouses.size)
    }

    @Test
    fun `getAllHouses returns houses under maximum price`() {
        houseService.createHouse(Name("H1"), Id(1), AreaSqMt(50), PricePerNight(40.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H2"), Id(1), AreaSqMt(60), PricePerNight(50.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H3"), Id(1), AreaSqMt(70), PricePerNight(60.0), Description("Desc"), Id(1))

        val allHouses = houseService.getAllHouses(minArea = minArea, maxPrice = maxPrice)
        val correctHouses = houseService.getAllHouses(minArea = minArea, maxPrice = PricePerNight(50.0))

        assertEquals(3, allHouses.size)
        assertEquals(2, correctHouses.size)
    }

    @Test
    fun `getAllHouses returns houses with correct location`() {
        houseService.createHouse(Name("H1"), Id(1), AreaSqMt(50), PricePerNight(40.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H2"), Id(1), AreaSqMt(60), PricePerNight(50.0), Description("Desc"), Id(1))
        houseService.createHouse(Name("H3"), Id(2), AreaSqMt(70), PricePerNight(60.0), Description("Desc"), Id(1))

        val allHouses = houseService.getAllHouses(minArea = minArea, maxPrice = maxPrice)
        val correctHouses = houseService.getAllHouses(minArea = minArea, maxPrice = maxPrice, location = Id(1))

        assertEquals(3, allHouses.size)
        assertEquals(2, correctHouses.size)
    }
}
