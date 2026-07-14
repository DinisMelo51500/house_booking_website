package pt.isel.ls.houses.services

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import pt.isel.ls.houses.data.jdbc.JDBCBookingRepository
import pt.isel.ls.houses.data.jdbc.JDBCHouseRepository
import pt.isel.ls.houses.data.mem.BookingRepository
import pt.isel.ls.houses.data.mem.HouseRepository
import pt.isel.ls.houses.domain.Booking
import pt.isel.ls.houses.domain.House
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.PricePerNight

class BookingService(
    private val bookingRepo: BookingRepository = JDBCBookingRepository(),
    private val houseRepo: HouseRepository = JDBCHouseRepository(),
) {
    fun createBooking(
        userId: Id,
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking {
        require(
            bookingRepo
                .findByHouse(houseId, DEFAULT_SKIP, Int.MAX_VALUE)
                .none { it.overlaps(startDate, endDate) },
        ) {
            "House is already booked for the given period"
        }

        return bookingRepo.save(userId, houseId, startDate, endDate)
    }

    fun getBookingInfo(id: Id): Booking? = bookingRepo.findById(id)

    fun getBookingsByHouseAndDate(
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Booking> {
        require(startDate <= endDate) {
            "startDate must be before or equal to endDate"
        }

        return bookingRepo.findByHouseAndDate(
            houseId,
            startDate,
            endDate,
            skip,
            limit,
        )
    }

    fun getAvailableHouses(
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<House> =
        houseRepo
            .findAll(
                minArea = 0u,
                maxPrice = PricePerNight(Double.MAX_VALUE),
            ).filter { house ->
                bookingRepo
                    .findAvailableBookings(startDate, endDate)
                    .none { it.houseId == house.id }
            }.drop(skip)
            .take(limit)

    fun getAvailableDays(
        houseId: Id,
        year: Int,
        month: Int,
    ): List<Int> {
        require(month in 1..12) {
            "Month must be between 1 and 12"
        }

        val startDate = LocalDate(year, month, 1)

        val endDate =
            if (month == 12) {
                LocalDate(year + 1, 1, 1)
                    .plus(DatePeriod(days = -1))
            } else {
                LocalDate(year, month + 1, 1)
                    .plus(DatePeriod(days = -1))
            }

        val bookings =
            bookingRepo.findBookingsInPeriod(
                houseId,
                startDate,
                endDate,
            )

        val unavailableDays = mutableSetOf<Int>()

        bookings.forEach { booking ->

            val bookingStart =
                if (booking.startDate < startDate) {
                    startDate
                } else {
                    booking.startDate
                }

            val bookingEnd =
                if (booking.endDate > endDate) {
                    endDate
                } else {
                    booking.endDate
                }

            for (i in 0..bookingStart.daysUntil(bookingEnd)) {
                unavailableDays.add(
                    bookingStart
                        .plus(DatePeriod(days = i))
                        .dayOfMonth,
                )
            }
        }

        return (1..endDate.dayOfMonth)
            .filter { it !in unavailableDays }
    }

    fun deleteBooking(id: Id): Boolean = bookingRepo.deleteById(id)

    fun updateBooking(
        id: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking? {
        val booking =
            bookingRepo.findById(id)
                ?: return null

        require(
            bookingRepo
                .findByHouseAndDate(
                    booking.houseId,
                    startDate,
                    endDate,
                    DEFAULT_SKIP,
                    Int.MAX_VALUE,
                ).none { it.id != id },
        ) {
            "House is already booked for the given period"
        }

        return bookingRepo.updateBooking(
            id,
            startDate,
            endDate,
        )
    }

    fun getBookingsByUser(
        userId: Id,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Booking> =
        bookingRepo.findByUser(
            userId,
            skip,
            limit,
        )
}
