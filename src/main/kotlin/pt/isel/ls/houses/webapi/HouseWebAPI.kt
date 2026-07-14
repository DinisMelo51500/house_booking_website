package pt.isel.ls.houses.webapi

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.House
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import pt.isel.ls.houses.services.HouseService
import pt.isel.ls.houses.services.UserService

@Serializable
data class HouseResponse(
    val id: Id,
    val title: String,
    val location: Id,
    val areaSqMt: AreaSqMt,
    val pricePerNight: Double,
    val description: String,
    val ownerId: Id,
)

private fun House.toResponse() = HouseResponse(id, title.value, location, areaSqMt, pricePerNight.value, description.value, ownerId)

class HouseWebApi(
    private val houseService: HouseService = HouseService(),
    private val userService: UserService = UserService(),
) {
    fun postHouse(request: Request): Response {
        return try {
            val ownerId = authenticate(request, userService)?.id ?: return Response(BAD_REQUEST).body("Invalid or missing token")
            val body = Json.decodeFromString<Map<String, String>>(request.bodyString())
            val title = Name(body["title"] ?: error("Missing title"))
            val location = Id((body["location"] ?: error("Missing location")).toInt())
            val area = AreaSqMt((body["areaSqMt"] ?: error("Missing areaSqMt")).toInt())
            val price = PricePerNight((body["pricePerNight"] ?: error("Missing pricePerNight")).toDouble())
            val description = Description(body["description"] ?: error("Missing description"))

            val id =
                houseService.createHouse(
                    title,
                    location,
                    area,
                    price,
                    description,
                    ownerId,
                )

            Response(CREATED)
                .header("Content-Type", "application/json")
                .body(
                    Json.encodeToString(
                        HouseResponse(id, title.value, location, area, price.value, description.value, ownerId),
                    ),
                )
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }
    }

    fun getHouseById(request: Request): Response {
        val id = request.path("id")!!.toUInt()
        val house = houseService.getHouseInfo(id)

        return if (house == null) {
            Response(NOT_FOUND).body("House Not Found")
        } else {
            Response(OK)
                .header("Content-Type", "application/json")
                .body(Json.encodeToString(house.toResponse()))
        }
    }

    fun getHouses(request: Request): Response {
        val skip = request.query("skip")?.toIntOrNull() ?: 0
        val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE
        val minArea = request.query("area")?.toUIntOrNull() ?: 0u
        val location = request.query("location")?.toUIntOrNull()
        val maxPrice = request.query("price")?.toDoubleOrNull() ?: Double.MAX_VALUE

        val houses =
            houseService
                .getAllHouses(
                    skip,
                    limit,
                    minArea,
                    location,
                    PricePerNight(maxPrice),
                ).map { it.toResponse() }

        return Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(houses))
    }
/*
    fun predictPrice(request: Request): Response =
        try {
            val paramW = request.query("params")?.toIntOrNull() ?: 0
            val paramB = request.query("params")?.toIntOrNull() ?: 0
            val limit = request.query("limit")?.toIntOrNull() ?: Int.MAX_VALUE

            val body = Json.decodeFromString<PricePredictionRequest>(request.bodyString())
            val prediction =
                PricePredictionService().predictRental(
                    areaSqMt = body.areaSqMt,
                    nights = body.nights,
                )
            Response(OK).body(Json.encodeToString(prediction))
        } catch (e: Exception) {
            Response(BAD_REQUEST).body("Invalid request: ${e.message}")
        }

 */

    val routes =
        routes(
            "/houses" bind POST to { req: Request ->
                postHouse(req)
            },
            "/houses/{id}" bind GET to { req: Request ->
                getHouseById(req)
            },
            "/houses" bind GET to { req: Request ->
                getHouses(req)
            },
            /*,
            "/houses/price-prediction" bind POST to { req: Request ->
                predictPrice(req)
            },
             */
        )
}
