import { fetchHouses, fetchHouseById, createHouse } from "../api/housesApi.js"
import { fetchLocations, createLocation } from "../api/locationsApi.js"
import { DEFAULT_SKIP, PAGE_SIZE } from "../config/constants.js"
import { housesView } from "../views/HouseViews.js"
import { houseDetailsView, houseAvailabilityMonthView, houseAvailabilityView } from "../views/HouseDetailsViews.js"
import { showToast } from "../helpers/helpers.js"
import { createHouseView } from "../views/CreateHouseViews.js"
import { createLocationView } from "../views/CreateLocationViews.js"

export async function getHouses(mainContent, params, query) {
    const skip = query?.skip ? parseInt(query.skip) : DEFAULT_SKIP
    const limit = query?.limit ? parseInt(query.limit) : PAGE_SIZE
    const area = query?.area ? parseInt(query.area) : null
    const price = query?.price ? parseInt(query.price) : null
    const location = query?.location ? parseInt(query.location) : null

    const houses = await fetchHouses({ skip, limit: limit + 1, area, price, location })

    const hasNext = houses.length > limit
    if (hasNext) houses.pop()

    housesView(houses, mainContent, { skip, limit, area, price, location }, hasNext)
}

export async function getHouseDetails(mainContent, params) {
    const id = params?.id ? parseInt(params.id) : null

    try {
        const house = await fetchHouseById(id)
        houseDetailsView(house, mainContent)
    } catch (err) {
        showToast(err.message)
    }
}

export async function getHouseAvailability (mainContent, params) {
    const id = params?.id ? parseInt(params.id) : null
    houseAvailabilityMonthView(id, mainContent)
}

export async function getHouseAvailabilityDays (mainContent, params, query) {
    const id = params?.id ? parseInt(params.id) : null
    houseAvailabilityView(id, mainContent, query)
}


export async function getCreateHouse(mainContent) {
    try {
        const allLocations = await fetchLocations(0, 1000)
        const rootCountries = allLocations.filter(loc => !loc.parentId)

        const handleLoadSubLocations = async (parentId) => {
            try {
                const locations = await fetchLocations(0, 1000)
                return locations.filter(loc => loc.parentId === parentId)
            } catch (err) {
                return []
            }
        }

        createHouseView(
            mainContent,
            rootCountries,
            handleLoadSubLocations,
            async (houseData) => {
                const result = await createHouse(houseData)
                if (result.success) {
                    showToast("House created successfully")
                    window.location.hash = "#houses"
                } else {
                    showToast(result.data || "Error creating house")
                }
            }
        )
    } catch (error) {
        showToast("Failed to initialize create house form.")
    }
}

export async function getCreateLocation(mainContent) {
    try {
        const allLocations = await fetchLocations(0, 1000)
        const rootCountries = allLocations.filter(loc => !loc.parentId)

        const handleLoadSubLocations = async (parentId) => {
            try {
                const locations = await fetchLocations(0, 1000)
                return locations.filter(loc => loc.parentId === parentId)
            } catch (err) {
                return []
            }
        }

        createLocationView(
            mainContent,
            rootCountries,
            handleLoadSubLocations,
            async (locationPayload) => {
                try {
                    await createLocation(locationPayload)
                    showToast("New location database registry created!")
                    window.location.hash = "#create-house"
                } catch (err) {
                    showToast("Error executing location creation API request")
                }
            }
        )
    } catch (error) {
        showToast("Failed to initialize location creation form.")
    }
}