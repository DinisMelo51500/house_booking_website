package pt.isel.ls.houses.domain

@JvmInline
value class Name(
    val value: String,
) {
    init {
        require(isValidString(value, 100))
    }
}
