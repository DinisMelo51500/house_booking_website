package pt.isel.ls.houses.services
import kotlinx.serialization.Serializable
import pt.isel.ls.houses.Scale
import pt.isel.ls.houses.predict
import kotlin.math.roundToLong

@Serializable
data class RentalPrediction(
    val predictedPricePerNight: Long,
    val predictedTotalPrice: Long,
)

class PricePredictionService(
    private val params: pt.isel.ls.houses.Params,
    private val areaScale: Scale,
    private val priceScale: Scale,
) {
    fun predictRental(
        areaSqMt: Double,
        nights: Int,
    ): RentalPrediction {
        val areaNorm = areaScale.normalize(areaSqMt)
        val priceNorm = predict(areaNorm, params)
        val totalPrice = priceScale.denormalize(priceNorm)
        val pricePerNight = totalPrice / nights

        return RentalPrediction(
            predictedPricePerNight = pricePerNight.roundToLong(),
            predictedTotalPrice = totalPrice.roundToLong(),
        )
    }
}
