package pt.isel.ls.houses.webapi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.routes
import pt.isel.ls.houses.data.mem.MemHouseRepository
import pt.isel.ls.houses.data.mem.MemUserRepository
import pt.isel.ls.houses.domain.AreaSqMt
import pt.isel.ls.houses.domain.Description
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.domain.PricePerNight
import pt.isel.ls.houses.services.HouseService
import pt.isel.ls.houses.services.UserService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class HouseWebApiTest {
    private val houseRepo = MemHouseRepository()
    private val userRepo = MemUserRepository()
    private val houseService = HouseService(houseRepo)
    private val userService = UserService(userRepo)
    private val app =
        routes(
            HouseWebApi(houseService, userService).routes,
            UserWebApi(userService).routes,
        )

    // --- POST /Houses ---

    @Test
    fun `postHouse returns 201 with id and token`() {
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
                Request(POST, "/houses")
                    .header("Authorization", "Bearer $jsonToken")
                    .body(
                        """{"title":"Apartamento",
                            |"location":"1",
                            |"areaSqMt":"100",
                            |"pricePerNight":"100",
                            |"description":"T2",
                            |"ownerId":"1"}
                        """.trimMargin(),
                    ),
            )

        assertEquals(CREATED, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertNotNull(json["title"])
        assertNotNull(json["location"])
        assertNotNull(json["areaSqMt"])
        assertNotNull(json["pricePerNight"])
        assertNotNull(json["description"])
        assertNotNull(json["ownerId"])
    }

    @Test
    fun `postHouse returns 400 when title is missing`() {
        val response =
            app(
                Request(POST, "/houses")
                    .body(
                        """{"location":"1","areaSqMt":"100","pricePerNight":"100","description":"T2","ownerId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postUser returns 500 when location is missing`() {
        val response =
            app(
                Request(POST, "/houses")
                    .body(
                        """{"title":"Apartamento","areaSqMt":"100","pricePerNight":"100","description":"T2","ownerId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postHouse returns 400 when areaSqMt is missing`() {
        val response =
            app(
                Request(POST, "/houses")
                    .body(
                        """{"title":"Apartamento","location":"1","pricePerNight":"100","description":"T2","ownerId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postHouse returns 400 when pricePerNight is missing`() {
        val response =
            app(
                Request(POST, "/houses")
                    .body(
                        """{"title":"Apartamento","location":"1","areaSqMt":"100","description":"T2","ownerId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postHouse returns 400 when description is missing`() {
        val response =
            app(
                Request(POST, "/houses")
                    .body(
                        """{"title":"Apartamento","location":"1","areaSqMt":"100","pricePerNight":"100","ownerId":"1"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postHouse returns 400 when ownerId is missing`() {
        val response =
            app(
                Request(POST, "/houses")
                    .body(
                        """{"title":"Apartamento","location":"1","areaSqMt":"100","pricePerNight":"100","description":"T2"}""",
                    ),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    // --- GET /users/{id} ---

    @Test
    fun `getHouseById returns 200 with correct user`() {
        val created =
            houseRepo.save(
                Name("Apartamento"),
                Id(1),
                AreaSqMt(100),
                PricePerNight(100.0),
                Description("T2"),
                ownerId = Id(1),
            )

        val response = app(Request(GET, "/houses/${created.id}"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals("Apartamento", json["title"]?.jsonPrimitive?.content)
        assertEquals("1", json["location"]?.jsonPrimitive?.content)
        assertEquals("100", json["areaSqMt"]?.jsonPrimitive?.content)
        assertEquals("100.0", json["pricePerNight"]?.jsonPrimitive?.content)
        assertEquals("T2", json["description"]?.jsonPrimitive?.content)
        assertEquals("1", json["ownerId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `getHouseById returns 404 when user does not exist`() {
        val response = app(Request(GET, "/houses/999"))

        assertEquals(NOT_FOUND, response.status)
    }

    // --- GET /houses ---

    @Test
    fun `getHouses returns all users`() {
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

        val response = app(Request(GET, "/houses"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }

    @Test
    fun `getHouses returns empty list when no users exist`() {
        val response = app(Request(GET, "/houses"))

        assertEquals(OK, response.status)
        assertEquals("[]", response.bodyString())
    }

    @Test
    fun `getHouses respects skip param`() {
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

        val response = app(Request(GET, "/houses?skip=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `getHouses respects limit param`() {
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

        val response = app(Request(GET, "/houses?limit=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `getHouses respects minimum area param`() {
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
            Name("Moradia"),
            Id(2),
            AreaSqMt(200),
            PricePerNight(250.0),
            Description("T4"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/houses?area=200"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }

    @Test
    fun `getHouses respects maximum price param`() {
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
            Name("Moradia"),
            Id(2),
            AreaSqMt(200),
            PricePerNight(150.0),
            Description("T4"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/houses?price=150"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }

    @Test
    fun `getHouses respects location param`() {
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
            Name("Moradia"),
            Id(2),
            AreaSqMt(200),
            PricePerNight(150.0),
            Description("T4"),
            ownerId = Id(2),
        )

        val response = app(Request(GET, "/houses?location=2"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }
}
