import router from "../../router/router.js"

describe("Router", function () {

    beforeEach(function () {
        if (router.clearRoutes) {
            router.clearRoutes()
        }
    })

    it("should add and retrieve route handler", function () {

        function homeHandler() {}

        router.addRouteHandler("home", homeHandler)

        const handler = router.getRouteHandler("home")

        handler.should.be.a("function")
    })

    it("should match dynamic params", function () {

        function houseHandler(mainContent, params) {
            return params.id
        }

        router.addRouteHandler("houses/:id", houseHandler)

        const handler = router.getRouteHandler("houses/10")

        handler.should.be.a("function")
    })

    it("should support query params", function () {

        function bookingsHandler(mainContent, params) {
            return params.skip
        }

        router.addRouteHandler("bookings", bookingsHandler)

        const handler = router.getRouteHandler("bookings?skip=5")

        handler.should.be.a("function")
    })

    it("should return not found handler", function () {

        const notFound = () => "not found"

        router.addDefaultNotFoundRouteHandler(notFound)

        const handler = router.getRouteHandler("unknown")

        handler.should.equal(notFound)
    })

})