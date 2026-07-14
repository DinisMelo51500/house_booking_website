import {
    fetchHouses,
    fetchHouseById
} from "../../api/housesApi.js"

describe("housesApi", function () {

    beforeEach(function () {

        window.fetch = async function () {

            return {
                ok: true,

                json: async function () {
                    return [
                        {
                            id: 1,
                            name: "House A"
                        }
                    ]
                }
            }
        }
    })

    it("should fetch houses", async function () {

        const houses = await fetchHouses({
            skip: 0,
            limit: 5
        })

        houses.should.have.length(1)

        houses[0].name
            .should.equal("House A")
    })

    it("should fetch house by id", async function () {

        window.fetch = async function () {

            return {
                ok: true,

                json: async function () {
                    return {
                        id: 10,
                        name: "Beach House"
                    }
                }
            }
        }

        const house = await fetchHouseById(10)

        house.id.should.equal(10)
    })

    it("should throw on failed request", async function () {

        window.fetch = async function () {
            return {
                ok: false
            }
        }

        try {

            await fetchHouses({
                skip: 0,
                limit: 5
            })

            throw new Error("Should fail")

        } catch (e) {

            e.message.should.not.equal("EXPECTED_TO_THROW");
            
            e.should.be.an.instanceOf(Error);
        }
    })

})