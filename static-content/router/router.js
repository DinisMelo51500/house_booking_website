const routes = []
let notFoundRouteHandler = () => { throw "Route handler for unknown routes not defined" }

function addRouteHandler(pathTemplate, handler){
    routes.push({pathTemplate, handler})
}

function addDefaultNotFoundRouteHandler(notFoundRH) {
    notFoundRouteHandler = notFoundRH
}

function getRouteHandler(path) {
    const [pathOnly, queryString] = path.split("?")
    const query = parseQuery(queryString)

    for (const route of routes) {
        const params = matchRoute(route.pathTemplate, pathOnly)
        if (params !== null) {
            return (mainContent) => route.handler(mainContent, params, query)
        }
    }
    return notFoundRouteHandler
}

function matchRoute(template, path) {
    const templateParts = template.split("/")
    const pathParts = path.split("/")

    if (templateParts.length !== pathParts.length) return null

    const params = {}
    for (let i = 0; i < templateParts.length; i++) {
        if (templateParts[i].startsWith(":")) {
            params[templateParts[i].slice(1)] = pathParts[i]
        } else if (templateParts[i] !== pathParts[i]) {
            return null
        }
    }
    return params
}

function parseQuery(queryString) {
    if (!queryString) return {}
    return Object.fromEntries(
        queryString.split("&").map(pair => pair.split("="))
    )
}

function clearRoutes() {
    routes.length = 0
}

export default {
    addRouteHandler,
    getRouteHandler,
    addDefaultNotFoundRouteHandler,
    clearRoutes
}

