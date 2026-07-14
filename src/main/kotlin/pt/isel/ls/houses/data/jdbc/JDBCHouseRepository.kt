package pt.isel.ls.houses.data.jdbc

import pt.isel.ls.houses.data.mem.HouseRepository
import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.House
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import java.sql.Connection
import java.sql.ResultSet

class JDBCHouseRepository(
    private val connection: Connection? = null,
) : HouseRepository {
    private fun <T> execute(block: (Connection) -> T): T =
        if (connection != null) {
            block(connection)
        } else {
            withConnection { block(it) }
        }

    override fun save(
        title: Name,
        location: Id,
        areaSqMt: AreaSqMt,
        pricePerNight: PricePerNight,
        description: Description,
        ownerId: Id,
    ): House =
        execute { connection ->
            val sql =
                """
                INSERT INTO houses (title, location_id, area_sq_mt, price_per_night, description, owner_id)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_house
                """.trimIndent()

            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, title.value)
                stmt.setInt(2, location.toInt())
                stmt.setInt(3, areaSqMt.toInt())
                stmt.setDouble(4, pricePerNight.value)
                stmt.setString(5, description.value)
                stmt.setInt(6, ownerId.toInt())

                val rs = stmt.executeQuery()
                if (rs.next()) {
                    House(
                        id = rs.getInt("id_house").toUInt(),
                        title = title,
                        location = location,
                        areaSqMt = areaSqMt,
                        pricePerNight = pricePerNight,
                        description = description,
                        ownerId = ownerId,
                    )
                } else {
                    throw Exception("Failed to insert house")
                }
            }
        }

    override fun findById(id: Id): House? =
        execute { connection ->
            val sql = "SELECT * FROM houses WHERE id_house = ?"
            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id.toInt())
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toHouse() else null
            }
        }

    override fun findAll(
        skip: Int,
        limit: Int,
        minArea: AreaSqMt,
        location: Id?,
        maxPrice: PricePerNight,
    ): List<House> =
        execute { connection ->
            val sql =
                """
                SELECT * FROM houses
                WHERE area_sq_mt >= ? AND price_per_night <= ? ${location?.let { "AND location_id = ?" } ?: ""}
                OFFSET ? LIMIT ?
                """.trimIndent()
            connection.prepareStatement(sql).use { stmt ->

                val locationIndex = if (location != null) 3 else null
                val skipIndex = if (location != null) 4 else 3
                val limitIndex = if (location != null) 5 else 4

                stmt.setInt(1, minArea.toInt())
                stmt.setDouble(2, maxPrice.value)
                location?.let { stmt.setInt(locationIndex!!, it.toInt()) }
                stmt.setInt(skipIndex, skip)
                stmt.setInt(limitIndex, limit)
                val rs = stmt.executeQuery()
                val houses = mutableListOf<House>()
                while (rs.next()) {
                    houses.add(rs.toHouse())
                }
                houses
            }
        }
}

fun ResultSet.toHouse() =
    House(
        id = getInt("id_house").toUInt(),
        title = Name(getString("title")),
        location = getInt("location_id").toUInt(),
        areaSqMt = getInt("area_sq_mt").toUInt(),
        pricePerNight = PricePerNight(getDouble("price_per_night")),
        description = Description(getString("description")),
        ownerId = getInt("owner_id").toUInt(),
    )
