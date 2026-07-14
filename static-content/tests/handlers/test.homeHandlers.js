describe("HomeHandlers", function () {

    it("should render home with locations", async function () {

        window.fetch = async function () {
            return {
                ok: true,
                json: async function () {
                    return [
                        { id: 1, name: "Lisbon" },
                        { id: 2, name: "Porto" }
                    ]
                }
            }
        }

        const { getHome } =
            await import("../../handlers/HomeHandlers.js")

        const mainContent =
            document.createElement("div")

        await getHome(mainContent)

        const hero =
            mainContent.querySelector(".hero")

        hero.should.not.equal(null)

        const select =
            mainContent.querySelector("select")

        select.should.not.equal(null)

        const options =
            select.querySelectorAll("option")

        // 1 default + 2 locations
        options.length.should.equal(3)

        options[1].textContent.should.equal("Lisbon")
        options[2].textContent.should.equal("Porto")
    })

})