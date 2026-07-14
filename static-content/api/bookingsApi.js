import { API_BASE_URL } from "../config/constants.js"

export async function fetchBookingsByHouse({ houseId, startDate, endDate, skip, limit }) {
    const query = new URLSearchParams({ startDate, endDate }).toString()
    return fetch(`${API_BASE_URL}bookings/${houseId}/dates?${query}&skip=${skip}&limit=${limit+1}`)
        .then(res => {
            if (res.status === 404) return []
            if (!res.ok) throw new Error("Failed to fetch bookings")
            return res.json()
        })
}

export async function fetchBookingsByUser({ userId, skip, limit }) {
    return fetch(`${API_BASE_URL}bookings/users/${userId}?skip=${skip}&limit=${limit+1}`)
        .then(res => {
            if (res.status === 404) return []
            if (!res.ok) throw new Error("Failed to fetch bookings")
            return res.json()
        })
}

export async function fetchBookingById(id) {
    return fetch(`${API_BASE_URL}bookings/${id}`)
        .then(res => {
            if (!res.ok) throw new Error("Booking not found")
            return res.json()
        })
}

export async function createBooking({ houseId, startDate, endDate }) {
    return fetch(`${API_BASE_URL}bookings`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("authToken")}`,
        },
        body: JSON.stringify({ houseId, startDate, endDate }),
    }).then(res => {
        if (!res.ok) throw new Error("Failed to create booking")
        return res.json()
    })
}

export async function updateBooking(id, { startDate, endDate }) {
    return fetch(`${API_BASE_URL}bookings/${id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("authToken")}`,
        },
        body: JSON.stringify({ startDate, endDate }),
    }).then(res => {
        if (!res.ok) throw new Error("Failed to update booking")
        return res.json()
    })
}

export async function deleteBooking(id) {
    return fetch(`${API_BASE_URL}bookings/${id}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${localStorage.getItem("authToken")}`,
        },
    }).then(res => {
        if (!res.ok) throw new Error("Failed to delete booking")
    })
}