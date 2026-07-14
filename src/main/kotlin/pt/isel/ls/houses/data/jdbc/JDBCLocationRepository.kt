package pt.isel.ls.houses.data.jdbc

import pt.isel.ls.houses.data.mem.LocationRepository
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Location
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types

class JDBCLocationRepository(
    private val connection: Connection? = null,
) : LocationRepository {
    private fun <T> execute(block: (Connection) -> T): T =
        if (connection != null) {
            block(connection)
        } else {
            withConnection { block(it) }
        }

    override fun save(
        name: Name,
        type: LocationType,
        parentId: Id?,
    ): Location =
        execute { connection ->
            val sql =
                """
                INSERT INTO locations (name, type, parent_id)
                VALUES (?, ?::location_type, ?)
                RETURNING id_loc
                """.trimIndent()
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, name.value)
                stmt.setString(2, type.name)
                if (parentId != null) stmt.setInt(3, parentId.toInt()) else stmt.setNull(3, Types.INTEGER)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    Location(
                        id = rs.getInt("id_loc").toUInt(),
                        name = name,
                        type = type,
                        parentId = parentId,
                    )
                } else {
                    throw Exception("Failed to insert location")
                }
            }
        }

    override fun findById(id: Id): Location? =
        execute { connection ->
            val sql = "SELECT * FROM locations WHERE id_loc = ?"
            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id.toInt())
                val rs = stmt.executeQuery()
                if (rs.next()) rs.toLocation() else null
            }
        }

    override fun findAll(
        skip: Int,
        limit: Int,
    ): List<Location> =
        execute { connection ->
            val sql = "SELECT * FROM locations LIMIT ? OFFSET ?"
            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, limit)
                stmt.setInt(2, skip)
                val rs = stmt.executeQuery()
                val result = mutableListOf<Location>()
                while (rs.next()) result.add(rs.toLocation())
                result
            }
        }

    override fun findChildren(
        parentId: Id,
        skip: Int,
        limit: Int,
    ): List<Location> =
        execute { connection ->
            val sql = "SELECT * FROM locations WHERE parent_id = ? LIMIT ? OFFSET ?"
            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, parentId.toInt())
                stmt.setInt(2, limit)
                stmt.setInt(3, skip)
                val rs = stmt.executeQuery()
                val result = mutableListOf<Location>()
                while (rs.next()) result.add(rs.toLocation())
                result
            }
        }

    override fun getFullPath(locationId: Id): List<Location> =
        execute { connection ->
            val sql =
                """
                WITH RECURSIVE path AS (
                    SELECT * FROM locations WHERE id_loc = ?
                    UNION ALL
                    SELECT l.* FROM locations l
                    INNER JOIN path p ON l.id_loc = p.parent_id
                )
                SELECT * FROM path
                """.trimIndent()
            connection.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, locationId.toInt())
                val rs = stmt.executeQuery()
                val result = mutableListOf<Location>()
                while (rs.next()) result.add(rs.toLocation())
                result.reversed()
            }
        }
}

private fun ResultSet.toLocation() =
    Location(
        id = getInt("id_loc").toUInt(),
        name = Name(getString("name")),
        type = LocationType.valueOf(getString("type")),
        parentId = getInt("parent_id").takeIf { !wasNull() }?.toUInt(),
    )
