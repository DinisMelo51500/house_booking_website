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
import pt.isel.ls.houses.domain.Id
import pt.isel.ls.houses.domain.Location
import pt.isel.ls.houses.domain.LocationType
import pt.isel.ls.houses.domain.Name
import pt.isel.ls.houses.services.LocationService
import pt.isel.ls.houses.services.UserService

@Serializable
data class LocationResponse(
    val id: Id,
    val name: String,
    val type: LocationType,
    val parentId: Id? = null,
)

private fun Location.toResponse() = LocationResponse(id, name.value, type, parentId)

class LocationWebApi(
    private val locationService: LocationService = LocationService(),
    private val userService: UserService = UserService(),
) {
    fun postLocation(request: Request): Response {
        authenticate(request, userService) ?: return Response(BAD_REQUEST).body("Invalid or missing token")
        return try {
            val body = Json.decodeFromString<Map<String, String>>(request.bodyString())
            val name = body["name"] ?: error("Missing name")
            val type = LocationType.valueOf(body["type"] ?: error("Missing type"))
            val parentId = body["parentId"]?.toInt()?.let { Id(it) }
            val id = locationService.createLocation(Name(name), type, parentId)
            Response(CREATED)
                .header("Content-Type", "application/json")
                .body(
                    Json.encodeToString(
                        LocationResponse(id, name, type, parentId),
                    ),
                )
        } catch (e: Exception) {
            Response(BAD_REQUEST).body(e.message ?: "Unknown error")
        }
    }

    fun getLocationById(request: Request): Response {
        val id = request.path("id")!!.toUInt()
        val location = locationService.getLocationInfo(id)
        return if (location == null) {
            Response(NOT_FOUND).body("Location Not Found")
        } else {
            Response(OK)
                .header("Content-Type", "application/json")
                .body(
                    Json.encodeToString(location.toResponse()),
                )
        }
    }

    fun getChildren(request: Request): Response {
        val id = request.path("id")!!.toUInt()
        val skip = request.query("skip")?.toInt() ?: 0
        val limit = request.query("limit")?.toInt() ?: Int.MAX_VALUE
        val children =
            locationService
                .getChildrenLocations(id, skip, limit)
                .map { it.toResponse() }
        return Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(children))
    }

    fun getPath(request: Request): Response {
        try {
            val id = request.path("id")!!.toUInt()
            val path =
                locationService
                    .getFullLocationPath(id)
                    .map { it.toResponse() }
            return Response(OK)
                .header("Content-Type", "application/json")
                .body(Json.encodeToString(path))
        } catch (e: Exception) {
            return Response(NOT_FOUND).body(e.message ?: "Unknown error")
        }
    }

    fun getLocations(request: Request): Response {
        val skip = request.query("skip")?.toInt() ?: 0
        val limit = request.query("limit")?.toInt() ?: Int.MAX_VALUE
        val locations =
            locationService
                .getAllLocations(skip, limit)
                .map { LocationResponse(it.id, it.name.value, it.type, it.parentId) }

        return Response(OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(locations))
    }

    val routes =
        routes(
            "/locations" bind POST to { req: Request ->
                postLocation(req)
            },
            "/locations" bind GET to { req: Request ->
                getLocations(req)
            },
            "/locations/{id}" bind GET to { req: Request ->
                getLocationById(req)
            },
            "/locations/{id}/children" bind GET to { req: Request ->
                getChildren(req)
            },
            "/locations/{id}/path" bind GET to { req: Request ->
                getPath(req)
            },
        )
}
