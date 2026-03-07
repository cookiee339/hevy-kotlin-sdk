package com.hevy.sdk.model.history

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExerciseHistoryEntryTest {
    private val json = SdkJson.instance

    @Test
    fun deserializesFullEntry() {
        val input =
            """
            {
                "workout_id": "abc-123",
                "workout_title": "Morning Workout",
                "workout_start_time": "2024-01-01T12:00:00Z",
                "workout_end_time": "2024-01-01T13:00:00Z",
                "exercise_template_id": "D04AC939",
                "weight_kg": 100.0,
                "reps": 10,
                "distance_meters": null,
                "duration_seconds": null,
                "rpe": 8.5,
                "custom_metric": null,
                "set_type": "normal"
            }
            """.trimIndent()

        val entry = json.decodeFromString<ExerciseHistoryEntry>(input)

        assertEquals("abc-123", entry.workoutId)
        assertEquals("Morning Workout", entry.workoutTitle)
        assertEquals("2024-01-01T12:00:00Z", entry.workoutStartTime)
        assertEquals("2024-01-01T13:00:00Z", entry.workoutEndTime)
        assertEquals("D04AC939", entry.exerciseTemplateId)
        assertEquals(100.0, entry.weightKg)
        assertEquals(10, entry.reps)
        assertNull(entry.distanceMeters)
        assertNull(entry.durationSeconds)
        assertEquals(8.5, entry.rpe)
        assertNull(entry.customMetric)
        assertEquals(SetType.NORMAL, entry.setType)
    }

    @Test
    fun deserializesWithNullableFieldsOmitted() {
        val input =
            """
            {
                "workout_id": "abc-123",
                "workout_title": "Run",
                "workout_start_time": "2024-06-01T07:00:00Z",
                "workout_end_time": "2024-06-01T07:30:00Z",
                "exercise_template_id": "RUNNING01",
                "distance_meters": 5000,
                "duration_seconds": 1800,
                "set_type": "normal"
            }
            """.trimIndent()

        val entry = json.decodeFromString<ExerciseHistoryEntry>(input)

        assertNull(entry.weightKg)
        assertNull(entry.reps)
        assertEquals(5000, entry.distanceMeters)
        assertEquals(1800, entry.durationSeconds)
        assertNull(entry.rpe)
        assertNull(entry.customMetric)
    }

    @Test
    fun roundTripsCorrectly() {
        val entry =
            ExerciseHistoryEntry(
                workoutId = "w-1",
                workoutTitle = "Test",
                workoutStartTime = "2024-01-01T00:00:00Z",
                workoutEndTime = "2024-01-01T01:00:00Z",
                exerciseTemplateId = "t-1",
                weightKg = 50.0,
                reps = 8,
                setType = SetType.WARMUP,
            )

        val encoded = json.encodeToString(ExerciseHistoryEntry.serializer(), entry)
        val decoded = json.decodeFromString<ExerciseHistoryEntry>(encoded)

        assertEquals(entry, decoded)
    }
}
