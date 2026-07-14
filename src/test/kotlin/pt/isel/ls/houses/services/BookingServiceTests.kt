package pt.isel.ls.houses.services

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertThrows
import pt.isel.ls.houses.data.mem.BookingRepository
import pt.isel.ls.houses.data.mem.HouseRepository
import pt.isel.ls.houses.data.mem.MemBookingRepository
import pt.isel.ls.houses.data.mem.MemHouseRepository
import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookingServiceTests {
    private lateinit var bookingService: BookingService
    private lateinit var houseRepo: HouseRepository
    private lateinit var bookingRepo: BookingRepository

    @BeforeTest
    fun setup() {
        houseRepo = MemHouseRepository()
        bookingRepo = MemBookingRepository()
        bookingService = BookingService(bookingRepo, houseRepo)

        // Criar algumas casas básicas para testes
        houseRepo.save(
            Name("House 1"),
            Id(1),
            AreaSqMt(100),
            PricePerNight(50.0),
            Description("Test House"),
            Id(1),
        )
        houseRepo.save(
            Name("House 2"),
            Id(1),
            AreaSqMt(80),
            PricePerNight(40.0),
            Description("Test House"),
            Id(2),
        )
    }

    @Test
    fun `createBooking creates booking`() {
        val booking =
            bookingService.createBooking(
                Id(1),
                Id(1),
                LocalDate(2026, 1, 1),
                LocalDate(2026, 1, 5),
            )

        assertNotNull(booking)
        assertEquals(Id(1), booking.houseId)
    }

    @Test
    fun `createBooking prevents overlapping bookings`() {
        bookingService.createBooking(
            Id(1),
            Id(2),
            LocalDate(2026, 2, 1),
            LocalDate(2026, 2, 5),
        )

        assertFailsWith<IllegalArgumentException> {
            bookingService.createBooking(
                Id(2),
                Id(2),
                LocalDate(2026, 2, 3),
                LocalDate(2026, 2, 6),
            )
        }
    }

    @Test
    fun `getBookingInfo returns booking`() {
        val booking =
            bookingService.createBooking(
                Id(1),
                Id(1),
                LocalDate(2026, 3, 1),
                LocalDate(2026, 3, 5),
            )

        val found = bookingService.getBookingInfo(booking.id)

        assertNotNull(found)
    }

    @Test
    fun `getAvailableHouses excludes booked houses`() {
        bookingService.createBooking(
            Id(1),
            Id(1),
            LocalDate(2026, 4, 1),
            LocalDate(2026, 4, 5),
        )

        val houses =
            bookingService.getAvailableHouses(
                LocalDate(2026, 4, 1),
                LocalDate(2026, 4, 5),
            )

        assertTrue(houses.none { it.id == Id(1) })
    }

    @Test
    fun `deleteBooking removes booking`() {
        val booking =
            bookingService.createBooking(
                Id(1),
                Id(1),
                LocalDate(2026, 5, 1),
                LocalDate(2026, 5, 5),
            )

        val deleted = bookingService.deleteBooking(booking.id)

        assertTrue(deleted)
        assertNull(bookingService.getBookingInfo(booking.id))
    }

    @Test
    fun `deleteBooking returns false if booking doesn't exist`() {
        val deleted = bookingService.deleteBooking(1U)

        assertFalse(deleted)
    }

    @Test
    fun `updateBooking changes dates`() {
        val booking =
            bookingService.createBooking(
                Id(1),
                Id(1),
                LocalDate(2026, 5, 1),
                LocalDate(2026, 5, 5),
            )
        val newStartDate = LocalDate(2026, 5, 2)
        val newEndDate = LocalDate(2026, 5, 6)

        val updated =
            bookingService.updateBooking(
                booking.id,
                newStartDate,
                newEndDate,
            )

        assertEquals(booking.id, updated?.id)
        assertEquals(newStartDate, updated?.startDate)
        assertEquals(newEndDate, updated?.endDate)

        val found = bookingService.getBookingInfo(booking.id)
        assertEquals(newStartDate, found?.startDate)
        assertEquals(newEndDate, found?.endDate)
    }

    @Test
    fun `updateBooking returns null if booking doesn't exist`() {
        val newStartDate = LocalDate(2026, 5, 2)
        val newEndDate = LocalDate(2026, 5, 6)

        val updated =
            bookingService.updateBooking(
                1U,
                newStartDate,
                newEndDate,
            )

        assertNull(updated)
    }

    @Test
    fun `updateBooking throws exception if date interval is invalid`() {
        val booking =
            bookingService.createBooking(
                Id(1),
                Id(1),
                LocalDate(2026, 5, 1),
                LocalDate(2026, 5, 5),
            )

        val booking2StartDate = LocalDate(2026, 5, 6)
        val booking2EndDate = LocalDate(2026, 5, 5)

        assertThrows(IllegalArgumentException::class.java) {
            bookingService.updateBooking(
                booking.id,
                booking2StartDate,
                booking2EndDate,
            )
        }
    }

    @Test
    fun `updateBooking throws exception if house already booked during new date interval`() {
        val booking =
            bookingService.createBooking(
                Id(1),
                Id(1),
                LocalDate(2026, 5, 1),
                LocalDate(2026, 5, 5),
            )

        val booking2StartDate = LocalDate(2026, 5, 6)
        val booking2EndDate = LocalDate(2026, 5, 10)

        bookingService.createBooking(
            Id(1),
            Id(1),
            booking2StartDate,
            booking2EndDate,
        )

        assertThrows(IllegalArgumentException::class.java) {
            bookingService.updateBooking(
                booking.id,
                booking2StartDate,
                booking2EndDate,
            )
        }
    }

    @Test
    fun `getBookingsByUser returns user bookings`() {
        bookingService.createBooking(
            Id(1),
            Id(1),
            LocalDate(2026, 6, 1),
            LocalDate(2026, 6, 5),
        )
        bookingService.createBooking(
            Id(1),
            Id(2),
            LocalDate(2026, 6, 10),
            LocalDate(2026, 6, 15),
        )

        val bookings = bookingService.getBookingsByUser(Id(1))

        assertEquals(2, bookings.size)
    }

    @Test
    fun `getBookingsByUser returns empty list if no bookings exist`() {
        val bookings = bookingService.getBookingsByUser(Id(1))

        assertTrue(bookings.isEmpty())
    }

    @Test
    fun `getBookingsByUser respects skip param`() {
        bookingService.createBooking(
            Id(1),
            Id(1),
            LocalDate(2026, 6, 1),
            LocalDate(2026, 6, 5),
        )
        bookingService.createBooking(
            Id(1),
            Id(2),
            LocalDate(2026, 6, 10),
            LocalDate(2026, 6, 15),
        )

        val bookings = bookingService.getBookingsByUser(Id(1), skip = 1)

        assertEquals(1, bookings.size)
    }

    @Test
    fun `getBookingsByUser respects limit param`() {
        bookingService.createBooking(
            Id(1),
            Id(1),
            LocalDate(2026, 6, 1),
            LocalDate(2026, 6, 5),
        )
        bookingService.createBooking(
            Id(1),
            Id(2),
            LocalDate(2026, 6, 10),
            LocalDate(2026, 6, 15),
        )

        val bookings = bookingService.getBookingsByUser(Id(1), skip = 1)

        assertEquals(1, bookings.size)
    }
}
