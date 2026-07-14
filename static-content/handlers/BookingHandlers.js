import { fetchBookingById, fetchBookingsByUser, fetchBookingsByHouse, deleteBooking, updateBooking, createBooking } from "../api/bookingsApi.js"
import {DEFAULT_END_DATE, DEFAULT_SKIP, DEFAULT_START_DATE, PAGE_SIZE} from "../config/constants.js"
import { userBookingView, houseBookingView, bookingDetailsView, createBookingView, editBookingView } from "../views/BookingViews.js"

export async function getBookingsByHouse(mainContent, params, query) {
    const skip = query?.skip ? parseInt(query.skip) : DEFAULT_SKIP
    const limit = query?.limit ? parseInt(query.limit) : PAGE_SIZE
    const startDate = query?.startDate || DEFAULT_START_DATE
    const endDate = query?.endDate || DEFAULT_END_DATE

    const houseId = params?.houseId

    const bookings = await fetchBookingsByHouse({
        houseId,
        startDate,
        endDate,
        skip,
        limit
    })

    const hasNext = bookings.length > limit
    if (hasNext) bookings.pop()

    houseBookingView(
        bookings,
        mainContent,
        { houseId, skip, limit, startDate, endDate },
        hasNext
    )
}

export async function getBookingsByUser(mainContent, params, query) {
    const userId = params?.userId
    const skip = query?.skip ? parseInt(query.skip) : DEFAULT_SKIP
    const limit = query?.limit ? parseInt(query.limit) : PAGE_SIZE

    const bookings = await fetchBookingsByUser({ userId, skip, limit: limit + 1 })

    const hasNext = bookings.length > limit
    if (hasNext) bookings.pop()

    userBookingView(bookings, mainContent, { userId, skip, limit }, hasNext)
}

export async function getBookingDetails(mainContent, params) {
    const id = params?.id ? parseInt(params.id) : null
    const booking = await fetchBookingById(id)
    bookingDetailsView(booking, mainContent)
}

export function getCreateBooking(mainContent, params, query) {
    const houseId = params?.houseId || query?.houseId
    createBookingView(mainContent, { houseId })
}

export async function postCreateBooking(mainContent, params, query) {
    const { houseId, startDate, endDate } = query
    try {
        await createBooking({ houseId, startDate, endDate })
        window.location.hash = `#bookings/${houseId}/dates`
    } catch (e) {
        window.location.hash = `#bookings/create?houseId=${houseId}`
    }
}

export async function getEditBooking(mainContent, params) {
    const id      = params?.id ? parseInt(params.id) : null
    const booking = await fetchBookingById(id)
    editBookingView(booking, mainContent)
}

export async function postEditBooking(mainContent, params, query) {
    const id        = params?.id ? parseInt(params.id) : null
    const startDate = query?.startDate
    const endDate   = query?.endDate
    try {
        await updateBooking(id, { startDate, endDate })
        window.location.hash = `#bookings/${id}`
    } catch (e) {
        window.location.hash = `#bookings/${id}/edit`
    }
}

export async function handleDeleteBooking(mainContent, params) {
    const id = params?.id ? parseInt(params.id) : null
    try {
        await deleteBooking(id)
        window.location.hash = "#home"
    } catch (e) {
        window.location.hash = `#bookings/${id}`
    }
}