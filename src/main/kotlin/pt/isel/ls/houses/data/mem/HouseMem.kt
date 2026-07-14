package pt.isel.ls.houses.data.mem

import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.House
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import pt.isel.ls.houses.services.DEFAULT_LIMIT
import pt.isel.ls.houses.services.DEFAULT_SKIP

interface HouseRepository {
    fun save(
        title: Name,
        location: Id,
        areaSqMt: AreaSqMt,
        pricePerNight: PricePerNight,
        description: Description,
        ownerId: Id,
    ): House

    fun findById(id: Id): House?

    fun findAll(
        skip: Int = DEFAULT_SKIP,
        limit: Int = DEFAULT_LIMIT,
        minArea: AreaSqMt,
        location: Id? = null,
        maxPrice: PricePerNight,
    ): List<House>
}

class MemHouseRepository : HouseRepository {
    private val houses = HashMap<UInt, House>()
    private var idGenerator = 1

    override fun save(
        title: Name,
        location: Id,
        areaSqMt: AreaSqMt,
        pricePerNight: PricePerNight,
        description: Description,
        ownerId: Id,
    ): House {
        val house =
            House(
                id = Id(idGenerator),
                title = title,
                location = location,
                areaSqMt = areaSqMt,
                pricePerNight = pricePerNight,
                description = description,
                ownerId = ownerId,
            )
        idGenerator++
        houses[house.id] = house
        return house
    }

    override fun findById(id: Id): House? = houses[id]

    override fun findAll(
        skip: Int,
        limit: Int,
        minArea: AreaSqMt,
        location: Id?,
        maxPrice: PricePerNight,
    ): List<House> =
        houses.values
            .toList()
            .filter {
                it.areaSqMt >= minArea && it.pricePerNight.value <= maxPrice.value &&
                    (location == null || it.location == location)
            }.drop(skip)
            .take(limit)

    fun clear() {
        houses.clear()
        idGenerator = 1
    }
}
