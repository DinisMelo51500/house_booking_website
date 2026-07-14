package pt.isel.ls.houses.domain

data class Location(
    val id: Id,
    val name: Name,
    val type: LocationType,
    val parentId: Id? = null,
) {
    init {
        if (type != LocationType.COUNTRY && parentId == null) {
            throw IllegalArgumentException("Non-root location must have a parent Id")
        }
        if (type == LocationType.COUNTRY && parentId != null) {
            throw IllegalArgumentException("Root location (COUNTRY) cannot have a parent Id")
        }
    }
}

enum class LocationType {
    COUNTRY,
    REGION,
    DISTRICT,
    MUNICIPALITY,
    LOCALITY,
}

fun List<Location>.childrenOf(parentId: Id) = this.filter { it.parentId == parentId }
