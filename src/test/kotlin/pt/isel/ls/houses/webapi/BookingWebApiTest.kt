package pt.isel.ls.houses.webapi

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.routes
import pt.isel.ls.houses.data.mem.MemBookingRepository
import pt.isel.ls.houses.data.mem.MemHouseRepository
import pt.isel.ls.houses.data.mem.MemUserRepository
import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import pt.isel.ls.houses.services.BookingService
import pt.isel.ls.houses.services.UserService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BookingWebApiTest {
    private val bookingRepo = MemBookingRepository()
    private val houseRepo = MemHouseRepository()
    private val userRepo = MemUserRepository()
    private val bookingService = BookingService(bookingRepo, houseRepo)
    private val userService = UserService(userRepo)
    private val app =
        routes(
            BookingWebAPI(bookingService, userService).routes,
            UserWebApi(userService).routes,
        )

    @BeforeTest
    fun setup() {
        bookingRepo.clear()
        houseRepo.clear()
        userRepo.clear()
    }

    // --- POST /Bookings ---

    @Test
    fun `postBooking returns 201 with id and token`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val response =
            app(
                Request(POST, "/bookings")
                    .header("Authorization", "Bearer $jsonToken")
                    .body(
                        """{"startDate":"2000-01-01","endDate":"2000-01-31","userId":"1","houseId":"1"}""",
                    ),
            )

        assertEquals(CREATED, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertNotNull(json["startDate"])
        assertNotNull(json["endDate"])
        assertNotNull(json["userId"])
        assertNotNull(json["houseId"])
    }

    @Test
    fun `postBooking returns 400 when startDate is missing`() {
        val response =
            app(
                Request(POST, "/bookings")
                    .body(
                        """{"endDate":"2000-01-31","userId":"1","houseId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postBooking returns 400 when endDate is missing`() {
        val response =
            app(
                Request(POST, "/bookings")
                    .body(
                        """{"startDate":"2000-01-01","userId":"1","houseId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postBooking returns 400 when userId is missing`() {
        val response =
            app(
                Request(POST, "/bookings")
                    .body(
                        """{"startDate":"2000-01-01","endDate":"2000-01-31","houseId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postBooking returns 400 when houseId is missing`() {
        val response =
            app(
                Request(POST, "/bookings")
                    .body(
                        """{"startDate":"2000-01-01","endDate":"2000-01-31","userId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    // --- PATCH /bookings/{id} ---

    @Test
    fun `updateBooking returns 200 with correct booking`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val created =
            bookingRepo.save(
                Id(1),
                Id(1),
                LocalDate.parse("2000-01-01"),
                LocalDate.parse("2000-01-31"),
            )
        val response =
            app(
                Request(PATCH, "/bookings/${created.id}")
                    .header("Authorization", "Bearer $jsonToken")
                    .body("""{"startDate":"2000-02-01","endDate":"2000-02-28"}"""),
            )

        println(response.bodyString())

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals("1", json["id"]?.jsonPrimitive?.content)
        assertEquals("1", json["userId"]?.jsonPrimitive?.content)
        assertEquals("1", json["houseId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `updateBooking returns 404 when booking doesn't exist`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val response =
            app(
                Request(PATCH, "/bookings/1")
                    .header("Authorization", "Bearer $jsonToken")
                    .body("""{"startDate":"2000-02-01","endDate":"2000-02-28"}"""),
            )
        assertEquals(NOT_FOUND, response.status)
    }

    @Test
    fun `updateBooking returns 400 when startDate is missing`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val created =
            bookingRepo.save(
                Id(1),
                Id(1),
                LocalDate.parse("2000-01-01"),
                LocalDate.parse("2000-01-31"),
            )
        val response =
            app(Request(PATCH, "/bookings/${created.id}?endDate=2000-02-28"))
                .header("Authorization", "Bearer $jsonToken")
        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `updateBooking returns 400 when endDate is missing`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val created =
            bookingRepo.save(
                Id(1),
                Id(1),
                LocalDate.parse("2000-01-01"),
                LocalDate.parse("2000-01-31"),
            )
        val response =
            app(
                Request(PATCH, "/bookings/${created.id}")
                    .header("Authorization", "Bearer $jsonToken")
                    .body("""{"startDate":"2000-02-01"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `updateBooking returns 400 when date interval is invalid`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val created =
            bookingRepo.save(
                Id(1),
                Id(1),
                LocalDate.parse("2000-01-01"),
                LocalDate.parse("2000-01-31"),
            )
        val response =
            app(
                Request(PATCH, "/bookings/${created.id}")
                    .header("Authorization", "Bearer $jsonToken")
                    .body("""{"startDate":"2000-02-31","endDate":"2000-02-28"}"""),
            )
        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `updateBooking returns 400 when house is already booked during date interval`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val created =
            bookingRepo.save(
                Id(1),
                Id(1),
                LocalDate.parse("2000-01-01"),
                LocalDate.parse("2000-01-10"),
            )

        val booking2StartDate = LocalDate(2026, 5, 11)
        val booking2EndDate = LocalDate(2026, 5, 14)

        bookingRepo.save(
            Id(1),
            Id(1),
            booking2StartDate,
            booking2EndDate,
        )
        val response =
            app(
                Request(PATCH, "/bookings/${created.id}")
                    .header("Authorization", "Bearer $jsonToken")
                    .body("""{"startDate":"2026-05-11","endDate":"2026-05-14"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    // --- GET /bookings/{id} ---

    @Test
    fun `getBookingById returns 200 with correct booking`() {
        val created =
            bookingRepo.save(
                Id(1),
                Id(1),
                LocalDate.parse("2000-01-01"),
                LocalDate.parse("2000-01-31"),
            )
        val response = app(Request(GET, "/bookings/${created.id}"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals("1", json["id"]?.jsonPrimitive?.content)
        assertEquals("1", json["userId"]?.jsonPrimitive?.content)
        assertEquals("1", json["houseId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `getBookingById returns 404 when booking does not exist`() {
        val response = app(Request(GET, "/bookings/999"))

        assertEquals(NOT_FOUND, response.status)
    }

    // --- DELETE /bookings/{id} ---

    @Test
    fun `deleteBooking returns 200 when booking is deleted`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val created =
            bookingRepo.save(
                Id(1),
                Id(1),
                LocalDate.parse("2000-01-01"),
                LocalDate.parse("2000-01-31"),
            )
        val response =
            app(
                Request(DELETE, "/bookings/${created.id}")
                    .header("Authorization", "Bearer $jsonToken"),
            )

        assertEquals(OK, response.status)
    }

    @Test
    fun `deleteBooking returns 404 when booking doesn't exist`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val response =
            app(
                Request(DELETE, "/bookings/1")
                    .header("Authorization", "Bearer $jsonToken"),
            )

        assertEquals(NOT_FOUND, response.status)
    }

    @Test
    fun `deleteBooking returns 400 when id is invalid`() {
        val token =
            app(
                Request(POST, "/users")
                    .body("""{"name":"Alice","email":"alice@example.com", "password":"password"}"""),
            )

        val jsonToken =
            Json
                .parseToJsonElement(token.bodyString())
                .jsonObject["token"]
                .toString()
                .trim('"')

        val response =
            app(
                Request(DELETE, "/bookings/a")
                    .header("Authorization", "Bearer $jsonToken"),
            )

        assertEquals(BAD_REQUEST, response.status)
    }
    // --- GET /bookings/{houseId}/dates ---

    @Test
    fun `getBookingByHouseId returns 200 with correct bookings`() {
        val houseId = Id(1)
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            Id(2),
            Id(1),
            LocalDate.parse("1999-12-31"),
            LocalDate.parse("2000-02-01"),
        )
        bookingRepo.save(
            Id(3),
            Id(1),
            LocalDate.parse("2000-02-01"),
            LocalDate.parse("2000-02-02"),
        )

        val response = app(Request(GET, "/bookings/$houseId/dates?startDate=2000-01-01&endDate=2000-01-31"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }

    @Test
    fun `getBookingByHouseId returns 404 when no booking exists`() {
        val houseId = Id(1)

        val response = app(Request(GET, "/bookings/$houseId/dates?startDate=2000-01-01&endDate=2000-01-31"))

        assertEquals(NOT_FOUND, response.status)
    }

    @Test
    fun `getBookingByHouseId returns 404 no booking exists in the given period`() {
        val houseId = Id(1)
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            Id(2),
            Id(1),
            LocalDate.parse("1999-12-31"),
            LocalDate.parse("2000-02-01"),
        )
        bookingRepo.save(
            Id(3),
            Id(1),
            LocalDate.parse("2000-02-01"),
            LocalDate.parse("2000-02-02"),
        )

        val response = app(Request(GET, "/bookings/$houseId/dates?startDate=2004-01-01&endDate=2004-01-31"))

        assertEquals(NOT_FOUND, response.status)
    }

    @Test
    fun `getBookingByHouseId respects skip param`() {
        val houseId = Id(1)
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            Id(2),
            Id(1),
            LocalDate.parse("1999-12-31"),
            LocalDate.parse("2000-02-01"),
        )
        bookingRepo.save(
            Id(3),
            Id(1),
            LocalDate.parse("2000-02-01"),
            LocalDate.parse("2000-02-02"),
        )

        val response = app(Request(GET, "/bookings/$houseId/dates?startDate=2000-01-01&endDate=2000-01-31&skip=1"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `getBookingByHouseId respects limit param`() {
        val houseId = Id(1)
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            Id(2),
            Id(1),
            LocalDate.parse("1999-12-31"),
            LocalDate.parse("2000-02-01"),
        )
        bookingRepo.save(
            Id(3),
            Id(1),
            LocalDate.parse("2000-02-01"),
            LocalDate.parse("2000-02-02"),
        )

        val response = app(Request(GET, "/bookings/$houseId/dates?startDate=2000-01-01&endDate=2000-01-31&limit=1"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    // --- GET /bookings/available ---

    @Test
    fun `getAvailableBookings returns 200 with correct bookings`() {
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        houseRepo.save(
            Name("Apartamento"),
            Id(1),
            AreaSqMt(100),
            PricePerNight(100.0),
            Description("T2"),
            ownerId = Id(1),
        )
        houseRepo.save(
            Name("Casa"),
            Id(2),
            AreaSqMt(350),
            PricePerNight(250.0),
            Description("T5"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/bookings/available?startDate=2000-01-01&endDate=2000-01-31"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `getAvailableBookings returns 400 when startDate is missing`() {
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        houseRepo.save(
            Name("Apartamento"),
            Id(1),
            AreaSqMt(100),
            PricePerNight(100.0),
            Description("T2"),
            ownerId = Id(1),
        )
        houseRepo.save(
            Name("Casa"),
            Id(2),
            AreaSqMt(350),
            PricePerNight(250.0),
            Description("T5"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/bookings/available?endDate=2000-01-31"))

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `getAvailableBookings returns 400 when endDate is missing`() {
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        houseRepo.save(
            Name("Apartamento"),
            Id(1),
            AreaSqMt(100),
            PricePerNight(100.0),
            Description("T2"),
            ownerId = Id(1),
        )
        houseRepo.save(
            Name("Casa"),
            Id(2),
            AreaSqMt(350),
            PricePerNight(250.0),
            Description("T5"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/bookings/available?startDate=2000-01-01"))

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `getAvailableBookings respects skip param`() {
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        houseRepo.save(
            Name("Apartamento"),
            Id(1),
            AreaSqMt(100),
            PricePerNight(100.0),
            Description("T2"),
            ownerId = Id(1),
        )
        houseRepo.save(
            Name("Casa"),
            Id(2),
            AreaSqMt(350),
            PricePerNight(250.0),
            Description("T5"),
            ownerId = Id(2),
        )
        houseRepo.save(
            Name("Casa2"),
            Id(2),
            AreaSqMt(350),
            PricePerNight(250.0),
            Description("T5"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/bookings/available?startDate=2000-01-01&endDate=2000-01-31&skip=1"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `getAvailableBookings respects limit param`() {
        bookingRepo.save(
            Id(1),
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        houseRepo.save(
            Name("Apartamento"),
            Id(1),
            AreaSqMt(100),
            PricePerNight(100.0),
            Description("T2"),
            ownerId = Id(1),
        )
        houseRepo.save(
            Name("Casa"),
            Id(2),
            AreaSqMt(350),
            PricePerNight(250.0),
            Description("T5"),
            ownerId = Id(2),
        )
        houseRepo.save(
            Name("Casa2"),
            Id(2),
            AreaSqMt(350),
            PricePerNight(250.0),
            Description("T5"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/bookings/available?startDate=2000-01-01&endDate=2000-01-31&limit=1"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    // --- GET /bookings/users/{userId} ---

    @Test
    fun `getBookingByUser returns 200 with correct bookings`() {
        val userId = Id(1)
        bookingRepo.save(
            userId,
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            userId,
            Id(2),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            userId,
            Id(3),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        val response = app(Request(GET, "/bookings/users/$userId"))

        assertEquals(OK, response.status)
        assertEquals(3, Json.parseToJsonElement(response.bodyString()).jsonArray.size)
    }

    @Test
    fun `getBookingByUser returns 404 when there are no bookings`() {
        val response = app(Request(GET, "/bookings/users/1"))

        assertEquals(NOT_FOUND, response.status)
    }

    @Test
    fun `getBookingByUser returns 400 when userId is invalid`() {
        val response = app(Request(GET, "/bookings/users/a"))

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `getBookingByUser respects skip param`() {
        val userId = Id(1)
        bookingRepo.save(
            userId,
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            userId,
            Id(2),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            userId,
            Id(3),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        val response = app(Request(GET, "/bookings/users/$userId?skip=1"))

        assertEquals(OK, response.status)
        assertEquals(2, Json.parseToJsonElement(response.bodyString()).jsonArray.size)
    }

    @Test
    fun `getBookingByUser respects limit param`() {
        val userId = Id(1)
        bookingRepo.save(
            userId,
            Id(1),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            userId,
            Id(2),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )
        bookingRepo.save(
            userId,
            Id(3),
            LocalDate.parse("2000-01-01"),
            LocalDate.parse("2000-01-31"),
        )

        val response = app(Request(GET, "/bookings/users/$userId?limit=1"))

        assertEquals(OK, response.status)
        assertEquals(1, Json.parseToJsonElement(response.bodyString()).jsonArray.size)
    }
}
