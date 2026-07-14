package pt.isel.ls.houses.webapi
import kotlinx.serialization.Serializable

@Serializable
data class PricePredictionRequest(
    val areaSqMt: Double,
    val nights: Int,
)
