import {
    housesView
} from "../../views/HouseViews.js"

import { houseDetailsView } from "../../views/HouseDetailsViews.js";

describe("HouseViews", function () {

    it("should render houses list", function () {

        const mainContent =
            document.createElement("div")

        const houses = [
            {
                id: 1,
                title: "Beach House",
                areaSqMt: 100,
                pricePerNight: 120
            }
        ]

        housesView(
            houses,
            mainContent,
            {
                skip: 0,
                limit: 5,
                area: 50,
                price: 200,
                location: 1
            },
            true
        )

        const title =
            mainContent.querySelector("h1")

        title.textContent.should.equal("Houses")

        const cards =
            mainContent.querySelectorAll(".house-card")

        cards.length.should.equal(1)

        const link =
            mainContent.querySelector(".house-card a")

        link.getAttribute("href")
            .should.equal("#houses/1")
    })

    it("should render pagination buttons correctly with filters", function () {

        const mainContent =
            document.createElement("div")

        housesView(
            [],
            mainContent,
            {
                skip: 10,
                limit: 5,
                area: 50,
                price: 200,
                location: 1
            },
            true
        )

        const links =
            mainContent.querySelectorAll(".pagination a")

        links.length.should.equal(2)

        const prev =
            Array.from(links)
                .find(a => a.textContent.includes("Previous"))

        const next =
            Array.from(links)
                .find(a => a.textContent.includes("Next"))

        prev.textContent.should.equal("← Previous")
        next.textContent.should.equal("Next →")

        // 🔥 NOVO: validar filtros na URL
        next.getAttribute("href").should.include("area=50")
        next.getAttribute("href").should.include("price=200")
        next.getAttribute("href").should.include("location=1")
    })

    it("should render house details", function () {

        const mainContent =
            document.createElement("div")

        houseDetailsView(
            {
                id: 1,
                title: "Beach House",
                areaSqMt: 100,
                pricePerNight: 120,
                description: "Nice place",
                location: 3,
                ownerId: 9
            },
            mainContent
        )

        const name =
            mainContent.querySelector(".house-details__name")

        name.textContent.should.equal("Beach House")

        const price =
            mainContent.querySelector(".house-details__price")

        price.textContent.should.include("120")

        const links =
            mainContent.querySelectorAll("a")

        links.length.should.equal(4)
    })

})