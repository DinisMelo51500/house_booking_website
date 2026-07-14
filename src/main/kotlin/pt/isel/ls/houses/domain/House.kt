package pt.isel.ls.houses.domain

data class House(
    val id: Id,
    val title: Name,
    val location: Id,
    val areaSqMt: AreaSqMt,
    val pricePerNight: PricePerNight,
    val description: Description,
    val ownerId: Id,
)

typealias AreaSqMt = UInt

@JvmInline
value class PricePerNight(
    val value: Double,
) {
    init {
        require(value > 0)
    }
}

@JvmInline
value class Description(
    val value: String,
) {
    init {
        require(isValidString(value, 500))
    }
}

fun isValidString(
    string: String,
    l: Int,
): Boolean = string.isNotBlank() && string.length < l
