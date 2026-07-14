import { API_BASE_URL } from "../config/constants.js"

const ENDPOINT_URL = `${API_BASE_URL}locations`

export function fetchLocationById(id) {
    return fetch(`${ENDPOINT_URL}/${id}`)
        .then(res => res.json())
}

export function fetchLocations(skip, limit) {
    return fetch(`${ENDPOINT_URL}?skip=${skip}&limit=${limit}`)
        .then(res => res.json())
}

export async function createLocation(locationData) {
    delete locationData.id;
    delete locationData.id_loc;

    const sanitizedData = {}
    for (const key in locationData) {
        if (locationData[key] !== undefined && locationData[key] !== null) {
            sanitizedData[key] = String(locationData[key])
        }
    }

    const res = await fetch(ENDPOINT_URL, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("authToken")}`
        },
        body: JSON.stringify(sanitizedData)
    })

    if (!res.ok) {
        const errorText = await res.text()
        console.error("O servidor rejeitou a localização com esta mensagem:", errorText)
        throw new Error(`Failed to create location: ${errorText}`)
    }

    return await res.json()
}