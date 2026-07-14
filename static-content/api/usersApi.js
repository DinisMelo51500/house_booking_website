import { API_BASE_URL } from "../config/constants.js"

export function fetchUserById(id) {
    return fetch(`${API_BASE_URL}users/${id}`)
        .then(res => res.json())
}

export function fetchLogin(name, password) {
    return fetch(`${API_BASE_URL}users/login`, {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ name, password }),
    }).then(res => res.json())
}

export function fetchCreateUser({ name, email, password }) {
    return fetch(`${API_BASE_URL}users`, {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ name, email, password }),
    }).then(res => res.json())
}