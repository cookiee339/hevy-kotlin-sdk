package com.hevy.sdk.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Default upper bound to prevent infinite pagination from a buggy server. */
private const val DEFAULT_MAX_PAGES = 1_000

/**
 * Auto-paginates through all pages, emitting each item as a [Flow] element.
 *
 * ```kotlin
 * val allWorkouts: Flow<Workout> = paginate { page ->
 *     workoutsApi.list(page = page, pageSize = 10)
 * }
 * ```
 *
 * @param startPage first page to fetch (must be >= 1).
 * @param maxPages upper bound on pages to fetch (guards against infinite loops from buggy servers).
 * @param fetchPage suspending function that fetches a single [Page].
 */
fun <T> paginate(
    startPage: Int = 1,
    maxPages: Int = DEFAULT_MAX_PAGES,
    fetchPage: suspend (page: Int) -> Page<T>,
): Flow<T> =
    flow {
        Validation.validatePage(startPage)
        require(maxPages >= 1) { "maxPages must be >= 1, was $maxPages" }

        var currentPage = startPage
        var fetched = 0
        do {
            val page = fetchPage(currentPage)
            page.items.forEach { emit(it) }
            currentPage++
            fetched++
        } while (page.hasNextPage && fetched < maxPages)
    }
