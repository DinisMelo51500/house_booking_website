import { div, h1, a } from "../helpers/helpers.js"

export function loginView(mainContent, login) {
    const form = document.createElement("form")
    form.className = "auth-form"

    const nameLabel = document.createElement("label")
    nameLabel.textContent = "Username"

    const nameInput = document.createElement("input")
    nameInput.type = "text"
    nameInput.required = true

    const passwordLabel = document.createElement("label")
    passwordLabel.textContent = "Password"

    const passwordInput = document.createElement("input")
    passwordInput.type = "password"
    passwordInput.required = true

    const submit = document.createElement("input")
    submit.type = "submit"
    submit.value = "Log In"

    form.append(nameLabel, nameInput, passwordLabel, passwordInput, submit)

    form.addEventListener("submit", (e) => {
        e.preventDefault()

        const name = nameInput.value
        const password = passwordInput.value

        login(name, password)
    })

    mainContent.replaceChildren(
        div({ class: 'auth-container' },
            div({ class: 'auth-container__header' },
                h1({ class: 'auth-container__title' }, 'Login')
            ),

            form,

            div({ class: 'auth-container__actions' },
                a({ class: 'auth-container__link', href: '#users' }, 'Need an account? Sign Up')
            )
        )
    )
}

export function signUpView(mainContent, signup) {
    const form = document.createElement("form")
    form.className = "auth-form"

    const nameLabel = document.createElement("label")
    nameLabel.textContent = "Full Name"

    const nameInput = document.createElement("input")
    nameInput.type = "text"
    nameInput.required = true

    const emailLabel = document.createElement("label")
    emailLabel.textContent = "Email"

    const emailInput = document.createElement("input")
    emailInput.type = "email"
    emailInput.required = true

    const passwordLabel = document.createElement("label")
    passwordLabel.textContent = "Password"

    const passwordInput = document.createElement("input")
    passwordInput.type = "password"
    passwordInput.required = true

    const submit = document.createElement("input")
    submit.type = "submit"
    submit.value = "Sign Up"

    form.append(nameLabel, nameInput, emailLabel, emailInput, passwordLabel, passwordInput, submit)

    form.addEventListener("submit", (e) => {
        e.preventDefault()

        const name = nameInput.value
        const email = emailInput.value
        const password = passwordInput.value

        signup(name, email, password)
    })

    mainContent.replaceChildren(
        div({ class: 'auth-container' },
            div({ class: 'auth-container__header' },
                h1({ class: 'auth-container__title' }, 'Create Account')
            ),

            form,

            div({ class: 'auth-container__actions' },
                a({ class: 'auth-container__link', href: '#users/login' }, 'Already have an account? Log In')
            )
        )
    )
}