package pt.isel.ls.houses.data.jdbc

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertThrows
import org.postgresql.util.PSQLException
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.Email
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JDBCBookingRepositoryTest {
    private val startDate1 = LocalDate(2025, 6, 10)
    private val endDate1 = LocalDate(2025, 6, 15)
    private val startDate2 = LocalDate(2025, 6, 16)
    private val endDate2 = LocalDate(2025, 6, 18)

    private fun setup(connection: Connection): Pair<Pair<Id, Id>, Pair<Id, Id>> {
        val userRepo = JDBCUserRepository(connection)
        val locationRepo = JDBCLocationRepository(connection)
        val houseRepo = JDBCHouseRepository(connection)

        val user1 = userRepo.save(Name("Test User 1"), Email("testuser1@example.com"), "password")
        val user2 = userRepo.save(Name("Test User 2"), Email("testuser2@example.com"), "password")

        val country = locationRepo.save(Name("Test Country"), LocationType.COUNTRY)
        val district = locationRepo.save(Name("Test District"), LocationType.DISTRICT, parentId = country.id)

        val house1 =
            houseRepo.save(
                Name("House One"),
                country.id,
                50U,
                PricePerNight(100.0),
                Description("House One Description"),
                user1.id,
            )
        val house2 =
            houseRepo.save(
                Name("House Two"),
                district.id,
                1000U,
                PricePerNight(250.0),
                Description("House Two Description"),
                user1.id,
            )

        return Pair(Pair(user1.id, user2.id), Pair(house1.id, house2.id))
    }

    @Test
    fun `save creates a new booking`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val booking = repo.save(userId1, houseId1, startDate1, endDate1)

            assertTrue(booking.id > 0U)
            assertEquals(userId1, booking.userId)
            assertEquals(houseId1, booking.houseId)
            assertEquals(startDate1, booking.startDate)
            assertEquals(endDate1, booking.endDate)
        }
    }

    @Test
    fun `save throws when end date is before start date`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            assertThrows(PSQLException::class.java) {
                repo.save(userId1, houseId1, LocalDate(2025, 6, 15), LocalDate(2025, 6, 10))
            }
        }
    }

    @Test
    fun `findById returns booking if exists`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val booking = repo.save(userId1, houseId1, startDate1, endDate1)

            val found = repo.findById(booking.id)
            assertNotNull(found)
            assertEquals(booking.id, found.id)
        }
    }

    @Test
    fun `findById returns null if booking does not exist`() {
        withRollback { connection ->
            val repo = JDBCBookingRepository(connection)

            // Delete a booking we just created and use its ID — guaranteed not to exist after deletion
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val booking = repo.save(userId1, houseId1, startDate1, endDate1)
            repo.deleteById(booking.id)

            val found = repo.findById(booking.id)
            assertNull(found)
        }
    }

    @Test
    fun `findByHouse returns bookings for given house`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, userId2) = users
            val (houseId1, houseId2) = houses

            val repo = JDBCBookingRepository(connection)
            val b1 = repo.save(userId1, houseId1, startDate1, endDate1)
            val b2 = repo.save(userId2, houseId1, startDate2, endDate2)
            val b3 = repo.save(userId1, houseId2, startDate1, endDate1)

            // Use containsAll + size based on what WE inserted, not total DB state
            val house1Bookings = repo.findByHouse(houseId1)
            assertTrue(house1Bookings.containsAll(listOf(b1, b2)))
            assertFalse(house1Bookings.contains(b3))

            val house2Bookings = repo.findByHouse(houseId2)
            assertTrue(house2Bookings.contains(b3))
            assertFalse(house2Bookings.contains(b1))
        }
    }

    @Test
    fun `findByHouseAndDate returns bookings including date`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, userId2) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val b1 = repo.save(userId1, houseId1, startDate1, endDate1)
            val b2 = repo.save(userId2, houseId1, startDate2, endDate2)

            val bookingsOnJune14To16 = repo.findByHouseAndDate(houseId1, LocalDate(2025, 6, 14), LocalDate(2025, 6, 16))
            assertTrue(bookingsOnJune14To16.containsAll(listOf(b1, b2)))

            val bookingsOnJune10 = repo.findByHouseAndDate(houseId1, LocalDate(2025, 6, 10), LocalDate(2025, 6, 10))
            assertTrue(bookingsOnJune10.contains(b1))
            assertFalse(bookingsOnJune10.contains(b2))
        }
    }

    @Test
    fun `findByUser returns all bookings for user`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val b1 = repo.save(userId1, houseId1, startDate1, endDate1)
            val b2 = repo.save(userId1, houseId1, startDate2, endDate2)

            val bookings = repo.findByUser(userId1)
            // Assert our bookings are present, regardless of any pre-existing data
            assertTrue(bookings.containsAll(listOf(b1, b2)))
        }
    }

    @Test
    fun `findByUser returns empty list if no bookings exist`() {
        withRollback { connection ->
            val (users, _) = setup(connection)
            val (userId1, _) = users

            // Fresh user from setupFixtures has no bookings yet
            val repo = JDBCBookingRepository(connection)
            val bookings = repo.findByUser(userId1)
            assertTrue(bookings.isEmpty())
        }
    }

    @Test
    fun `findByUser respects skip param`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            repo.save(userId1, houseId1, startDate1, endDate1)
            repo.save(userId1, houseId1, startDate2, endDate2)

            val withoutSkip = repo.findByUser(userId1)
            val withSkip = repo.findByUser(userId1, skip = 1)
            assertEquals(withoutSkip.size - 1, withSkip.size)
        }
    }

    @Test
    fun `findByUser respects limit param`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            repo.save(userId1, houseId1, startDate1, endDate1)
            repo.save(userId1, houseId1, startDate2, endDate2)

            val limited = repo.findByUser(userId1, limit = 1)
            assertEquals(1, limited.size)
        }
    }

    @Test
    fun `deleteById removes booking if exists`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val booking = repo.save(userId1, houseId1, startDate1, endDate1)

            assertTrue(repo.deleteById(booking.id))
            assertNull(repo.findById(booking.id))
        }
    }

    @Test
    fun `deleteById returns false if booking does not exist`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val booking = repo.save(userId1, houseId1, startDate1, endDate1)
            repo.deleteById(booking.id)

            // Deleting again returns false
            assertFalse(repo.deleteById(booking.id))
        }
    }

    @Test
    fun `updateBooking updates booking if exists`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val booking = repo.save(userId1, houseId1, startDate1, endDate1)

            val updated = repo.updateBooking(booking.id, startDate2, endDate2)
            assertNotNull(updated)
            assertEquals(startDate2, updated.startDate)
            assertEquals(endDate2, updated.endDate)
        }
    }

    @Test
    fun `updateBooking returns null if booking does not exist`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val booking = repo.save(userId1, houseId1, startDate1, endDate1)
            repo.deleteById(booking.id)

            assertNull(repo.updateBooking(booking.id, startDate2, endDate2))
        }
    }

    @Test
    fun `updateBooking throws exception if date interval is invalid`() {
        withRollback { connection ->
            val (users, houses) = setup(connection)
            val (userId1, _) = users
            val (houseId1, _) = houses

            val repo = JDBCBookingRepository(connection)
            val booking = repo.save(userId1, houseId1, startDate1, endDate1)

            assertThrows(PSQLException::class.java) {
                repo.updateBooking(booking.id, endDate2, startDate2)
            }
        }
    }
}
