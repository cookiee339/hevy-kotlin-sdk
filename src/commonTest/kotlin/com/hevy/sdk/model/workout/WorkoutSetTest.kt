package com.hevy.sdk.model.workout

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutSetTest {
    private val json = SdkJson.instance

    @Test
    fun deserializesFullSet() {
        val input =
            """
            {
                "index": 0,
                "type": "normal",
                "weight_kg": 100.0,
                "reps": 10.0,
                "distance_meters": null,
                "duration_seconds": null,
                "rpe": 9.5,
                "custom_metric": null
            }
            """.trimIndent()

        val set = json.decodeFromString<WorkoutSet>(input)

        assertEquals(0, set.index)
        assertEquals(SetType.NORMAL, set.type)
        assertEquals(100.0, set.weightKg)
        assertEquals(10.0, set.reps)
        assertNull(set.distanceMeters)
        assertNull(set.durationSeconds)
        assertEquals(9.5, set.rpe)
        assertNull(set.customMetric)
    }

    @Test
    fun deserializesSetWithAllNullableFieldsAbsent() {
        val input = """{"index": 1, "type": "warmup"}"""

        val set = json.decodeFromString<WorkoutSet>(input)

        assertEquals(1, set.index)
        assertEquals(SetType.WARMUP, set.type)
        assertNull(set.weightKg)
        assertNull(set.reps)
        assertNull(set.distanceMeters)
        assertNull(set.durationSeconds)
        assertNull(set.rpe)
        assertNull(set.customMetric)
    }

    @Test
    fun deserializesCardioSet() {
        val input =
            """
            {
                "index": 0,
                "type": "normal",
                "weight_kg": null,
                "reps": null,
                "distance_meters": 5000.0,
                "duration_seconds": 1800.0,
                "rpe": null,
                "custom_metric": 50.0
            }
            """.trimIndent()

        val set = json.decodeFromString<WorkoutSet>(input)

        assertEquals(5000.0, set.distanceMeters)
        assertEquals(1800.0, set.durationSeconds)
        assertEquals(50.0, set.customMetric)
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            WorkoutSet(
                index = 2,
                type = SetType.DROPSET,
                weightKg = 80.5,
                reps = 8.0,
                distanceMeters = null,
                durationSeconds = null,
                rpe = 8.5,
                customMetric = null,
            )

        val encoded = json.encodeToString(WorkoutSet.serializer(), original)
        val decoded = json.decodeFromString<WorkoutSet>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun serializesSnakeCaseFieldNames() {
        val set =
            WorkoutSet(
                index = 0,
                type = SetType.NORMAL,
                weightKg = 100.0,
                reps = 10.0,
            )

        val encoded = json.encodeToString(WorkoutSet.serializer(), set)

        assertTrue(encoded.contains("\"weight_kg\""), "Expected snake_case weight_kg")
        assertTrue(encoded.contains("\"custom_metric\""), "Expected snake_case custom_metric")
        assertTrue(encoded.contains("\"distance_meters\""), "Expected snake_case distance_meters")
        assertTrue(encoded.contains("\"duration_seconds\""), "Expected snake_case duration_seconds")
    }
}
