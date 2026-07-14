import {
    getUserDetails
} from "../../handlers/UserHandlers.js"

describe("UserHandlers", function () {

    it("should render user details", async function () {

        window.fetch = async function () {

            return {
                json: async function () {
                    return {
                        id: 1,
                        name: "John",
                        email: "john@test.com"
                    }
                }
            }
        }

        const mainContent =
            document.createElement("div")

        await getUserDetails(mainContent, {
            id: 1
        })

        setTimeout(() => {

            const userName =
                mainContent.querySelector(
                    ".user-details__name"
                )

            userName.textContent
                .should.equal("John")

        }, 0)
    })

})