package com.hevy.sdk.common

/**
 * Generic wrapper for paginated API responses.
 *
 * Not directly serializable — each API endpoint returns items under
 * a different JSON key (`workouts`, `routines`, etc.), so the API layer
 * constructs [Page] after deserializing the endpoint-specific response.
 *
 * @param T the item type contained in this page.
 */
data class Page<out T>(
    val page: Int,
    val pageCount: Int,
    val items: List<T>,
) {
    /** True when more pages are available after this one. */
    val hasNextPage: Boolean get() = page < pageCount

    /** Returns a new [Page] with items transformed by [transform]. */
    fun <R> map(transform: (T) -> R): Page<R> =
        Page(
            page = page,
            pageCount = pageCount,
            items = items.map(transform),
        )
}
