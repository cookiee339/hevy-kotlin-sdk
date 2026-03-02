package com.hevy.sdk.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PageTest {
    @Test
    fun hasNextPageWhenPageLessThanPageCount() {
        val page = Page(page = 1, pageCount = 3, items = listOf("a", "b"))

        assertTrue(page.hasNextPage)
    }

    @Test
    fun noNextPageWhenPageEqualsPageCount() {
        val page = Page(page = 3, pageCount = 3, items = listOf("a"))

        assertFalse(page.hasNextPage)
    }

    @Test
    fun noNextPageWhenPageCountIsZero() {
        val page = Page(page = 1, pageCount = 0, items = emptyList<String>())

        assertFalse(page.hasNextPage)
    }

    @Test
    fun emptyPageHasNoItems() {
        val page = Page(page = 1, pageCount = 1, items = emptyList<Int>())

        assertTrue(page.items.isEmpty())
    }

    @Test
    fun mapTransformsItems() {
        val page = Page(page = 1, pageCount = 2, items = listOf(1, 2, 3))

        val mapped = page.map { it * 10 }

        assertEquals(listOf(10, 20, 30), mapped.items)
        assertEquals(1, mapped.page)
        assertEquals(2, mapped.pageCount)
    }
}
