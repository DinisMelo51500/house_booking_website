import { div, h1, p, a, span } from "../helpers/helpers.js"

export function housesView(houses, mainContent, { skip, limit, area, price, location }, hasNext) {

    const buildUrl = (newSkip) => {
        const params = new URLSearchParams()
        params.set("skip", newSkip)
        if (area) params.set("area", area)
        if (price) params.set("price", price)
        if (location) params.set("location", location)
        return `#houses?${params.toString()}`
    }

    const createHouseBtn = document.createElement("button")
    createHouseBtn.textContent = "+ Create House"
    createHouseBtn.className = "primary-btn-filled"

    createHouseBtn.addEventListener("click", () => {
        window.location.hash = "#create-house"
    })

    const prev = skip > 0
        ? a({ href: buildUrl(skip - limit < 0 ? 0 : (skip - limit))}, '← Previous')
        : null

    const next = hasNext
        ? a({ href: buildUrl(skip + limit) }, "Next →")
        : null

    mainContent.replaceChildren(
        div({ class: "houses-container" },
            h1({ class: "houses-title" }, "Houses"),

            div({ class: "houses-top-bar" },
                createHouseBtn
            ),

            div({ class: "houses-grid" },
                ...houses.map(h =>
                    div({ class: "house-card" },
                        a({ href: `#houses/${h.id}`, class: "house-card__title" }, h.title),
                        div({ class: "house-info" },
                            p(`Area: ${h.areaSqMt} m²`)
                        ),
                        div({ class: "house-price" },
                            `${h.pricePerNight}€ / night`
                        )
                    )
                )
            ),

            div({ class: "pagination" },
                ...(prev ? [prev] : []),
                ...(prev && next ? [span("  ")] : []),
                ...(next ? [next] : [])
            )
        )
    )
}