package pt.isel.ls.houses.data.mem

import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemHouseRepositoryTest {
    private fun createRepo() = MemHouseRepository()

    private val ownerId1 = Id(1)
    private val ownerId2 = Id(2)

    private val location1 = Id(10)
    private val location2 = Id(20)

    private val title1 = Name("Beach House")
    private val title2 = Name("Mountain Cabin")

    private val area1 = AreaSqMt(150)
    private val area2 = AreaSqMt(90)

    private val price1 = PricePerNight(120.0)
    private val price2 = PricePerNight(80.0)

    private val description1 = Description("Nice house with sea view")
    private val description2 = Description("Cozy cabin in the mountains")

    private val minArea = 0u
    private val maxPrice = PricePerNight(Double.MAX_VALUE)

    @Test
    fun `save creates a new house`() {
        val repo = createRepo()
        val house = repo.save(title1, location1, area1, price1, description1, ownerId1)

        assertEquals(1U, house.id)
        assertEquals(title1, house.title)
        assertEquals(location1, house.location)
        assertEquals(area1, house.areaSqMt)
        assertEquals(price1, house.pricePerNight)
        assertEquals(description1, house.description)
        assertEquals(ownerId1, house.ownerId)
    }

    @Test
    fun `findById returns house if exists`() {
        val repo = createRepo()
        val house = repo.save(title1, location1, area1, price1, description1, ownerId1)

        val found = repo.findById(house.id)
        assertNotNull(found)
        assertEquals(house.id, found.id)
        assertEquals(house.title, found.title)
    }

    @Test
    fun `findById returns null if house does not exist`() {
        val repo = createRepo()
        val found = repo.findById(Id(999))
        assertNull(found)
    }

    @Test
    fun `findAll returns all houses`() {
        val repo = createRepo()
        val house1 = repo.save(title1, location1, area1, price1, description1, ownerId1)
        val house2 = repo.save(title2, location2, area2, price2, description2, ownerId2)

        val allHouses = repo.findAll(minArea = minArea, maxPrice = maxPrice)
        assertEquals(2, allHouses.size)
        assertTrue(allHouses.contains(house1))
        assertTrue(allHouses.contains(house2))
    }

    @Test
    fun `findAll returns all houses with at least minimum area`() {
        val repo = createRepo()
        val house1 = repo.save(title1, location1, area1, price1, description1, ownerId1)
        val house2 = repo.save(title2, location2, area1, price2, description2, ownerId2)
        repo.save(title2, location2, area2, price2, description2, ownerId2)

        val allHouses = repo.findAll(minArea = 100u, maxPrice = maxPrice)
        assertEquals(2, allHouses.size)
        assertTrue(allHouses.contains(house1))
        assertTrue(allHouses.contains(house2))
    }

    @Test
    fun `findAll returns all houses under maximum price`() {
        val repo = createRepo()
        val house1 = repo.save(title1, location1, area1, price2, description1, ownerId1)
        val house2 = repo.save(title2, location2, area1, price2, description2, ownerId2)
        repo.save(title2, location2, area2, price1, description2, ownerId2)

        val allHouses = repo.findAll(minArea = minArea, maxPrice = PricePerNight(100.0))
        assertEquals(2, allHouses.size)
        assertTrue(allHouses.contains(house1))
        assertTrue(allHouses.contains(house2))
    }

    @Test
    fun `findAll returns all houses with correct location`() {
        val repo = createRepo()
        val house1 = repo.save(title1, location1, area1, price2, description1, ownerId1)
        val house2 = repo.save(title2, location1, area1, price2, description2, ownerId2)
        repo.save(title2, location2, area2, price1, description2, ownerId2)

        val allHouses = repo.findAll(minArea = minArea, maxPrice = maxPrice, location = location1)
        assertEquals(2, allHouses.size)
        assertTrue(allHouses.contains(house1))
        assertTrue(allHouses.contains(house2))
    }
}
