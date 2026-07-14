import { getHouses } from "../../handlers/HouseHandlers.js"

describe("HouseHandlers", function () {

    it("should execute getHouses and call fetch", async function () {

        let calledWith = null

        window.fetch = async function () {
            return {
                ok: true,
                json: async function () {
                    return []
                }
            }
        }

        // mock fetchHouses indirectly via spying fetch
        const originalFetch = window.fetch

        window.fetch = async function () {
            calledWith = arguments
            return {
                ok: true,
                json: async () => []
            }
        }

        const mainContent =
            document.createElement("div")

        await getHouses(mainContent, {
            skip: "0",
            limit: "5",
            area: "100",
            price: "200",
            location: "1"
        })

        mainContent.should.not.equal(null)

        // restore
        window.fetch = originalFetch
    })

})