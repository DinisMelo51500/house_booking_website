package pt.isel.ls.houses.data.jdbc

import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JDBCHouseRepositoryTest {
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

    private fun setup(connection: Connection): Pair<Pair<Id, Id>, Pair<Id, Id>> {
        val userRepo = JDBCUserRepository(connection)
        val locationRepo = JDBCLocationRepository(connection)

        val owner1 = userRepo.save(Name("Alice"), Email("alice@isel.pt"), "password")
        val owner2 = userRepo.save(Name("Bob"), Email("bob@isel.pt"), "password")
        val location1 = locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        val location2 = locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, location1.id)

        return Pair(Pair(owner1.id, owner2.id), Pair(location1.id, location2.id))
    }

    @Test
    fun `save creates a new house`() {
        withRollback { connection ->
            val (owners, locations) = setup(connection)
            val (ownerId1, _) = owners
            val (locationId1, _) = locations

            val repo = JDBCHouseRepository(connection)
            val house = repo.save(title1, locationId1, area1, price1, description1, ownerId1)

            assertTrue(house.id > 0U)
            assertEquals(title1, house.title)
            assertEquals(locationId1, house.location)
            assertEquals(area1, house.areaSqMt)
            assertEquals(price1, house.pricePerNight)
            assertEquals(description1, house.description)
            assertEquals(ownerId1, house.ownerId)
        }
    }

    @Test
    fun `findById returns house if exists`() {
        withRollback { connection ->
            val (owners, locations) = setup(connection)
            val (ownerId1, _) = owners
            val (locationId1, _) = locations

            val repo = JDBCHouseRepository(connection)
            val house = repo.save(title1, locationId1, area1, price1, description1, ownerId1)

            val found = repo.findById(house.id)
            assertNotNull(found)
            assertEquals(house.id, found.id)
            assertEquals(house.title, found.title)
        }
    }

    @Test
    fun `findById returns null if house does not exist`() {
        withRollback { connection ->
            val (owners, locations) = setup(connection)
            val (ownerId1, _) = owners
            val (locationId1, _) = locations

            val repo = JDBCHouseRepository(connection)
            val house = repo.save(title1, locationId1, area1, price1, description1, ownerId1)
            val nonExistentId = house.id + 999999U

            assertNull(repo.findById(nonExistentId))
        }
    }

    @Test
    fun `findAll returns all houses`() {
        withRollback { connection ->
            val (owners, locations) = setup(connection)
            val (ownerId1, ownerId2) = owners
            val (locationId1, locationId2) = locations

            val repo = JDBCHouseRepository(connection)
            val before = repo.findAll(minArea = minArea, maxPrice = maxPrice).size

            val house1 = repo.save(title1, locationId1, area1, price1, description1, ownerId1)
            val house2 = repo.save(title2, locationId2, area2, price2, description2, ownerId2)

            val allHouses = repo.findAll(minArea = minArea, maxPrice = maxPrice)
            assertEquals(before + 2, allHouses.size)
            assertTrue(allHouses.contains(house1))
            assertTrue(allHouses.contains(house2))
        }
    }

    @Test
    fun `findAll returns all houses with at least minimum area`() {
        withRollback { connection ->
            val (owners, locations) = setup(connection)
            val (ownerId1, ownerId2) = owners
            val (locationId1, locationId2) = locations

            val repo = JDBCHouseRepository(connection)
            val before = repo.findAll(minArea = 100u, maxPrice = maxPrice).size

            val house1 = repo.save(title1, locationId1, area1, price1, description1, ownerId1)
            val house2 = repo.save(title2, locationId2, area1, price2, description2, ownerId2)
            repo.save(title2, locationId2, area2, price2, description2, ownerId2)

            val allHouses = repo.findAll(minArea = 100u, maxPrice = maxPrice)
            assertEquals(before + 2, allHouses.size)
            assertTrue(allHouses.contains(house1))
            assertTrue(allHouses.contains(house2))
        }
    }

    @Test
    fun `findAll returns all houses under maximum price`() {
        withRollback { connection ->
            val (owners, locations) = setup(connection)
            val (ownerId1, ownerId2) = owners
            val (locationId1, locationId2) = locations

            val repo = JDBCHouseRepository(connection)
            val before = repo.findAll(minArea = minArea, maxPrice = PricePerNight(100.0)).size

            val house1 = repo.save(title1, locationId1, area1, price2, description1, ownerId1)
            val house2 = repo.save(title2, locationId2, area1, price2, description2, ownerId2)
            repo.save(title2, locationId2, area2, price1, description2, ownerId2)

            val allHouses = repo.findAll(minArea = minArea, maxPrice = PricePerNight(100.0))
            assertEquals(before + 2, allHouses.size)
            assertTrue(allHouses.contains(house1))
            assertTrue(allHouses.contains(house2))
        }
    }

    @Test
    fun `findAll returns all houses with correct location`() {
        withRollback { connection ->
            val (owners, locations) = setup(connection)
            val (ownerId1, ownerId2) = owners
            val (locationId1, locationId2) = locations

            val repo = JDBCHouseRepository(connection)
            val before = repo.findAll(minArea = minArea, maxPrice = maxPrice, location = locationId1).size

            val house1 = repo.save(title1, locationId1, area1, price2, description1, ownerId1)
            val house2 = repo.save(title2, locationId1, area1, price2, description2, ownerId2)
            repo.save(title2, locationId2, area2, price1, description2, ownerId2)

            val allHouses = repo.findAll(minArea = minArea, maxPrice = maxPrice, location = locationId1)
            assertEquals(before + 2, allHouses.size)
            assertTrue(allHouses.contains(house1))
            assertTrue(allHouses.contains(house2))
        }
    }
}
