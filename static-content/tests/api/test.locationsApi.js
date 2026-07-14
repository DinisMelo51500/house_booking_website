import {
    fetchLocationById
} from "../../api/locationsApi.js"

describe("locationsApi", function () {

    it("should fetch location", async function () {

        window.fetch = async function () {

            return {
                json: async function () {
                    return {
                        id: 1,
                        name: "Lisbon"
                    }
                }
            }
        }

        const location =
            await fetchLocationById(1)

        location.name.should.equal("Lisbon")
    })

})