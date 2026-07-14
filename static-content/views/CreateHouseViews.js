import { div, h1, input, button, label, select, option } from "../helpers/helpers.js"

export function createHouseView(mainContent, initialCountries, onLoadSubLocations, onSubmit) {
    const title = input({ placeholder: "e.g., Cozy Apartment in Lisbon", required: "true" })
    const area = input({ type: "number", placeholder: "Area (m²)", min: "1", required: "true" })
    const price = input({ type: "number", placeholder: "Price per night (€)", min: "0.01", step: "0.01", required: "true" })

    const description = document.createElement("textarea")
    description.placeholder = "Tell us about your house (max 500 characters)..."
    description.maxLength = 500

    const locationCascadeContainer = div({ class: "location-cascade" })

    const redirectToCreateLocationBtn = button({
        class: "btn-create-location",
        type: "button"
    }, "Create a location")

    redirectToCreateLocationBtn.addEventListener("click", (e) => {
        e.preventDefault()
        window.location.hash = "#create-location"
    })

    let currentActiveParentId = null

    const levels = [
        { type: "COUNTRY", label: "Country" },
        { type: "DISTRICT", label: "District" },
        { type: "MUNICIPALITY", label: "Municipality" },
        { type: "LOCALITY", label: "Locality" }
    ]

    function renderLevel(levelIndex, locationsList) {
        while (locationCascadeContainer.children.length > levelIndex) {
            locationCascadeContainer.removeChild(locationCascadeContainer.lastChild)
        }

        if (levelIndex >= levels.length) return

        const currentLevel = levels[levelIndex]
        const selectOptions = [option({ value: "" }, `-- Select ${currentLevel.label} --`)]
        locationsList.forEach(loc => {
            selectOptions.push(option({ value: loc.id }, loc.name))
        })

        const levelSelect = select({ "data-type": currentLevel.type }, ...selectOptions)

        levelSelect.addEventListener("change", async () => {
            const selectedVal = levelSelect.value
            if (selectedVal) {
                currentActiveParentId = parseInt(selectedVal)
                const subLocations = await onLoadSubLocations(currentActiveParentId)
                renderLevel(levelIndex + 1, subLocations)
            } else {
                currentActiveParentId = levelIndex > 0 ? parseInt(locationCascadeContainer.children[levelIndex - 1].querySelector("select").value) : null
                renderLevel(levelIndex + 1, [])
            }
        })

        locationCascadeContainer.appendChild(
            div({ class: "cascade-item" },
                label({}, currentLevel.label, levelSelect)
            )
        )
    }

    renderLevel(0, initialCountries)

    const btn = button({ class: "primary-btn-filled", style: "width: 100%; margin-top: 20px;" }, "Create House")

    btn.addEventListener("click", async (e) => {
        e.preventDefault()
        if (!title.value.trim() || !area.value || !price.value) {
            alert("Please fill in all required fields.")
            return
        }

        if (!currentActiveParentId) {
            alert("Please select a valid location path for your house.")
            return
        }

        await onSubmit({
            title: title.value.trim(),
            location: currentActiveParentId,
            areaSqMt: parseInt(area.value),
            pricePerNight: parseFloat(price.value),
            description: description.value.trim()
        })
    })

    mainContent.replaceChildren(
        div({ class: "create-house-container" },
            h1("Create House"),

            div({ class: "form-group" },
                label({}, "Title *"),
                title
            ),

            div({ class: "form-group" },
                label({}, "Location Path *"),
                locationCascadeContainer,
                redirectToCreateLocationBtn
            ),

            div({ class: "form-group" },
                label({}, "Area (m²) *"),
                area
            ),

            div({ class: "form-group" },
                label({}, "Price per night (€) *"),
                price
            ),

            div({ class: "form-group" },
                label({}, "Description"),
                description
            ),

            btn
        )
    )
}