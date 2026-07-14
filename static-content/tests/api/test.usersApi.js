import {
    fetchUserById
} from "../../api/usersApi.js"

describe("usersApi", function () {

    it("should fetch user", async function () {

        window.fetch = async function () {

            return {
                json: async function () {
                    return {
                        id: 1,
                        name: "John"
                    }
                }
            }
        }

        const user =
            await fetchUserById(1)

        user.name.should.equal("John")
    })

})