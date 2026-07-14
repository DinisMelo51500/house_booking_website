import { div, h1, p, a } from "../helpers/helpers.js"
import { fetchUserById, fetchLogin, fetchCreateUser } from "../api/usersApi.js"
import { loginView, signUpView } from "../views/UserViews.js"

export function getUserDetails(mainContent, params) {
    const id = params?.id ? parseInt(params.id) : null
    fetchUserById(id)
        .then(u => {
            mainContent.replaceChildren(
                div({ class: 'user-details' },
                    div({ class: 'user-details__header' },
                        div({ class: 'user-details__avatar' }, u.name[0].toUpperCase()),
                        div(
                            h1({ class: 'user-details__name' }, u.name),
                            p({ class: 'user-details__email' }, u.email),
                        ),
                    ),
                    div({ class: 'user-details__actions' },
                        a({ class: 'user-details__btn', href: `#bookings/users/${id}` }, 'View Bookings'),
                    ),
                )
            )
        })
}

export function getLogin(mainContent) {
    const handleLogin = async (name, password) => {
        try {
            const userData = await fetchLogin(name, password)

            localStorage.setItem("authToken", userData.token)
            localStorage.setItem("currentUserName", name)
            localStorage.setItem("currentUserId", userData.id)

            window.location.hash = `#home`
        } catch (e) {
            console.error("Login failed", e)
            window.location.hash = `#users/login`
        }
    }

    loginView(mainContent, handleLogin)
}

export function getSignUp(mainContent) {
    const handleSignUp = async (name, email, password) => {
        try {
            await fetchCreateUser({ name, email, password })
            getLogin(mainContent)

        } catch (e) {
            console.error("Sign up failed", e)
            window.location.hash = `#users`
        }
    }

    signUpView(mainContent, handleSignUp)
}

export function logOut() {
    localStorage.removeItem("authToken")
    localStorage.removeItem("currentUserId")
    localStorage.removeItem("currentUserName")

    window.location.hash = "home"
}