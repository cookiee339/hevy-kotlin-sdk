package com.hevy.sdk.model.routine

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineSetTest {
    private val json = SdkJson.instance

    @Test
    fun deserializesFullSetWithRepRange() {
        val input =
            """
            {
                "index": 0,
                "type": "normal",
                "weight_kg": 100.0,
                "reps": 10,
                "rep_range": {"start": 8, "end": 12},
                "distance_meters": null,
                "duration_seconds": null,
                "rpe": 9.0,
                "custom_metric": null
            }
            """.trimIndent()

        val set = json.decodeFromString<RoutineSet>(input)

        assertEquals(0, set.index)
        assertEquals(SetType.NORMAL, set.type)
        assertEquals(100.0, set.weightKg)
        assertEquals(10, set.reps)
        assertEquals(RepRange(start = 8, end = 12), set.repRange)
        assertNull(set.distanceMeters)
        assertNull(set.durationSeconds)
        assertEquals(9.0, set.rpe)
        assertNull(set.customMetric)
    }

    @Test
    fun deserializesSetWithNullRepRange() {
        val input =
            """
            {
                "index": 1,
                "type": "warmup",
                "rep_range": null
            }
            """.trimIndent()

        val set = json.decodeFromString<RoutineSet>(input)

        assertEquals(1, set.index)
        assertEquals(SetType.WARMUP, set.type)
        assertNull(set.repRange)
    }

    @Test
    fun deserializesSetWithAllNullableFieldsAbsent() {
        val input = """{"index": 2, "type": "dropset"}"""

        val set = json.decodeFromString<RoutineSet>(input)

        assertEquals(2, set.index)
        assertEquals(SetType.DROPSET, set.type)
        assertNull(set.weightKg)
        assertNull(set.reps)
        assertNull(set.repRange)
        assertNull(set.distanceMeters)
        assertNull(set.durationSeconds)
        assertNull(set.rpe)
        assertNull(set.customMetric)
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            RoutineSet(
                index = 0,
                type = SetType.NORMAL,
                weightKg = 80.0,
                reps = 10,
                repRange = RepRange(start = 8, end = 12),
                distanceMeters = null,
                durationSeconds = null,
                rpe = 8.5,
                customMetric = null,
            )

        val encoded = json.encodeToString(RoutineSet.serializer(), original)
        val decoded = json.decodeFromString<RoutineSet>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun serializesSnakeCaseFieldNames() {
        val set =
            RoutineSet(
                index = 0,
                type = SetType.NORMAL,
                weightKg = 100.0,
                reps = 10,
                repRange = RepRange(start = 8, end = 12),
                distanceMeters = 500,
                durationSeconds = 60,
                customMetric = 1.0,
            )

        val encoded = json.encodeToString(RoutineSet.serializer(), set)

        assertTrue(encoded.contains("\"weight_kg\""), "Expected snake_case weight_kg")
        assertTrue(encoded.contains("\"rep_range\""), "Expected snake_case rep_range")
        assertTrue(encoded.contains("\"distance_meters\""), "Expected snake_case distance_meters")
        assertTrue(encoded.contains("\"duration_seconds\""), "Expected snake_case duration_seconds")
        assertTrue(encoded.contains("\"custom_metric\""), "Expected snake_case custom_metric")
    }
}
