export const API_BASE_URL = " "
export const PAGE_SIZE = 5
export const DEFAULT_SKIP = 0
export const DEV_AUTH_TOKEN = "e3b2c1a0-d4ef-4cba-bc1a-2b3c4d5e6f7a";
export const DEFAULT_START_DATE = new Date('2024-01-01').toISOString().split('T')[0];
export const DEFAULT_END_DATE = new Date('2028-01-01').toISOString().split('T')[0];
export const ERROR_MESSAGES = {
    400: "Invalid request. Please check your inputs.",
    401: "You must be logged in to view this.",
    404: "Sorry, we couldn't find what you're looking for.",
    500: "Our servers are down. Please try again later.",
};