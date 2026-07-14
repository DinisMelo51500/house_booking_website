package pt.isel.ls.houses.data.jdbc

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import pt.isel.ls.houses.data.mem.BookingRepository
import pt.isel.ls.houses.domain.Booking
import pt.isel.ls.houses.domain.Id
import java.sql.Connection
import java.sql.ResultSet

class JDBCBookingRepository(
    private val connection: Connection? = null,
) : BookingRepository {
    private fun <T> execute(block: (Connection) -> T): T =
        if (connection != null) {
            block(connection)
        } else {
            withConnection { block(it) }
        }

    override fun save(
        userId: Id,
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking =
        execute { connection ->
            val sql =
                """
                INSERT INTO bookings (user_id, house_id, start_date, end_date)
                VALUES (?, ?, ?, ?)
                RETURNING id_booking
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, userId.toInt())
                stmt.setInt(2, houseId.toInt())
                stmt.setObject(3, startDate.toJavaLocalDate())
                stmt.setObject(4, endDate.toJavaLocalDate())

                val rs = stmt.executeQuery()

                if (rs.next()) {
                    Booking(
                        id = rs.getInt("id_booking").toUInt(),
                        startDate = startDate,
                        endDate = endDate,
                        userId = userId,
                        houseId = houseId,
                    )
                } else {
                    throw Exception("Failed to insert booking")
                }
            }
        }

    override fun findById(id: Id): Booking? =
        execute { connection ->
            val sql = "SELECT * FROM bookings WHERE id_booking = ?"

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id.toInt())

                val rs = stmt.executeQuery()

                if (rs.next()) rs.toBooking() else null
            }
        }

    override fun findByUser(
        userId: Id,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        execute { connection ->
            val sql =
                """
                SELECT * FROM bookings
                WHERE user_id = ?
                ORDER BY start_date
                OFFSET ? LIMIT ?
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, userId.toInt())
                stmt.setInt(2, skip)
                stmt.setInt(3, limit)

                val rs = stmt.executeQuery()

                val result = mutableListOf<Booking>()

                while (rs.next()) {
                    result.add(rs.toBooking())
                }

                result
            }
        }

    override fun findByHouse(
        houseId: Id,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        execute { connection ->
            val sql =
                """
                SELECT * FROM bookings
                WHERE house_id = ?
                ORDER BY start_date
                OFFSET ? LIMIT ?
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, houseId.toInt())
                stmt.setInt(2, skip)
                stmt.setInt(3, limit)

                val rs = stmt.executeQuery()

                val result = mutableListOf<Booking>()

                while (rs.next()) {
                    result.add(rs.toBooking())
                }

                result
            }
        }

    override fun findByHouseAndDate(
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        execute { connection ->
            val sql =
                """
                SELECT * FROM bookings
                WHERE house_id = ?
                  AND end_date >= ?
                  AND start_date <= ?
                ORDER BY start_date
                OFFSET ? LIMIT ?
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, houseId.toInt())
                stmt.setObject(2, startDate.toJavaLocalDate())
                stmt.setObject(3, endDate.toJavaLocalDate())
                stmt.setInt(4, skip)
                stmt.setInt(5, limit)

                val rs = stmt.executeQuery()

                val result = mutableListOf<Booking>()

                while (rs.next()) {
                    result.add(rs.toBooking())
                }

                result
            }
        }

    override fun findBookingsInPeriod(
        houseId: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Booking> =
        execute { connection ->
            val sql =
                """
                SELECT * FROM bookings
                WHERE house_id = ?
                  AND end_date >= ?
                  AND start_date <= ?
                ORDER BY start_date
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, houseId.toInt())
                stmt.setObject(2, startDate.toJavaLocalDate())
                stmt.setObject(3, endDate.toJavaLocalDate())

                val rs = stmt.executeQuery()

                val result = mutableListOf<Booking>()

                while (rs.next()) {
                    result.add(rs.toBooking())
                }

                result
            }
        }

    override fun findAvailableBookings(
        startDate: LocalDate,
        endDate: LocalDate,
        skip: Int,
        limit: Int,
    ): List<Booking> =
        execute { connection ->
            val sql =
                """
                SELECT * FROM bookings
                WHERE end_date >= ?
                  AND start_date <= ?
                ORDER BY start_date
                OFFSET ? LIMIT ?
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setObject(1, startDate.toJavaLocalDate())
                stmt.setObject(2, endDate.toJavaLocalDate())
                stmt.setInt(3, skip)
                stmt.setInt(4, limit)

                val rs = stmt.executeQuery()

                val result = mutableListOf<Booking>()

                while (rs.next()) {
                    result.add(rs.toBooking())
                }

                result
            }
        }

    override fun deleteById(id: Id): Boolean =
        execute { connection ->
            val sql = "DELETE FROM bookings WHERE id_booking = ?"

            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id.toInt())

                stmt.executeUpdate() > 0
            }
        }

    override fun updateBooking(
        id: Id,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Booking? =
        execute { connection ->
            val sql =
                """
                UPDATE bookings
                SET start_date = ?, end_date = ?
                WHERE id_booking = ?
                RETURNING *
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setObject(1, startDate.toJavaLocalDate())
                stmt.setObject(2, endDate.toJavaLocalDate())
                stmt.setInt(3, id.toInt())

                val rs = stmt.executeQuery()

                if (rs.next()) rs.toBooking() else null
            }
        }
}

private fun ResultSet.toBooking() =
    Booking(
        id = getInt("id_booking").toUInt(),
        startDate = getObject("start_date", java.time.LocalDate::class.java).toKotlinLocalDate(),
        endDate = getObject("end_date", java.time.LocalDate::class.java).toKotlinLocalDate(),
        userId = getInt("user_id").toUInt(),
        houseId = getInt("house_id").toUInt(),
    )
