package pt.isel.ls.houses

/*
 Linear Regression em Kotlin
 Previsão de preço de aluguer baseado na área da casa
*/

// ===============================
// 1. Estrutura de dados
// ===============================

data class House(
    val area: Double,
    val price: Double,
)

val houses =
    listOf(
        House(35.0, 120000.0),
        House(52.0, 155000.0),
        House(70.0, 210000.0),
        House(95.0, 260000.0),
        House(140.0, 340000.0),
        House(220.0, 480000.0),
    )

// ===============================
// 2. Normalização
// ===============================

class Scale(
    values: List<Double>,
) {
    init {
        require(values.isNotEmpty()) { "Scale requires a non-empty list" }
    }

    val min: Double = values.minOrNull() ?: 0.0
    val max: Double = values.maxOrNull() ?: 1.0
    val delta: Double = max - min

    fun normalize(value: Double) = (value - min) / delta

    fun denormalize(value: Double) = value * delta + min
}

data class NormalizedData(
    val areas: Scale,
    val prices: Scale,
    val data: List<House>,
)

fun List<House>.normalize(): NormalizedData {
    val areas = Scale(map { it.area })
    val prices = Scale(map { it.price })
    return NormalizedData(
        areas,
        prices,
        map { House(areas.normalize(it.area), prices.normalize(it.price)) },
    )
}

// ===============================
// 3. Modelo de regressão linear
// ===============================

data class Params(
    val w: Double,
    val b: Double,
)

operator fun Params.plus(other: Params) = Params(w + other.w, b + other.b)

fun predict(
    x: Double,
    p: Params,
): Double = p.w * x + p.b

fun error(
    yPred: Double,
    yReal: Double,
) = yPred - yReal

fun gradients(
    x: Double,
    error: Double,
    n: Int,
) = Params(
    (2.0 / n) * error * x,
    (2.0 / n) * error,
)

fun updateParams(
    p: Params,
    delta: Params,
    lr: Double,
) = Params(
    p.w - lr * delta.w,
    p.b - lr * delta.b,
)

// ===============================
// 4. Treinamento
// ===============================

fun train(
    data: List<House>,
    epochs: Int = 3000,
    lr: Double = 0.05,
): Params {
    var params = Params(0.0, 0.0)

    repeat(epochs) {
        val total =
            data.fold(Params(0.0, 0.0)) { p, house ->
                val yPred = predict(house.area, params)
                val e = error(yPred, house.price)
                p + gradients(house.area, e, data.size)
            }
        params = updateParams(params, total, lr)
    }

    return params
}
/*
// ===============================
// 5. PricePredictionService
// ===============================

fun buildPricePredictionService(): PricePredictionService {
    val (areas, prices, normalizedData) = houses.normalize()
    val params = train(normalizedData)
    return PricePredictionService(params, areas, prices)
}
 */
