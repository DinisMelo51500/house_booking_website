import router from "./router/router.js"

import { getHome } from "./handlers/HomeHandlers.js"
import { getHouses, getHouseDetails, getHouseAvailability, getHouseAvailabilityDays } from "./handlers/HouseHandlers.js"
import {getUserDetails, getSignUp, getLogin, logOut} from "./handlers/UserHandlers.js"
import {  getBookingDetails, getBookingsByHouse, getBookingsByUser, handleDeleteBooking, postEditBooking, postCreateBooking, getCreateBooking, getEditBooking } from "./handlers/BookingHandlers.js"
import { getLocationDetails } from "./handlers/LocationHandlers.js"
import { getCreateHouse, getCreateLocation } from "./handlers/HouseHandlers.js"

window.addEventListener("load", loadHandler)
window.addEventListener("hashchange", hashChangeHandler)

function loadHandler() {

    // Routes
   router.addRouteHandler("houses/:id/availability/days", getHouseAvailabilityDays)
   router.addRouteHandler("houses/:id/availability", getHouseAvailability)
   router.addRouteHandler("houses/:id", getHouseDetails)
   router.addRouteHandler("houses", getHouses)
   router.addRouteHandler("users", getSignUp)
   router.addRouteHandler("users/login", getLogin)
   router.addRouteHandler("users/:id", getUserDetails)
   router.addRouteHandler("logout", logOut)
   router.addRouteHandler("bookings/create", getCreateBooking)
   router.addRouteHandler("bookings/create/confirm", postCreateBooking)
   router.addRouteHandler("bookings/:id/edit", getEditBooking)
   router.addRouteHandler("bookings/:id/edit/confirm", postEditBooking)
   router.addRouteHandler("bookings/:id/delete", handleDeleteBooking)
   router.addRouteHandler("bookings/:houseId/dates", getBookingsByHouse)
   router.addRouteHandler("bookings/users/:userId", getBookingsByUser)
   router.addRouteHandler("bookings/:id", getBookingDetails)
   router.addRouteHandler("locations/:id", getLocationDetails)
   router.addRouteHandler("home", getHome)
   router.addDefaultNotFoundRouteHandler(() => {
       window.location.hash = "#home"
   })
   router.addRouteHandler("create-house", getCreateHouse)
   router.addRouteHandler("create-location", getCreateLocation)

    //força sempre home no arranque
    if (!window.location.hash) {
        window.location.hash = "#home"
    } else {
        hashChangeHandler()
    }
}

function hashChangeHandler() {
    // Obtém o elemento onde será renderizado o conteúdo da página
    const mainContent = document.getElementById("mainContent")
    // Obtém o hash atual do URL e remove o caracter '#'
    const path = window.location.hash.replace("#", "")
    // Pede ao router o handler associado à rota atual
    const handler = router.getRouteHandler(path)
    // Se não existir nenhum handler para a rota, redireciona o utilizador para a página Home
    if (!handler) {
        window.location.hash = "#home"
        return
    }
    // Executa o handler encontrado e passa-lhe o elemento mainContent para que este possa renderizar a página
    handler(mainContent)
}