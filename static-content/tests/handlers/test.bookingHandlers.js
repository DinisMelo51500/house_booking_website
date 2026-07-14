import {
    getBookingDetails
} from "../../handlers/BookingHandlers.js"

describe("BookingHandlers", function () {

    it("should render booking details", async function () {

        window.fetch = async function () {

            return {
                ok: true,

                json: async function () {
                    return {
                        id: 1
                    }
                }
            }
        }

        const mainContent =
            document.createElement("div")

        await getBookingDetails(mainContent, {
            id: 1
        })

        mainContent.should.not.equal(null)
    })

})