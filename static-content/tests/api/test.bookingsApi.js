import {
    fetchBookingsByUser,
    fetchBookingsByHouse,
    fetchBookingById
} from "../../api/bookingsApi.js"

describe("bookingsApi", function () {

    it("should fetch bookings by user", async function () {

        window.fetch = async function () {

            return {
                ok: true,

                json: async function () {
                    return [
                        { id: 1 }
                    ]
                }
            }
        }

        const bookings =
            await fetchBookingsByUser({
                userId: 1,
                skip: 0,
                limit: 5
            })

        bookings.length.should.equal(1)
    })

    it("should return empty array on 404", async function () {

        window.fetch = async function () {

            return {
                status: 404,
                ok: false
            }
        }

        const bookings =
            await fetchBookingsByUser({
                userId: 1,
                skip: 0,
                limit: 5
            })

        bookings.should.deep.equal([])
    })

    it("should fetch booking by id", async function () {

        window.fetch = async function () {

            return {
                ok: true,

                json: async function () {
                    return {
                        id: 100
                    }
                }
            }
        }

        const booking =
            await fetchBookingById(100)

        booking.id.should.equal(100)
    })

    it("should throw on invalid booking", async function () {

        window.fetch = async function () {

            return {
                ok: false
            }
        }

        try {

            await fetchBookingById(1)

            throw new Error("Should fail")

        } catch (e) {

            e.message.should.equal(
                "Booking not found"
            )
        }
    })

})