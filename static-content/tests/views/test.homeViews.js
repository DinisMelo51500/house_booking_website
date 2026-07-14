import { homeView } from "../../views/HomeViews.js"

describe("HomeViews", function () {

    it("should render hero section", function () {

        const mainContent =
            document.createElement("div")

        homeView(mainContent, [], function () {})

        const hero =
            mainContent.querySelector(".hero")

        hero.should.not.equal(null)
    })

    it("should render features section", function () {

        const mainContent =
            document.createElement("div")

        homeView(mainContent, [], function () {})

        const features =
            mainContent.querySelector(".features")

        features.should.not.equal(null)
    })

    it("should render location options", function () {

        const mainContent =
            document.createElement("div")

        const locations = [
            { id: 1, name: "Lisbon" },
            { id: 2, name: "Porto" }
        ]

        homeView(mainContent, locations, function () {})

        const select =
            mainContent.querySelector("select")

        const options =
            select.querySelectorAll("option")

        options.length.should.equal(3)

        options[1].textContent.should.equal("Lisbon")
        options[2].textContent.should.equal("Porto")
    })

    it("should call onSearch with filters", function () {

        const mainContent =
            document.createElement("div")

        let captured = null

        homeView(
            mainContent,
            [
                { id: 1, name: "Lisbon" }
            ],
            function (data) {
                captured = data
            }
        )

        const areaInput =
            mainContent.querySelector("input[placeholder*='Min area']")

        const priceInput =
            mainContent.querySelector("input[placeholder*='Max price']")

        const select =
            mainContent.querySelector("select")

        const button =
            mainContent.querySelector("button")

        areaInput.value = "50"
        priceInput.value = "200"

        select.value = "1"
        select.dispatchEvent(new Event("change"))

        button.click()

        captured.should.include({
            area: "50",
            price: "200",
            location: "1"
        })
    })

    it("should render explore houses link", function () {

        const mainContent =
            document.createElement("div")

        homeView(mainContent, [], function () {})

        const link =
            mainContent.querySelector("a.secondary-btn")

        link.getAttribute("href")
            .should.equal("#houses")
    })

})