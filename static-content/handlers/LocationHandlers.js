import { div, h1, p, a, span } from "../helpers/helpers.js"
import { fetchLocationById } from "../api/locationsApi.js"

async function getLineage(id) {
    const lineage = []

    let current = await fetchLocationById(id)

    while (current) {
        lineage.unshift(current)
        if (!current.parentId) break
        current = await fetchLocationById(current.parentId)
    }

    return lineage
}

export async function getLocationDetails(mainContent, params) {
    const id = params?.id ? parseInt(params.id) : null
    const lineage = await getLineage(id)
    const current = lineage[lineage.length - 1]

    mainContent.replaceChildren(
        div({ class: "card location-details" },
            h1(current.name),
            p(current.type),

            div({ class: "location-breadcrumb" },
                ...lineage.map((loc, index) =>
                    div({ class: "location-breadcrumb__item" },
                        index < lineage.length - 1
                            ? a(
                                { href: `#locations/${loc.id}` },
                                loc.name
                              )
                            : span({ class: "location-breadcrumb__current" }, loc.name),

                        index < lineage.length - 1
                            ? span({ class: "location-breadcrumb__sep" }, " → ")
                            : null
                    )
                )
            )
        )
    )
}