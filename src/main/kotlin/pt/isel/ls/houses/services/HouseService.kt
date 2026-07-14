package pt.isel.ls.houses.services

import pt.isel.ls.houses.data.jdbc.JDBCHouseRepository
import pt.isel.ls.houses.data.mem.HouseRepository
import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.House
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight

class HouseService(
    private val repo: HouseRepository = JDBCHouseRepository(),
) {
    fun createHouse(
        title: Name,
        location: Id,
        areaSqMt: AreaSqMt,
        pricePerNight: PricePerNight,
        description: Description,
        ownerId: Id,
    ): Id = repo.save(title, location, areaSqMt, pricePerNight, description, ownerId).id

    fun getHouseInfo(id: Id): House? = repo.findById(id)

    fun getAllHouses(
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
        minArea: AreaSqMt,
        location: Id? = null,
        maxPrice: PricePerNight,
    ): List<House> =
        repo.findAll(
            skip,
            limit,
            minArea,
            location,
            maxPrice,
        )
}
