import { fetchLocations } from "../api/locationsApi.js"
import { homeView } from "../views/HomeViews.js"

export async function getHome(mainContent) {
    const locations = await fetchLocations(0, 100)

    homeView(mainContent, locations, ({ area, price, location }) => {
        const params = new URLSearchParams()
        if (area) params.set("area", area)
        if (price) params.set("price", price)
        if (location) params.set("location", location)

        window.location.hash = `#houses?${params.toString()}`
    })
}