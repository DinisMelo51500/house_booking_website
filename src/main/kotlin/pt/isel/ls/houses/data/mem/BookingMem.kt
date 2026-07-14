package pt.isel.ls.houses.data.mem

import kotlinx.datetime.LocalDate
import pt.isel.ls.houses.domain.Booking
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.services.DEFAULT_LIMIT
import pt.isel.ls.houses.services.DEFAULT_SKIP

interface BookingRepository {
    fun save(
        userId: Id,
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking

    fun findById(id: Id): Booking?

    fun findByHouse(
        houseId: Id,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Booking>

    fun findByUser(
        userId: Id,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Booking>

    fun findByHouseAndDate(
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Booking>

    fun findBookingsInPeriod(
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Booking>

    fun findAvailableBookings(
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Booking>

    fun deleteById(id: Id): Boolean

    fun updateBooking(
        id: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking?
}

class MemBookingRepository : BookingRepository {
    private val bookings = HashMap<UInt, Booking>()
    private var idGenerator = 1

    override fun save(
        userId: Id,
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking {
        require(endDate >= startDate) { "End date must be on or after start date" }

        val booking =
            Booking(
                id = Id(idGenerator),
                userId = userId,
                houseId = houseId,
                startDate = startDate,
                endDate = endDate,
            )

        bookings[booking.id] = booking
        idGenerator++

        return booking
    }

    override fun findById(id: Id): Booking? = bookings[id]

    override fun findByUser(
        userId: Id,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        bookings.values
            .filter { it.userId == userId }
            .drop(skip)
            .take(limit)

    override fun findByHouse(
        houseId: Id,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        bookings.values
            .filter { it.houseId == houseId }
            .drop(skip)
            .take(limit)

    override fun findByHouseAndDate(
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        bookings.values
            .filter {
                it.houseId == houseId &&
                    it.startDate <= endDate &&
                    it.endDate >= startDate
            }.drop(skip)
            .take(limit)

    override fun findBookingsInPeriod(
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Booking> =
        bookings.values.filter {
            it.houseId == houseId &&
                it.overlaps(startDate, endDate)
        }

    override fun findAvailableBookings(
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        bookings.values
            .filter {
                it.overlaps(startDate, endDate)
            }.drop(skip)
            .take(limit)

    override fun deleteById(id: Id): Boolean = bookings.remove(id) != null

    override fun updateBooking(
        id: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking? {
        require(endDate >= startDate) {
            "End date must be on or after start date"
        }

        val current = bookings[id] ?: return null

        val updated =
            current.copy(
                startDate = startDate,
                endDate = endDate,
            )

        bookings[id] = updated

        return updated
    }

    fun clear() {
        bookings.clear()
        idGenerator = 1
    }
}
