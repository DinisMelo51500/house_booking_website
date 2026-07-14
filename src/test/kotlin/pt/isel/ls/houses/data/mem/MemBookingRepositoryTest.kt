package pt.isel.ls.houses.data.mem

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertThrows
import pt.isel.ls.houses.domain.Id
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemBookingRepositoryTest {
    private fun createRepo() = MemBookingRepository()

    private val userId1 = Id(1)
    private val userId2 = Id(2)
    private val houseId1 = Id(10)
    private val houseId2 = Id(20)

    private val startDate1 = LocalDate(2025, 6, 10)
    private val endDate1 = LocalDate(2025, 6, 15)

    private val startDate2 = LocalDate(2025, 6, 14)
    private val endDate2 = LocalDate(2025, 6, 18)

    private val startDate3 = LocalDate(2025, 6, 20)
    private val endDate3 = LocalDate(2025, 6, 25)

    @Test
    fun `save creates a new booking`() {
        val repo = createRepo()
        val booking = repo.save(userId1, houseId1, startDate1, endDate1)

        assertEquals(1U, booking.id)
        assertEquals(userId1, booking.userId)
        assertEquals(houseId1, booking.houseId)
        assertEquals(startDate1, booking.startDate)
        assertEquals(endDate1, booking.endDate)
    }

    @Test
    fun `save throws when end date is before start date`() {
        val repo = createRepo()
        assertThrows(IllegalArgumentException::class.java) {
            repo.save(userId1, houseId1, LocalDate(2025, 6, 15), LocalDate(2025, 6, 10))
        }
    }

    @Test
    fun `findById returns booking if exists`() {
        val repo = createRepo()
        val booking = repo.save(userId1, houseId1, startDate1, endDate1)

        val found = repo.findById(booking.id)
        assertNotNull(found)
        assertEquals(booking.id, found.id)
    }

    @Test
    fun `findById returns null if booking does not exist`() {
        val repo = createRepo()
        val found = repo.findById(Id(999))
        assertNull(found)
    }

    @Test
    fun `findByHouse returns bookings for given house`() {
        val repo = createRepo()
        val b1 = repo.save(userId1, houseId1, startDate1, endDate1)
        val b2 = repo.save(userId2, houseId1, startDate2, endDate2)
        val b3 = repo.save(userId1, houseId2, startDate1, endDate1)

        val house1Bookings = repo.findByHouse(houseId1)
        assertEquals(2, house1Bookings.size)
        assertTrue(house1Bookings.contains(b1))
        assertTrue(house1Bookings.contains(b2))

        val house2Bookings = repo.findByHouse(houseId2)
        assertEquals(1, house2Bookings.size)
        assertTrue(house2Bookings.contains(b3))
    }

    @Test
    fun `findByHouseAndDate returns bookings including date`() {
        val repo = createRepo()
        val b1 = repo.save(userId1, houseId1, startDate1, endDate1)
        val b2 = repo.save(userId2, houseId1, startDate2, endDate2)

        val bookingsOnJune14 = repo.findByHouseAndDate(houseId1, LocalDate(2025, 6, 14), LocalDate(2025, 6, 14))
        assertEquals(2, bookingsOnJune14.size)
        assertTrue(bookingsOnJune14.contains(b1))
        assertTrue(bookingsOnJune14.contains(b2))

        val bookingsOnJune10 = repo.findByHouseAndDate(houseId1, LocalDate(2025, 6, 10), LocalDate(2025, 6, 10))
        assertEquals(1, bookingsOnJune10.size)
        assertTrue(bookingsOnJune10.contains(b1))
    }

    @Test
    fun `findByUser returns all bookings with correct user`() {
        val repo = createRepo()
        repo.save(userId1, houseId1, startDate1, endDate1)
        repo.save(userId1, houseId1, startDate2, endDate2)
        repo.save(userId1, houseId1, startDate3, endDate3)

        val userId1Bookings = repo.findByUser(userId1)
        assertEquals(3, userId1Bookings.size)
    }

    @Test
    fun `findByUser returns empty list if no bookings exist`() {
        val repo = createRepo()

        val userId1Bookings = repo.findByUser(userId1)
        assertTrue(userId1Bookings.isEmpty())
    }

    @Test
    fun `findByUser respects skip param`() {
        val repo = createRepo()
        repo.save(userId1, houseId1, startDate1, endDate1)
        repo.save(userId1, houseId1, startDate2, endDate2)
        repo.save(userId1, houseId1, startDate3, endDate3)

        val userId1Bookings = repo.findByUser(userId1, skip = 1)
        assertEquals(2, userId1Bookings.size)
    }

    @Test
    fun `findByUser respects limit param`() {
        val repo = createRepo()
        repo.save(userId1, houseId1, startDate1, endDate1)
        repo.save(userId1, houseId1, startDate2, endDate2)
        repo.save(userId1, houseId1, startDate3, endDate3)

        val userId1Bookings = repo.findByUser(userId1, limit = 1)
        assertEquals(1, userId1Bookings.size)
    }

    @Test
    fun `deleteById removes booking if exists`() {
        val repo = createRepo()
        val booking = repo.save(userId1, houseId1, startDate1, endDate1)

        val deleted = repo.deleteById(booking.id)
        assertTrue(deleted)

        val found = repo.findById(booking.id)
        assertNull(found)
    }

    @Test
    fun `deleteById returns false if booking doesn't exist`() {
        val repo = createRepo()

        val deleted = repo.deleteById(1U)
        assertFalse(deleted)
    }

    @Test
    fun `updateBooking updates booking if exists`() {
        val repo = createRepo()
        val booking = repo.save(userId1, houseId1, startDate1, endDate1)

        val updated = repo.updateBooking(booking.id, startDate2, endDate2)
        assertNotNull(updated)
        assertEquals(startDate2, updated.startDate)
        assertEquals(endDate2, updated.endDate)
    }

    @Test
    fun `updateBooking returns null if booking doesn't exist`() {
        val repo = createRepo()

        val updated = repo.updateBooking(1U, startDate2, endDate2)
        assertNull(updated)
    }

    @Test
    fun `updateBooking throws exception if date interval is invalid`() {
        val repo = createRepo()
        val booking = repo.save(userId1, houseId1, startDate1, endDate1)

        assertThrows(IllegalArgumentException::class.java) {
            repo.updateBooking(booking.id, endDate2, startDate2)
        }
    }
}
