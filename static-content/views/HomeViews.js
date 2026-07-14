import { div, h1, h2, p, a, span, input, button, select, option, label } from "../helpers/helpers.js"

export function homeView(mainContent, locations, onSearch) {
    const areaInput = input({ type: "number", placeholder: "Min area (m²)", class: "filter-input", min: "0" })
    const priceInput = input({ type: "number", placeholder: "Max price (€)", class: "filter-input", min: "0" })

    const locationSelect = select({ class: "filter-input" },
        option({ value: "" }, "Any location"),
        ...locations.map(loc => option({ value: loc.id }, loc.name))
    )

    const searchBtn = button({ class: "primary-btn" }, "Search")

    searchBtn.addEventListener("click", () => {
        const area = areaInput.value.trim()
        const price = priceInput.value.trim()
        const location = locationSelect.value
        onSearch({ area, price, location })
    })

    const userName = localStorage.getItem("currentUserName")

    const navBar = userName
        ? div({ class: "top-nav" },
            span({ class: "top-nav__greeting" }, `Hello, ${userName}`),
            a({ href: `#users/${localStorage.getItem("currentUserId")}`, class: "top-nav__link" }, "My Profile"),
            a({ href: "#logout", class: "top-nav__btn secondary-btn" }, "Log Out")
        )
        : div({ class: "top-nav" },
            a({ href: "#users/login", class: "top-nav__link" }, "Log In"),
            a({ href: "#users", class: "top-nav__btn primary-btn" }, "Sign Up")
        )

    mainContent.replaceChildren(
        div({class: "home-container"},
            navBar,
            div({ class: "hero" },
                span({ class: "hero-logo" }, "TuffBookings"),
                h1("Find your next stay"),
                p("Discover unique houses, explore locations and book your perfect place."),

                div({ class: "filters" },
                    label({}, "Min Area (m²)", areaInput),
                    label({}, "Max Price (€)", priceInput),
                    label({}, "Location", locationSelect),
                    searchBtn
                ),

                a({ href: "#houses", class: "secondary-btn" }, "Explore All Houses")
            ),

            div({ class: "features" },
                div({ class: "feature-card" }, h2("🏡 Amazing Houses"), p("Browse a curated list of beautiful homes.")),
                div({ class: "feature-card" }, h2("📍 Great Locations"), p("Explore cities and unique places.")),
                div({ class: "feature-card" }, h2("📅 Easy Booking"), p("Simple and fast booking system."))
            )
        )
    )
}