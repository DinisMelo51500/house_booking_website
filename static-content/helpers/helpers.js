function helper(tag, attrsOrChild, ...rest) {
    const element = document.createElement(tag)

    // Check if first argument is an attributes object
    const hasAttrs = attrsOrChild !== null
        && typeof attrsOrChild === "object"
        && !(attrsOrChild instanceof HTMLElement)

    const attrs    = hasAttrs ? attrsOrChild : {}
    const children = hasAttrs ? rest : [attrsOrChild, ...rest]

    // Apply attributes
    Object.entries(attrs).forEach(([key, value]) => {
        if (key === "onClick") {
            element.addEventListener("click", value)
        } else {
            element.setAttribute(key, value)
        }
    })

    // Append children
    children.forEach(child => {
        if (child === null || child === undefined) return
        if (typeof child === "string") {
            element.appendChild(document.createTextNode(child))
        } else {
            element.appendChild(child)
        }
    })

    return element
}

export function showToast(message) {
    const toastDiv = div({ class: 'toast-notification' }, message);

    document.body.appendChild(toastDiv);

    setTimeout(() => {
        toastDiv.remove();
    }, 3000);
}

export function div(...c)  { return helper("div",  ...c) }
export function h1(...c)   { return helper("h1",   ...c) }
export function h2(...c)   { return helper("h2",   ...c) }
export function p(...c)    { return helper("p",     ...c) }
export function ul(...c)   { return helper("ul",    ...c) }
export function li(...c)   { return helper("li",    ...c) }
export function a(...c)    { return helper("a",     ...c) }
export function span(...c) { return helper("span",  ...c) }
export function button(...c) { return helper("button", ...c) }
export function input(...c)  { return helper("input",  ...c) }
export function select(...c) { return helper("select", ...c) }
export function option(...c) { return helper("option", ...c) }
export function label(...c)  { return helper("label",  ...c) }