package com.hevy.sdk.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ValidationTest {
    // --- validatePage ---

    @Test
    fun validatePageAcceptsPositiveValues() {
        assertEquals(1, Validation.validatePage(1))
        assertEquals(100, Validation.validatePage(100))
    }

    @Test
    fun validatePageRejectsZero() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validatePage(0)
        }
    }

    @Test
    fun validatePageRejectsNegative() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validatePage(-1)
        }
    }

    // --- validatePageSize (default max) ---

    @Test
    fun validatePageSizeAcceptsValidRange() {
        assertEquals(1, Validation.validatePageSize(1))
        assertEquals(5, Validation.validatePageSize(5))
        assertEquals(10, Validation.validatePageSize(10))
    }

    @Test
    fun validatePageSizeRejectsZero() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validatePageSize(0)
        }
    }

    @Test
    fun validatePageSizeRejectsOverMax() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validatePageSize(11)
        }
    }

    // --- validatePageSize (custom max for exercise templates) ---

    @Test
    fun validatePageSizeAcceptsCustomMax() {
        assertEquals(
            100,
            Validation.validatePageSize(100, maxPageSize = ApiConstants.MAX_EXERCISE_TEMPLATE_PAGE_SIZE),
        )
    }

    @Test
    fun validatePageSizeRejectsOverCustomMax() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validatePageSize(101, maxPageSize = ApiConstants.MAX_EXERCISE_TEMPLATE_PAGE_SIZE)
        }
    }

    // --- validateId ---

    @Test
    fun validateIdAcceptsNonBlank() {
        assertEquals("abc-123", Validation.validateId("abc-123", "workoutId"))
    }

    @Test
    fun validateIdRejectsBlank() {
        val ex =
            assertFailsWith<IllegalArgumentException> {
                Validation.validateId("", "workoutId")
            }
        assertEquals("workoutId must not be blank", ex.message)
    }

    @Test
    fun validateIdRejectsWhitespaceOnly() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateId("   ", "routineId")
        }
    }

    @Test
    fun validateIdAcceptsUuid() {
        val uuid = "b459cba5-cd6d-463c-abd6-54f8eafcadcb"
        assertEquals(uuid, Validation.validateId(uuid, "workoutId"))
    }

    @Test
    fun validateIdAcceptsShortHexId() {
        assertEquals("D04AC939", Validation.validateId("D04AC939", "exerciseTemplateId"))
    }

    @Test
    fun validateIdRejectsPathTraversal() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateId("../admin", "workoutId")
        }
    }

    @Test
    fun validateIdRejectsSlash() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateId("abc/def", "workoutId")
        }
    }

    @Test
    fun validateIdRejectsQueryString() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateId("abc?key=val", "workoutId")
        }
    }

    // --- validateIntId ---

    @Test
    fun validateIntIdAcceptsPositiveValues() {
        assertEquals(1, Validation.validateIntId(1, "folderId"))
        assertEquals(42, Validation.validateIntId(42, "folderId"))
        assertEquals(Int.MAX_VALUE, Validation.validateIntId(Int.MAX_VALUE, "folderId"))
    }

    @Test
    fun validateIntIdRejectsZero() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateIntId(0, "folderId")
        }
    }

    @Test
    fun validateIntIdRejectsNegative() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateIntId(-1, "folderId")
        }
        assertFailsWith<IllegalArgumentException> {
            Validation.validateIntId(Int.MIN_VALUE, "folderId")
        }
    }

    @Test
    fun validateIntIdErrorMessageIncludesParamName() {
        val ex =
            assertFailsWith<IllegalArgumentException> {
                Validation.validateIntId(0, "folderId")
            }
        assertEquals("folderId must be positive, was 0", ex.message)
    }

    // --- validateTimestamp ---

    @Test
    fun validateTimestampAcceptsValidIso8601() {
        assertEquals(
            "2024-01-01T00:00:00Z",
            Validation.validateTimestamp("2024-01-01T00:00:00Z", "since"),
        )
    }

    @Test
    fun validateTimestampAcceptsEndOfYear() {
        assertEquals(
            "2024-12-31T23:59:59Z",
            Validation.validateTimestamp("2024-12-31T23:59:59Z", "endDate"),
        )
    }

    @Test
    fun validateTimestampRejectsBlank() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateTimestamp("", "since")
        }
    }

    @Test
    fun validateTimestampRejectsWhitespace() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateTimestamp("   ", "startDate")
        }
    }

    @Test
    fun validateTimestampRejectsDateOnly() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateTimestamp("2024-01-01", "since")
        }
    }

    @Test
    fun validateTimestampRejectsArbitraryString() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateTimestamp("not-a-date", "since")
        }
    }

    @Test
    fun validateTimestampRejectsUrlInjection() {
        assertFailsWith<IllegalArgumentException> {
            Validation.validateTimestamp("2024-01-01T00:00:00Z&extra=val", "since")
        }
    }

    @Test
    fun validateTimestampErrorMessageIncludesParamName() {
        val ex =
            assertFailsWith<IllegalArgumentException> {
                Validation.validateTimestamp("bad", "since")
            }
        assertTrue(ex.message!!.contains("since"))
        assertTrue(ex.message!!.contains("ISO 8601"))
    }
}
