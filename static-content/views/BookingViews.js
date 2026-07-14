import { div, h1, p, a, button, showToast, input, label } from "../helpers/helpers.js"

function bookingCard(b) {
    return div({ class: "house-card" },
        a({ href: `#bookings/${b.id}` }, `Booking ${b.id}`),

        div({ class: "house-info" },
            p(`${b.startDate} → ${b.endDate}`)
        )
    )
}

function bookingsView(bookings, mainContent, prev, next) {
    mainContent.replaceChildren(
        div({ class: 'bookings-page' },
            h1({ class: 'bookings-page__title' }, 'Bookings'),

            div({ class: 'houses-grid' },
                ...bookings.map(bookingCard)
            ),

            div({ class: 'pagination' },
                ...(prev ? [prev] : []),
                ...(next ? [next] : [])
            ),
        )
    )
}

export function userBookingView(bookings, mainContent, { userId, skip, limit }, hasNext) {
    const base = `#bookings/users/${userId}`

    const prev = skip > 0
        ? a({ href: `${base}?skip=${(skip - limit < 0) ? 0 : (skip - limit)}` }, '← Previous')
        : null

    const next = hasNext
        ? a({ href: `${base}?skip=${skip + limit}` }, 'Next →')
        : null

    bookingsView(bookings, mainContent, prev, next)
}

export function houseBookingView(bookings, mainContent, { houseId, skip, limit, startDate, endDate }, hasNext) {
    const base = `#bookings/${houseId}/dates?startDate=${startDate}&endDate=${endDate}`

    const prev = skip > 0
        ? a({ href: `${base}?skip=${(skip - limit < 0) ? 0 : (skip - limit)}` }, '← Previous')
        : null

    const next = hasNext
        ? a({ href: `${base}&skip=${skip + limit}` }, 'Next →')
        : null

    bookingsView(bookings, mainContent, prev, next)
}

export function bookingDetailsView(b, mainContent) {
    mainContent.replaceChildren(
        div({ class: "booking-details" },
            div({ class: "booking-details__header" },
                h1({ class: "booking-details__name" }, `Booking ${b.id}`),
                p({ class: "booking-details__dates" }, `${b.startDate} → ${b.endDate}`),
            ),

            div({ class: "booking-details__actions" },
                a({
                    class: "booking-details__btn booking-details__btn--house",
                    href: `#houses/${b.houseId}`
                }, "View House"),

                a({
                    class: "booking-details__btn booking-details__btn--user",
                    href: `#users/${b.userId}`
                }, "View User"),

                a({
                    class: "booking-details__btn booking-details__btn--edit",
                    href: `#bookings/${b.id}/edit`
                }, "Edit"),

                button({
                    class: "booking-details__btn booking-details__btn--delete",
                    onClick: () => {
                        if (!confirm("Delete this booking?")) return
                        window.location.hash = `#bookings/${b.id}/delete`
                    }
                }, "Delete"),
            ),
        )
    )
}

export function createBookingView(mainContent, { houseId, userId }) {
    const startInput = input({
        type: "date",
        id: "startDate",
        class: "booking-form__input"
    })

    const endInput = input({
        type: "date",
        id: "endDate",
        class: "booking-form__input"
    })

    mainContent.replaceChildren(
        div({ class: "booking-form" },
            h1({ class: "booking-form__title" }, "New Booking"),

            div({ class: "booking-form__group" },
                label({
                    for: "startDate",
                    class: "booking-form__label"
                }, "Start Date"),

                startInput,
            ),

            div({ class: "booking-form__group" },
                label({
                    for: "endDate",
                    class: "booking-form__label"
                }, "End Date"),

                endInput,
            ),

            div({ class: "booking-form__actions" },
                button({
                    class: "booking-form__btn booking-form__btn--submit",
                    onClick: () => {
                        const startDate = startInput.value
                        const endDate = endInput.value

                        if (!startDate || !endDate) {
                            showToast("Please fill in both dates.")
                            return
                        }

                        window.location.hash =
                            `#bookings/create/confirm?houseId=${houseId}&userId=${userId}&startDate=${startDate}&endDate=${endDate}`
                    }
                }, "Create Booking"),

                a({
                    class: "booking-form__btn booking-form__btn--cancel",
                    href: `#houses/${houseId}`
                }, "Cancel"),
            ),
        )
    )
}

export function editBookingView(b, mainContent) {
    const startInput = input({
        type: "date",
        id: "startDate",
        class: "booking-form__input",
        value: b.startDate
    })

    const endInput = input({
        type: "date",
        id: "endDate",
        class: "booking-form__input",
        value: b.endDate
    })

    mainContent.replaceChildren(
        div({ class: "booking-form" },
            h1({ class: "booking-form__title" }, `Edit Booking ${b.id}`),

            div({ class: "booking-form__group" },
                label({
                    for: "startDate",
                    class: "booking-form__label"
                }, "Start Date"),

                startInput,
            ),

            div({ class: "booking-form__group" },
                label({
                    for: "endDate",
                    class: "booking-form__label"
                }, "End Date"),

                endInput,
            ),

            div({ class: "booking-form__actions" },
                button({
                    class: "booking-form__btn booking-form__btn--submit",
                    onClick: () => {
                        const startDate = startInput.value
                        const endDate = endInput.value

                        if (!startDate || !endDate) {
                            showToast("Please fill in both dates.")
                            return
                        }

                        window.location.hash =
                            `#bookings/${b.id}/edit/confirm?startDate=${startDate}&endDate=${endDate}`
                    }
                }, "Save Changes"),

                a({
                    class: "booking-form__btn booking-form__btn--cancel",
                    href: `#bookings/${b.id}`
                }, "Cancel"),
            ),
        )
    )
}