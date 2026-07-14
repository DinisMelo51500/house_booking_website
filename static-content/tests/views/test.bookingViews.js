import {
    userBookingView,
    houseBookingView,
    bookingDetailsView
} from "../../views/BookingViews.js"

describe("BookingViews", function () {

    it("should render user bookings", function () {

        const mainContent =
            document.createElement("div")

        const bookings = [
            {
                id: 1,
                startDate: "2024-01-01",
                endDate: "2024-01-05"
            }
        ]

        userBookingView(
            bookings,
            mainContent,
            {
                userId: 1,
                skip: 0,
                limit: 5
            },
            true
        )

        const cards =
            mainContent.querySelectorAll(".house-card")

        cards.length.should.equal(1)

        const link =
            mainContent.querySelector(".house-card a")

        link.getAttribute("href")
            .should.include("#bookings/1")
    })

    it("should render house bookings", function () {

        const mainContent =
            document.createElement("div")

        houseBookingView(
            [
                {
                    id: 1,
                    startDate: "2024-01-01",
                    endDate: "2024-01-05"
                }
            ],
            mainContent,
            {
                houseId: 1,
                skip: 0,
                limit: 5,
                startDate: "2024-01-01",
                endDate: "2024-01-10"
            },
            true
        )

        const cards =
            mainContent.querySelectorAll(".house-card")

        cards.length.should.equal(1)
    })

    it("should render booking details", function () {

        const mainContent =
            document.createElement("div")

        bookingDetailsView(
            {
                id: 1,
                startDate: "2024-01-01",
                endDate: "2024-01-05",
                houseId: 2,
                userId: 3
            },
            mainContent
        )

        const title =
            mainContent.querySelector(".booking-details__name")

        title.textContent.should.equal("Booking 1")

        const links =
            mainContent.querySelectorAll("a")

        links.length.should.equal(2)
    })

})