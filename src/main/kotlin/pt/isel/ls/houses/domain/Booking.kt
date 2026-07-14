package pt.isel.ls.houses.domain
import kotlinx.datetime.LocalDate

data class Booking(
    val id: Id,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: Id,
    val houseId: Id,
) {
    init {
        require(endDate >= startDate)
    }

    fun includes(date: LocalDate): Boolean = date >= startDate && date <= endDate

    fun overlaps(
        start: LocalDate,
        end: LocalDate,
    ): Boolean = end >= startDate && start <= endDate
}
