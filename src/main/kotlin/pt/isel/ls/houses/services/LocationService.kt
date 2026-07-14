package pt.isel.ls.houses.services

import pt.isel.ls.houses.data.jdbc.JDBCLocationRepository
import pt.isel.ls.houses.data.mem.LocationRepository
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Location
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name

class LocationService(
    private val repo: LocationRepository = JDBCLocationRepository(),
) {
    fun createLocation(
        name: Name,
        type: LocationType,
        parentId: Id? = null,
    ): Id = repo.save(name, type, parentId).id

    fun getLocationInfo(id: Id): Location? = repo.findById(id)

    fun getChildrenLocations(
        parentId: Id,
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
    ): List<Location> = repo.findChildren(parentId, skip, limit)

    fun getFullLocationPath(locationId: Id): List<Location> = repo.getFullPath(locationId)

    fun getAllLocations(
        skip: Int = 0,
        limit: Int = Int.MAX_VALUE,
    ): List<Location> = repo.findAll(skip, limit)
}
