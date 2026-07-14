import { div, h1, input, button, label, select, option } from "../helpers/helpers.js"

export function createLocationView(mainContent, initialCountries, onLoadSubLocations, onSubmit) {
    const newLocationNameInput = input({ placeholder: "e.g., Oeiras or Paço de Arcos", required: "true" })
    const locationCascadeContainer = div({ class: "location-cascade" })

    let currentActiveParentId = null
    let currentLevelType = "COUNTRY"

    const levels = [
        { type: "COUNTRY", label: "Parent Country" },
        { type: "DISTRICT", label: "Parent District" },
        { type: "MUNICIPALITY", label: "Parent Municipality" }
    ]

    function renderLevel(levelIndex, locationsList) {
        while (locationCascadeContainer.children.length > levelIndex) {
            locationCascadeContainer.removeChild(locationCascadeContainer.lastChild)
        }

        if (levelIndex >= levels.length) {
            currentLevelType = "LOCALITY"
            return
        }

        const currentLevel = levels[levelIndex]

        if (currentActiveParentId === null && levelIndex === 0) {
            currentLevelType = "COUNTRY"
        }

        const selectOptions = [option({ value: "" }, `-- Optional: Select ${currentLevel.label} --`)]
        locationsList.forEach(loc => {
            selectOptions.push(option({ value: loc.id }, loc.name))
        })

        const levelSelect = select({ "data-type": currentLevel.type }, ...selectOptions)

        levelSelect.addEventListener("change", async () => {
            const selectedVal = levelSelect.value
            if (selectedVal) {
                currentActiveParentId = parseInt(selectedVal)

                if (currentLevel.type === "COUNTRY") currentLevelType = "DISTRICT"
                else if (currentLevel.type === "DISTRICT") currentLevelType = "MUNICIPALITY"

                const subLocations = await onLoadSubLocations(currentActiveParentId)
                renderLevel(levelIndex + 1, subLocations)
            } else {
                if (levelIndex > 0) {
                    currentActiveParentId = parseInt(locationCascadeContainer.children[levelIndex - 1].querySelector("select").value) || null
                    currentLevelType = levels[levelIndex].type
                } else {
                    currentActiveParentId = null
                    currentLevelType = "COUNTRY"
                }
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

    const btn = button({ class: "primary-btn-filled", style: "width: 100%; margin-top: 10px;" }, "Save Location")

    btn.addEventListener("click", async (e) => {
        e.preventDefault()
        const name = newLocationNameInput.value.trim()

        if (!name) {
            alert("Please enter a name for the location.")
            return
        }

        await onSubmit({
            name: name,
            type: currentLevelType,
            parentId: currentActiveParentId
        })
    })

    mainContent.replaceChildren(
        div({ class: "create-location-container" },
            h1("Create New Location"),

            div({ class: "form-group" },
                label({}, "Select Parent Location Hierarchy (Leave empty to create a Country)"),
                locationCascadeContainer
            ),

            div({ class: "form-group" },
                label({}, `New Location Name *`),
                newLocationNameInput
            ),

            btn
        )
    )
}