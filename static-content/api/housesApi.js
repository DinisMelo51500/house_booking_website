import { API_BASE_URL } from "../config/constants.js"

const ENDPOINT_URL = `${API_BASE_URL}houses`

export async function fetchHouses({ skip, limit, area, price, location }) {
    const res = await fetch(
        `${ENDPOINT_URL}?skip=${skip}&limit=${limit}&area=${area}&price=${price}&location=${location}`
    )

    if (!res.ok) {
        throw new Error("Failed to fetch houses")
    }

    return await res.json()
}

export async function fetchHouseById(id) {
    const res = await fetch(`${ENDPOINT_URL}/${id}`)

    if (!res.ok) {
        throw new Error("House not found")
    }

    return await res.json()
}

export async function createHouse(houseData) {
    const sanitizedData = {}

    for (const key in houseData) {
        if (houseData[key] !== undefined && houseData[key] !== null) {
            sanitizedData[key] = String(houseData[key])
        }
    }

    const response = await fetch(ENDPOINT_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("authToken")}`
        },
        body: JSON.stringify(sanitizedData)
    })

    if (!response.ok) {
        const errorText = await response.text()
        console.error("O servidor rejeitou com esta mensagem:", errorText)
        return { success: false, data: errorText }
    }

    const data = await response.json()
    return { success: true, data: data }
}