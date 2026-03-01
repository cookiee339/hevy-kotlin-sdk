package com.hevy.sdk.common

internal object ApiConstants {
    const val BASE_URL = "https://api.hevyapp.com"

    /** Header name for API key authentication. See hevy_openapi.yaml parameters. */
    const val API_KEY_HEADER = "api-key"

    const val DEFAULT_PAGE = 1

    /** Matches the Hevy API's documented default; max is 10 for most endpoints. */
    const val DEFAULT_PAGE_SIZE = 5

    const val MAX_PAGE_SIZE = 10
    const val MAX_EXERCISE_TEMPLATE_PAGE_SIZE = 100
}
