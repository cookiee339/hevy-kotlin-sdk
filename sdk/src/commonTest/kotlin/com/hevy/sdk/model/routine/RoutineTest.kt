package com.hevy.sdk.model.routine

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineTest {
    private val json = SdkJson.instance

    private val sampleRoutineJson =
        """
        {
            "id": "routine-1",
            "title": "Push Pull Legs",
            "folder_id": 42,
            "updated_at": "2024-06-15T10:00:00Z",
            "created_at": "2024-01-01T08:00:00Z",
            "exercises": [
                {
                    "index": 0,
                    "title": "Bench Press (Barbell)",
                    "rest_seconds": "90",
                    "notes": "Warm up well",
                    "exercise_template_id": "BP-001",
                    "supersets_id": null,
                    "sets": [
                        {
                            "index": 0,
                            "type": "normal",
                            "weight_kg": 100.0,
                            "reps": 10,
                            "rep_range": {"start": 8, "end": 12},
                            "rpe": 8.0
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

    @Test
    fun deserializesFullRoutine() {
        val routine = json.decodeFromString<Routine>(sampleRoutineJson)

        assertEquals("routine-1", routine.id)
        assertEquals("Push Pull Legs", routine.title)
        assertEquals(42, routine.folderId)
        assertEquals("2024-06-15T10:00:00Z", routine.updatedAt)
        assertEquals("2024-01-01T08:00:00Z", routine.createdAt)
        assertEquals(1, routine.exercises.size)
    }

    @Test
    fun deserializesNestedExercisesAndSets() {
        val routine = json.decodeFromString<Routine>(sampleRoutineJson)
        val exercise = routine.exercises[0]

        assertEquals("Bench Press (Barbell)", exercise.title)
        assertEquals("90", exercise.restSeconds)
        assertEquals("Warm up well", exercise.notes)
        assertEquals("BP-001", exercise.exerciseTemplateId)
        assertNull(exercise.supersetsId)
        assertEquals(1, exercise.sets.size)

        val set = exercise.sets[0]
        assertEquals(SetType.NORMAL, set.type)
        assertEquals(100.0, set.weightKg)
        assertEquals(RepRange(start = 8, end = 12), set.repRange)
    }

    @Test
    fun deserializesExerciseWithMissingNotes() {
        val input =
            """
            {
                "id": "r-1",
                "title": "Minimal",
                "folder_id": null,
                "updated_at": "t",
                "created_at": "t",
                "exercises": [
                    {
                        "index": 0,
                        "title": "Squat",
                        "rest_seconds": "60",
                        "exercise_template_id": "SQ-1",
                        "sets": []
                    }
                ]
            }
            """.trimIndent()

        val routine = json.decodeFromString<Routine>(input)

        assertNull(routine.exercises[0].notes)
    }

    @Test
    fun deserializesRoutineWithNullFolderId() {
        val input =
            """
            {
                "id": "r-1",
                "title": "My Routine",
                "folder_id": null,
                "updated_at": "t",
                "created_at": "t",
                "exercises": []
            }
            """.trimIndent()

        val routine = json.decodeFromString<Routine>(input)

        assertNull(routine.folderId)
    }

    @Test
    fun deserializesRoutineWithEmptyExercises() {
        val input =
            """
            {
                "id": "r-1",
                "title": "Empty Routine",
                "folder_id": null,
                "updated_at": "t",
                "created_at": "t",
                "exercises": []
            }
            """.trimIndent()

        val routine = json.decodeFromString<Routine>(input)

        assertEquals(0, routine.exercises.size)
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            Routine(
                id = "r-1",
                title = "Test Routine",
                folderId = 5,
                updatedAt = "2024-01-01T10:00:00Z",
                createdAt = "2024-01-01T08:00:00Z",
                exercises =
                    listOf(
                        RoutineExercise(
                            index = 0,
                            title = "Squat",
                            restSeconds = "120",
                            notes = "",
                            exerciseTemplateId = "SQ-1",
                            supersetsId = null,
                            sets =
                                listOf(
                                    RoutineSet(
                                        index = 0,
                                        type = SetType.NORMAL,
                                        weightKg = 140.0,
                                        reps = 5,
                                        repRange = RepRange(start = 3, end = 5),
                                    ),
                                ),
                        ),
                    ),
            )

        val encoded = json.encodeToString(Routine.serializer(), original)
        val decoded = json.decodeFromString<Routine>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun serializesWithSnakeCaseFieldNames() {
        val routine =
            Routine(
                id = "id",
                title = "T",
                folderId = 1,
                updatedAt = "t",
                createdAt = "t",
                exercises = emptyList(),
            )

        val encoded = json.encodeToString(Routine.serializer(), routine)

        assertTrue(encoded.contains("\"folder_id\""))
        assertTrue(encoded.contains("\"updated_at\""))
        assertTrue(encoded.contains("\"created_at\""))
    }
}
