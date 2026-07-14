import {
    getLocationDetails
} from "../../handlers/LocationHandlers.js"

describe("LocationHandlers", function () {

    it("should render location details", async function () {

        window.fetch = async function () {

            return {
                json: async function () {
                    return {
                        id_loc: 1,
                        name: "Lisbon",
                        type: "City",
                        parentId: null
                    }
                }
            }
        }

        const mainContent =
            document.createElement("div")

        await getLocationDetails(mainContent, {
            id: 1
        })

        const title =
            mainContent.querySelector("h1")

        title.textContent.should.equal("Lisbon")
    })

})