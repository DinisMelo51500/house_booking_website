package pt.isel.ls.houses.domain

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertThrows
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingTest {
    private val bookingId = Id(1)
    private val userId = Id(2)
    private val houseId = Id(3)

    private val startDate = LocalDate(2025, 6, 10)
    private val endDate = LocalDate(2025, 6, 15)

    @Test
    fun `creates booking with valid dates`() {
        Booking(
            id = bookingId,
            startDate = startDate,
            endDate = endDate,
            userId = userId,
            houseId = houseId,
        )
    }

    @Test
    fun `throws when end date is before start date`() {
        assertThrows(IllegalArgumentException::class.java) {
            Booking(
                id = bookingId,
                startDate = LocalDate(2025, 6, 15),
                endDate = LocalDate(2025, 6, 10),
                userId = userId,
                houseId = houseId,
            )
        }
    }

    @Test
    fun `includes returns true when date is inside booking`() {
        val booking =
            Booking(
                id = bookingId,
                startDate = startDate,
                endDate = endDate,
                userId = userId,
                houseId = houseId,
            )

        assertTrue(booking.includes(LocalDate(2025, 6, 12)))
    }

    @Test
    fun `includes returns true for start date`() {
        val booking =
            Booking(
                id = bookingId,
                startDate = startDate,
                endDate = endDate,
                userId = userId,
                houseId = houseId,
            )

        assertTrue(booking.includes(startDate))
    }

    @Test
    fun `includes returns true for end date`() {
        val booking =
            Booking(
                id = bookingId,
                startDate = startDate,
                endDate = endDate,
                userId = userId,
                houseId = houseId,
            )

        assertTrue(booking.includes(endDate))
    }

    @Test
    fun `includes returns false when date is outside booking`() {
        val booking =
            Booking(
                id = bookingId,
                startDate = startDate,
                endDate = endDate,
                userId = userId,
                houseId = houseId,
            )

        assertFalse(booking.includes(LocalDate(2025, 6, 20)))
    }

    @Test
    fun `overlaps returns true when bookings intersect`() {
        val booking =
            Booking(
                id = bookingId,
                startDate = startDate,
                endDate = endDate,
                userId = userId,
                houseId = houseId,
            )

        assertTrue(
            booking.overlaps(
                LocalDate(2025, 6, 12),
                LocalDate(2025, 6, 18),
            ),
        )
    }

    @Test
    fun `overlaps returns true when range fully inside booking`() {
        val booking =
            Booking(
                id = bookingId,
                startDate = startDate,
                endDate = endDate,
                userId = userId,
                houseId = houseId,
            )

        assertTrue(
            booking.overlaps(
                LocalDate(2025, 6, 11),
                LocalDate(2025, 6, 13),
            ),
        )
    }

    @Test
    fun `overlaps returns false when ranges do not intersect`() {
        val booking =
            Booking(
                id = bookingId,
                startDate = startDate,
                endDate = endDate,
                userId = userId,
                houseId = houseId,
            )

        assertFalse(
            booking.overlaps(
                LocalDate(2025, 6, 20),
                LocalDate(2025, 6, 25),
            ),
        )
    }
}
