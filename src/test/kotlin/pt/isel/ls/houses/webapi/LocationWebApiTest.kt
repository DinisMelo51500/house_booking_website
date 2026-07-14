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
import pt.isel.ls.houses.data.mem.MemLocationRepository
import pt.isel.ls.houses.data.mem.MemUserRepository
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.services.LocationService
import pt.isel.ls.houses.services.UserService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LocationWebApiTest {
    private val locationRepo = MemLocationRepository()
    private val userRepo = MemUserRepository()
    private val locationService = LocationService(locationRepo)
    private val userService = UserService(userRepo)
    private val app =
        routes(
            LocationWebApi(locationService, userService).routes,
            UserWebApi(userService).routes,
        )

    // --- POST /locations ---

    @Test
    fun `postLocation returns 201 with id when type = COUNTRY and null parentId`() {
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
                Request(POST, "/locations")
                    .header("Authorization", "Bearer $jsonToken")
                    .body("""{"name":"Portugal","type":"COUNTRY"}"""),
            )

        assertEquals(CREATED, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertNotNull(json["id"])
    }

    @Test
    fun `postLocation returns 400 when name is missing`() {
        val response =
            app(
                Request(POST, "/locations")
                    .body("""{"type":"DISTRICT","parentId":"1"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postLocation returns 400 when type is missing`() {
        val response =
            app(
                Request(POST, "/locations")
                    .body("""{"name":"Lisboa","parentId":"1"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postLocation returns 400 when type = Country and !null parentId`() {
        val response =
            app(
                Request(POST, "/locations")
                    .body("""{"name":"Portugal","type":"COUNTRY","parentId":"1"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    @Test
    fun `postLocation returns 400 when type != COUNTRY and null parentId`() {
        val response =
            app(
                Request(POST, "/locations")
                    .body("""{"name":"Lisboa","type":"DISTRICT"}"""),
            )

        assertEquals(BAD_REQUEST, response.status)
    }

    // --- GET /locations/{id} ---

    @Test
    fun `getLocationById returns 200 with correct location`() {
        val created = locationRepo.save(Name("Portugal"), LocationType.COUNTRY)

        val response = app(Request(GET, "/locations/${created.id}"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals("Portugal", json["name"]?.jsonPrimitive?.content)
        assertEquals("COUNTRY", json["type"]?.jsonPrimitive?.content)
        assertEquals(null, json["parentId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `getLocationById returns 404 when location does not exist`() {
        val response = app(Request(GET, "/locations/999"))

        assertEquals(NOT_FOUND, response.status)
    }

    // --- GET /locations ---

    @Test
    fun `getLocations returns all locations`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, parentId = Id(1))

        val response = app(Request(GET, "/locations"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }

    @Test
    fun `getLocations returns empty list when no locations exist`() {
        val response = app(Request(GET, "/locations"))

        assertEquals(OK, response.status)
        assertEquals("[]", response.bodyString())
    }

    @Test
    fun `getLocations respects skip param`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, parentId = Id(1))

        val response = app(Request(GET, "/locations?skip=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    @Test
    fun `getLocations respects limit param`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, parentId = Id(1))

        val response = app(Request(GET, "/locations?limit=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    // --- GET /locations/{id}/children ---

    @Test
    fun `getChildren returns all children locations`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, parentId = Id(1))
        locationRepo.save(Name("Leiria"), LocationType.DISTRICT, parentId = Id(1))
        locationRepo.save(Name("Setúbal"), LocationType.DISTRICT, parentId = Id(1))

        val response = app(Request(GET, "/locations/1/children"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(3, json.size)
    }

    @Test
    fun `getChildren returns empty list when no children exist`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        val response = app(Request(GET, "/locations/1/children"))

        assertEquals(OK, response.status)
        assertEquals("[]", response.bodyString())
    }

    @Test
    fun `getChildren respects skip param`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, parentId = Id(1))
        locationRepo.save(Name("Leiria"), LocationType.DISTRICT, parentId = Id(1))
        locationRepo.save(Name("Setúbal"), LocationType.DISTRICT, parentId = Id(1))

        val response = app(Request(GET, "/locations/1/children?skip=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(2, json.size)
    }

    @Test
    fun `getChildren respects limit param`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, parentId = Id(1))
        locationRepo.save(Name("Leiria"), LocationType.DISTRICT, parentId = Id(1))
        locationRepo.save(Name("Setúbal"), LocationType.DISTRICT, parentId = Id(1))

        val response = app(Request(GET, "/locations/1/children?limit=1"))

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(1, json.size)
    }

    // --- GET /locations/{id}/path ---

    @Test
    fun `getPath returns 200 with full path`() {
        locationRepo.save(Name("Portugal"), LocationType.COUNTRY)
        locationRepo.save(Name("Lisboa"), LocationType.DISTRICT, parentId = Id(1))
        locationRepo.save(Name("Loures"), LocationType.MUNICIPALITY, parentId = Id(2))
        locationRepo.save(Name("Prior Velho"), LocationType.LOCALITY, parentId = Id(3))

        val response = app(Request(GET, "/locations/4/path"))

        assertEquals(OK, response.status)

        val json = Json.parseToJsonElement(response.bodyString()).jsonArray
        assertEquals(4, json.size)
        assertEquals(null, json[0].jsonObject["parentId"]?.jsonPrimitive?.content)
        assertEquals("1", json[1].jsonObject["parentId"]?.jsonPrimitive?.content)
        assertEquals("2", json[2].jsonObject["parentId"]?.jsonPrimitive?.content)
        assertEquals("3", json[3].jsonObject["parentId"]?.jsonPrimitive?.content)
    }
}
