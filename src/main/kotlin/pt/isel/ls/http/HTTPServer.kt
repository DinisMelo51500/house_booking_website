package pt.isel.ls.http

import org.http4k.routing.ResourceLoader
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import pt.isel.ls.houses.webapi.BookingWebAPI
import pt.isel.ls.houses.webapi.HouseWebApi
import pt.isel.ls.houses.webapi.LocationWebApi
import pt.isel.ls.houses.webapi.UserWebApi

private val logger = LoggerFactory.getLogger("pt.isel.ls.houses.server")

fun main() {
    val userApi = UserWebApi()
    val locationApi = LocationWebApi()
    val houseApi = HouseWebApi()
    val bookingApi = BookingWebAPI()

    val app =
        routes(
            userApi.routes,
            locationApi.routes,
            houseApi.routes,
            bookingApi.routes,
            singlePageApp(ResourceLoader.Directory("static-content")),
        )

    val server = app.asServer(Jetty(9000)).start()

    logger.info("Server started at http://localhost:9000")

    readln()

    server.stop()
    logger.info("Server stopped")
}
