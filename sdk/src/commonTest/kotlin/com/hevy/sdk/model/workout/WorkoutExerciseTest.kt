package com.hevy.sdk.model.workout

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutExerciseTest {
    private val json = SdkJson.instance

    @Test
    fun deserializesFullExercise() {
        val input =
            """
            {
                "index": 0,
                "title": "Bench Press (Barbell)",
                "notes": "Felt great today!",
                "exercise_template_id": "05293BCA",
                "supersets_id": 1,
                "sets": [
                    {"index": 0, "type": "warmup", "weight_kg": 60.0, "reps": 10},
                    {"index": 1, "type": "normal", "weight_kg": 100.0, "reps": 8}
                ]
            }
            """.trimIndent()

        val exercise = json.decodeFromString<WorkoutExercise>(input)

        assertEquals(0, exercise.index)
        assertEquals("Bench Press (Barbell)", exercise.title)
        assertEquals("Felt great today!", exercise.notes)
        assertEquals("05293BCA", exercise.exerciseTemplateId)
        assertEquals(1, exercise.supersetsId)
        assertEquals(2, exercise.sets.size)
        assertEquals(SetType.WARMUP, exercise.sets[0].type)
        assertEquals(SetType.NORMAL, exercise.sets[1].type)
    }

    @Test
    fun deserializesExerciseWithNullSupersetsId() {
        val input =
            """
            {
                "index": 0,
                "title": "Squats",
                "notes": "",
                "exercise_template_id": "ABC123",
                "supersets_id": null,
                "sets": []
            }
            """.trimIndent()

        val exercise = json.decodeFromString<WorkoutExercise>(input)

        assertNull(exercise.supersetsId)
    }

    @Test
    fun deserializesExerciseWithNullNotes() {
        val input =
            """
            {
                "index": 0,
                "title": "Squats",
                "notes": null,
                "exercise_template_id": "ABC123",
                "supersets_id": null,
                "sets": []
            }
            """.trimIndent()

        val exercise = json.decodeFromString<WorkoutExercise>(input)

        assertNull(exercise.notes)
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            WorkoutExercise(
                index = 1,
                title = "Deadlift",
                notes = "PR attempt",
                exerciseTemplateId = "DEAD-LIFT-1",
                supersetsId = null,
                sets =
                    listOf(
                        WorkoutSet(index = 0, type = SetType.NORMAL, weightKg = 180.0, reps = 5),
                    ),
            )

        val encoded = json.encodeToString(WorkoutExercise.serializer(), original)
        val decoded = json.decodeFromString<WorkoutExercise>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun serializesWithSnakeCaseFieldNames() {
        val exercise =
            WorkoutExercise(
                index = 0,
                title = "Test",
                notes = "",
                exerciseTemplateId = "ID",
                supersetsId = 1,
                sets = emptyList(),
            )

        val encoded = json.encodeToString(WorkoutExercise.serializer(), exercise)

        assertTrue(encoded.contains("\"exercise_template_id\""))
        assertTrue(encoded.contains("\"supersets_id\""))
    }
}
