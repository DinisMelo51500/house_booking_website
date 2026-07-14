import {
    div,
    h1,
    p,
    button,
    span
} from "../../helpers/helpers.js"

describe("Helpers", function () {

    it("should create div element", function () {

        const el = div("Hello")

        el.tagName.should.equal("DIV")
        el.textContent.should.equal("Hello")
    })

    it("should create h1 with class", function () {

        const el = h1(
            { class: "title" },
            "Welcome"
        )

        el.getAttribute("class")
            .should.equal("title")

        el.textContent
            .should.equal("Welcome")
    })

    it("should append child elements", function () {

        const child = p("Paragraph")

        const parent = div(child)

        parent.children.length.should.equal(1)
    })

    it("should register click events", function () {

        let clicked = false

        const btn = button(
            {
                onClick: () => clicked = true
            },
            "Click"
        )

        btn.click()

        clicked.should.equal(true)
    })

    it("should support nested elements", function () {

        const el = div(
            h1("Title"),
            p("Description"),
            span("Footer")
        )

        el.children.length.should.equal(3)
    })

})