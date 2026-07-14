package pt.isel.ls.houses.data.mem

import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Location
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.services.DEFAULT_LIMIT
import pt.isel.ls.houses.services.DEFAULT_SKIP

interface LocationRepository {
    fun save(
        name: Name,
        type: LocationType,
        parentId: Id? = null,
    ): Location

    fun findById(id: Id): Location?

    fun findAll(
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Location>

    fun findChildren(
        parentId: Id,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Location>

    fun getFullPath(locationId: Id): List<Location>
}

class MemLocationRepository : LocationRepository {
    private val locations = HashMap<UInt, Location>()
    private var idGenerator = 1

    override fun save(
        name: Name,
        type: LocationType,
        parentId: Id?,
    ): Location {
        if (parentId != null && locations.values.none { it.id == parentId }) {
            throw IllegalArgumentException("Parent with id $parentId doesnt exist")
        }
        val location =
            Location(
                id = Id(idGenerator),
                name = name,
                type = type,
                parentId = parentId,
            )
        locations[location.id] = location
        idGenerator++
        return location
    }

    override fun findById(id: Id): Location? = locations[id]

    override fun findAll(
        skip: Int,
        limit: Int,
    ): List<Location> =
        locations.values
            .toList()
            .drop(skip)
            .take(limit)

    override fun findChildren(
        parentId: Id,
        skip: Int,
        limit: Int,
    ): List<Location> =
        locations.values
            .filter { it.parentId == parentId }
            .drop(skip)
            .take(limit)

    override fun getFullPath(locationId: Id): List<Location> {
        val path = mutableListOf<Location>()
        var current = findById(locationId)
        while (current != null) {
            path.add(0, current)
            current = current.parentId?.let { findById(it) }
        }
        return path
    }

    fun clear() {
        locations.clear()
        idGenerator = 1
    }
}
