package com.hevy.sdk.common

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaginationUtilsTest {
    @Test
    fun paginateEmitsAllItemsAcrossPages() =
        runTest {
            val pages =
                listOf(
                    Page(page = 1, pageCount = 3, items = listOf("a", "b")),
                    Page(page = 2, pageCount = 3, items = listOf("c", "d")),
                    Page(page = 3, pageCount = 3, items = listOf("e")),
                )

            val items = paginate { page -> pages[page - 1] }.toList()

            assertEquals(listOf("a", "b", "c", "d", "e"), items)
        }

    @Test
    fun paginateStopsAfterLastPage() =
        runTest {
            var fetchCount = 0
            val items =
                paginate { page ->
                    fetchCount++
                    Page(page = page, pageCount = 2, items = listOf(page))
                }.toList()

            assertEquals(listOf(1, 2), items)
            assertEquals(2, fetchCount)
        }

    @Test
    fun paginateHandlesEmptyFirstPage() =
        runTest {
            val items =
                paginate { _ ->
                    Page(page = 1, pageCount = 0, items = emptyList<String>())
                }.toList()

            assertEquals(emptyList(), items)
        }

    @Test
    fun paginateHandlesSinglePage() =
        runTest {
            val items =
                paginate { _ ->
                    Page(page = 1, pageCount = 1, items = listOf(42))
                }.toList()

            assertEquals(listOf(42), items)
        }

    @Test
    fun paginateWithCustomStartPage() =
        runTest {
            val pages =
                mapOf(
                    3 to Page(page = 3, pageCount = 4, items = listOf("c")),
                    4 to Page(page = 4, pageCount = 4, items = listOf("d")),
                )

            val items = paginate(startPage = 3) { page -> pages.getValue(page) }.toList()

            assertEquals(listOf("c", "d"), items)
        }

    @Test
    fun paginateRejectsInvalidStartPage() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                paginate(startPage = 0) { _ ->
                    Page(page = 1, pageCount = 1, items = listOf("x"))
                }.toList()
            }
        }

    @Test
    fun paginateRejectsNegativeStartPage() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                paginate(startPage = -1) { _ ->
                    Page(page = 1, pageCount = 1, items = listOf("x"))
                }.toList()
            }
        }

    @Test
    fun paginateStopsAtMaxPages() =
        runTest {
            var fetchCount = 0
            val items =
                paginate(maxPages = 2) { page ->
                    fetchCount++
                    // Server always claims more pages
                    Page(page = page, pageCount = 999, items = listOf(page))
                }.toList()

            assertEquals(listOf(1, 2), items)
            assertEquals(2, fetchCount)
        }

    @Test
    fun paginatePropagatesExceptionFromFetchPage() =
        runTest {
            val flow =
                paginate { page ->
                    if (page == 2) throw RuntimeException("Server error on page 2")
                    Page(page = page, pageCount = 3, items = listOf(page))
                }

            val ex =
                assertFailsWith<RuntimeException> {
                    flow.toList()
                }
            assertEquals("Server error on page 2", ex.message)
        }

    @Test
    fun paginateCancellationStopsFetching() =
        runTest {
            var fetchCount = 0
            val items =
                paginate { page ->
                    fetchCount++
                    Page(page = page, pageCount = 100, items = listOf(page))
                }.take(3).toList()

            assertEquals(listOf(1, 2, 3), items)
            assertTrue(fetchCount <= 4, "Expected at most 4 fetches, got $fetchCount")
        }
}
