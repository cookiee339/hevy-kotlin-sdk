package com.hevy.sdk.common

/**
 * Input validation for API parameters.
 *
 * Validates at the SDK boundary so invalid requests never reach the network.
 */
internal object Validation {
    /** Validates and returns a page number (must be >= 1). */
    fun validatePage(page: Int): Int {
        require(page >= 1) { "page must be >= 1, was $page" }
        return page
    }

    /**
     * Validates and returns a page size.
     *
     * @param maxPageSize upper bound — defaults to [ApiConstants.MAX_PAGE_SIZE] (10),
     *   but exercise template endpoints allow up to [ApiConstants.MAX_EXERCISE_TEMPLATE_PAGE_SIZE] (100).
     */
    fun validatePageSize(
        pageSize: Int,
        maxPageSize: Int = ApiConstants.MAX_PAGE_SIZE,
    ): Int {
        require(pageSize in 1..maxPageSize) {
            "pageSize must be in 1..$maxPageSize, was $pageSize"
        }
        return pageSize
    }

    /**
     * Validates and returns a resource ID.
     *
     * IDs must not be blank and must only contain safe characters
     * (alphanumeric, hyphens). This prevents path traversal and
     * URL injection when IDs are interpolated into request paths.
     */
    fun validateId(
        id: String,
        paramName: String,
    ): String {
        require(id.isNotBlank()) { "$paramName must not be blank" }
        require(SAFE_ID_REGEX.matches(id)) {
            "$paramName contains invalid characters, was: $id"
        }
        return id
    }

    /** Validates and returns a positive integer ID (e.g. routine folder IDs). */
    fun validateIntId(
        id: Int,
        paramName: String,
    ): Int {
        require(id > 0) { "$paramName must be positive, was $id" }
        return id
    }

    /**
     * Validates that a timestamp string matches ISO 8601 format (e.g. "2024-01-01T00:00:00Z").
     *
     * Prevents malformed or malicious values from being sent as query parameters.
     */
    fun validateTimestamp(
        value: String,
        paramName: String,
    ): String {
        require(value.isNotBlank()) { "$paramName must not be blank" }
        require(ISO_8601_REGEX.matches(value)) {
            "$paramName must be ISO 8601 (e.g. 2024-01-01T00:00:00Z), was: $value"
        }
        return value
    }

    private val SAFE_ID_REGEX = Regex("^[A-Za-z0-9\\-]+$")
    private val ISO_8601_REGEX = Regex("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$""")
}
