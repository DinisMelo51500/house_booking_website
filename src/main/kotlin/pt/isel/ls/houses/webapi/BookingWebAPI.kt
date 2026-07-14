package pt.isel.ls.houses.webapi

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
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
import pt.isel.ls.houses.domain.Booking
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.services.BookingService
import pt.isel.ls.houses.services.UserService

@Serializable
data class BookingResponse(
    val id: Id,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val userId: Id,
    val houseId: Id,
)

@Serializable
data class AvailableDaysResponse(
    val houseId: Id,
    val year: Int,
    val month: Int,
    val availableDays: List<Int>,
)

private fun Booking.toResponse() = BookingResponse(id, startDate, endDate, userId, houseId)

class BookingWebAPI(
    private val bookingService: BookingService = BookingService(),
    private val userService: UserService = UserService(),
) {
    fun postBooking(request: Request): Response =
        try {
            val userId =
                authenticate(request, userService)?.id
                    ?: return Response(BAD_REQUEST).body("Invalid or missing token")

            val body =
                Json.decodeFromString<Map<String, String>>(request.bodyString())

            val startDate =
                LocalDate.parse(body["startDate"] ?: error("Missing startDate"))

            val endDate =
                LocalDate.parse(body["endDate"] ?: error("Missing endDate"))

            val houseId =
                Id(body["houseId"]?.toInt() ?: error("Missing houseId"))

            val booking =
                bookingService.createBooking(
                    userId,
                    houseId,
                    startDate,
                    endDate,
                )

            Response(CREATED)
                .header("Content-Type", "application/json")
                .body(Json.encodeToString(booking.toResponse()))
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }

    fun getBookingById(request: Request): Response {
        val id =
            request.path("id")?.toUIntOrNull()
                ?: return Response(BAD_REQUEST).body("Invalid id")

        val booking =
            bookingService.getBookingInfo(id)
                ?: return Response(NOT_FOUND).body("Booking Not Found")

        return Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(booking.toResponse()))
    }

    fun getBookingByHouseIdAndDate(request: Request): Response {
        val houseId =
            request.path("houseId")?.toUIntOrNull()
                ?: return Response(BAD_REQUEST).body("Invalid houseId")

        val startDate =
            request.query("startDate")?.let { LocalDate.parse(it) }
                ?: return Response(BAD_REQUEST).body("Missing startDate")

        val endDate =
            request.query("endDate")?.let { LocalDate.parse(it) }
                ?: return Response(BAD_REQUEST).body("Missing endDate")

        val skip = request.query("skip")?.toInt() ?: 0
        val limit = request.query("limit")?.toInt() ?: Int.MAX_VALUE

        val bookings =
            bookingService
                .getBookingsByHouseAndDate(
                    houseId,
                    startDate,
                    endDate,
                    skip,
                    limit,
                ).map { it.toResponse() }

        if (bookings.isEmpty()) {
            return Response(NOT_FOUND).body("Booking Not Found")
        }

        return Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(bookings))
    }

    fun getAvailableBookings(request: Request): Response =
        try {
            val startDate =
                LocalDate.parse(request.query("startDate") ?: error("Missing startDate"))

            val endDate =
                LocalDate.parse(request.query("endDate") ?: error("Missing endDate"))

            val skip = request.query("skip")?.toInt() ?: 0
            val limit = request.query("limit")?.toInt() ?: Int.MAX_VALUE

            val houses =
                bookingService
                    .getAvailableHouses(startDate, endDate, skip, limit)
                    .map {
                        HouseResponse(
                            it.id,
                            it.title.value,
                            it.location,
                            it.areaSqMt,
                            it.pricePerNight.value,
                            it.description.value,
                            it.ownerId,
                        )
                    }

            if (houses.isEmpty()) {
                Response(NOT_FOUND).body("No available houses")
            } else {
                Response(OK)
                    .header("Content-Type", "application/json")
                    .body(Json.encodeToString(houses))
            }
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }

    fun getAvailableDays(request: Request): Response =
        try {
            val houseId =
                request.path("houseId")?.toUIntOrNull()
                    ?: return Response(BAD_REQUEST).body("Invalid houseId")

            val year =
                request.query("year")?.toIntOrNull()
                    ?: return Response(BAD_REQUEST).body("Missing year")

            val month =
                request.query("month")?.toIntOrNull()
                    ?: return Response(BAD_REQUEST).body("Missing month")

            val availableDays =
                bookingService.getAvailableDays(
                    houseId,
                    year,
                    month,
                )

            Response(OK)
                .header("Content-Type", "application/json")
                .body(
                    Json.encodeToString(
                        AvailableDaysResponse(
                            houseId,
                            year,
                            month,
                            availableDays,
                        ),
                    ),
                )
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }

    fun updateBooking(request: Request): Response {
        authenticate(request, userService)
            ?: return Response(BAD_REQUEST).body("Invalid or missing token")

        val id =
            request.path("id")?.toUIntOrNull()
                ?: return Response(BAD_REQUEST).body("Invalid id")

        return try {
            val body =
                Json.decodeFromString<Map<String, String>>(request.bodyString())

            val startDate =
                LocalDate.parse(body["startDate"] ?: error("Missing startDate"))

            val endDate =
                LocalDate.parse(body["endDate"] ?: error("Missing endDate"))

            val updated =
                bookingService.updateBooking(id, startDate, endDate)
                    ?: return Response(NOT_FOUND).body("Booking Not Found")

            Response(OK)
                .header("Content-Type", "application/json")
                .body(Json.encodeToString(updated.toResponse()))
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }
    }

    fun deleteBooking(request: Request): Response {
        authenticate(request, userService)
            ?: return Response(BAD_REQUEST).body("Invalid or missing token")

        val id =
            request.path("id")?.toUIntOrNull()
                ?: return Response(BAD_REQUEST).body("Invalid id")

        return if (bookingService.deleteBooking(id)) {
            Response(OK).body("Booking Deleted")
        } else {
            Response(NOT_FOUND).body("Booking Not Found")
        }
    }

    fun getBookingsByUser(request: Request): Response {
        val userId =
            request.path("userId")?.toUIntOrNull()
                ?: return Response(BAD_REQUEST).body("Invalid userId")

        val skip = request.query("skip")?.toInt() ?: 0
        val limit = request.query("limit")?.toInt() ?: Int.MAX_VALUE

        val bookings =
            bookingService
                .getBookingsByUser(userId, skip, limit)
                .map { it.toResponse() }

        if (bookings.isEmpty()) {
            return Response(NOT_FOUND).body("Bookings Not Found")
        }

        return Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(bookings))
    }

    val routes =
        routes(
            "/bookings" bind POST to { postBooking(it) },
            "/bookings/available" bind GET to { getAvailableBookings(it) },
            "/bookings/{id}" bind GET to { getBookingById(it) },
            "/bookings/{id}" bind PATCH to { updateBooking(it) },
            "/bookings/{id}" bind DELETE to { deleteBooking(it) },
            "/bookings/{houseId}/dates" bind GET to { getBookingByHouseIdAndDate(it) },
            "/bookings/{houseId}/availability" bind GET to { getAvailableDays(it) },
            "/bookings/users/{userId}" bind GET to { getBookingsByUser(it) },
        )
}
