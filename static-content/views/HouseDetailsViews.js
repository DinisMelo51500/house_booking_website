import { div, h1, p, a, span, input } from "../helpers/helpers.js"
import {DEFAULT_END_DATE, DEFAULT_START_DATE} from "../config/constants.js"

export function houseDetailsView(house, mainContent) {
    const form = document.createElement("form")

    const startLabel = document.createElement("label")
    startLabel.textContent = "Start date"

    const startInput = document.createElement("input")
    startInput.type = "date"

    const endLabel = document.createElement("label")
    endLabel.textContent = "End date"

    const endInput = document.createElement("input")
    endInput.type = "date"

    const submit = document.createElement("input")
    submit.type = "submit"
    submit.value = "View bookings"

    form.append(startLabel, startInput, endLabel, endInput, submit)

    form.addEventListener("submit", (e) => {
        e.preventDefault()

        const startDate = startInput.value
        const endDate = endInput.value

        window.location.hash =
            `bookings/${house.id}/dates?startDate=${startDate}&endDate=${endDate}`
    })

    mainContent.replaceChildren(
        div({ class: 'house-details' },
            div({ class: 'house-details__header' },
                div(
                    h1({ class: 'house-details__name' }, house.title),
                    p({ class: 'house-details__size' }, `${house.areaSqMt} m²`),
                ),
                p({ class: 'house-details__price' }, `€ ${house.pricePerNight} `, span('/ night')),
            ),

            p({ class: 'house-details__description' }, house.description),

            form,

            div({ class: 'house-details__actions' },
                a({ class: 'house-details__btn house-details__btn--location', href: `#locations/${house.location}` }, 'View Location'),
                a({ class: 'house-details__btn house-details__btn--owner', href: `#users/${house.ownerId}` }, 'View Owner'),
                a({class: 'house-details__btn house-details__btn--availability', href: `#houses/${house.id}/availability`}, 'Create Booking'),
            ),
        )
    )
}

export function houseAvailabilityMonthView(houseId, mainContent) {

    const form = document.createElement("form")

    const labelYear = document.createElement("label")
    labelYear.textContent = "Year"

    const inputYear = document.createElement("input")
    inputYear.type = "number"
    inputYear.value = new Date().getFullYear()

    const labelMonth = document.createElement("label")
    labelMonth.textContent = "Month"

    const inputMonth = document.createElement("input")
    inputMonth.type = "number"
    inputMonth.min = 1
    inputMonth.max = 12
    inputMonth.value = new Date().getMonth() + 1

    const submit = document.createElement("input")
    submit.type = "submit"
    submit.value = "View Availability"

    form.append(labelYear, inputYear, labelMonth, inputMonth, submit)

    form.addEventListener("submit", (e) => {
        e.preventDefault()

        const year = parseInt(inputYear.value)
        const month = parseInt(inputMonth.value)

        window.location.hash =
            `houses/${houseId}/availability/days?year=${year}&month=${month}`
    })

    mainContent.replaceChildren(
        div({ class: "house-availability" },
            h1("Available Days"),
            form
        )
    )
}


export function houseAvailabilityView(houseId, mainContent, query) {

    const year = query?.year
        ? parseInt(query.year)
        : new Date().getFullYear()

    const month = query?.month
        ? parseInt(query.month)
        : new Date().getMonth() + 1

    fetch(`/bookings/${houseId}/availability?year=${year}&month=${month}`)
        .then(res => res.json())
        .then(data => {

            mainContent.replaceChildren(
                div(
                    h1("Available Days"),

                    p(`Year: ${year}`),
                    p(`Month: ${month}`),

                    div({ class: "availability-grid" },
                        ...(data.availableDays ?? []).map(day => {
                            const date = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`
                            return a(
                                { class: "day-box available", href: `#bookings/create?houseId=${houseId}&startDate=${date}` },
                                String(day)
                            )
                        })
                    )
                )
            )
        })
}