package com.hevy.sdk.model.workout

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutTest {
    private val json = SdkJson.instance

    private val sampleWorkoutJson =
        """
        {
            "id": "b459cba5-cd6d-463c-abd6-54f8eafcadcb",
            "title": "Morning Workout",
            "routine_id": "r-123",
            "description": "Pushed myself today!",
            "start_time": "2021-09-14T12:00:00Z",
            "end_time": "2021-09-14T13:00:00Z",
            "updated_at": "2021-09-14T13:05:00Z",
            "created_at": "2021-09-14T12:00:00Z",
            "exercises": [
                {
                    "index": 0,
                    "title": "Bench Press (Barbell)",
                    "notes": "Good form",
                    "exercise_template_id": "05293BCA",
                    "supersets_id": null,
                    "sets": [
                        {
                            "index": 0,
                            "type": "normal",
                            "weight_kg": 100.0,
                            "reps": 10,
                            "rpe": 8.5
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

    @Test
    fun deserializesFullWorkout() {
        val workout = json.decodeFromString<Workout>(sampleWorkoutJson)

        assertEquals("b459cba5-cd6d-463c-abd6-54f8eafcadcb", workout.id)
        assertEquals("Morning Workout", workout.title)
        assertEquals("r-123", workout.routineId)
        assertEquals("Pushed myself today!", workout.description)
        assertEquals("2021-09-14T12:00:00Z", workout.startTime)
        assertEquals("2021-09-14T13:00:00Z", workout.endTime)
        assertEquals("2021-09-14T13:05:00Z", workout.updatedAt)
        assertEquals("2021-09-14T12:00:00Z", workout.createdAt)
        assertEquals(1, workout.exercises.size)
    }

    @Test
    fun deserializesNestedExercisesAndSets() {
        val workout = json.decodeFromString<Workout>(sampleWorkoutJson)
        val exercise = workout.exercises[0]

        assertEquals("Bench Press (Barbell)", exercise.title)
        assertEquals("05293BCA", exercise.exerciseTemplateId)
        assertEquals(1, exercise.sets.size)

        val set = exercise.sets[0]
        assertEquals(SetType.NORMAL, set.type)
        assertEquals(100.0, set.weightKg)
        assertEquals(10, set.reps)
        assertEquals(8.5, set.rpe)
    }

    @Test
    fun deserializesWorkoutWithEmptyExercises() {
        val input =
            """
            {
                "id": "abc",
                "title": "Rest Day",
                "routine_id": "",
                "description": "",
                "start_time": "2021-09-14T12:00:00Z",
                "end_time": "2021-09-14T12:01:00Z",
                "updated_at": "2021-09-14T12:01:00Z",
                "created_at": "2021-09-14T12:00:00Z",
                "exercises": []
            }
            """.trimIndent()

        val workout = json.decodeFromString<Workout>(input)

        assertEquals(0, workout.exercises.size)
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            Workout(
                id = "test-id",
                title = "Test Workout",
                routineId = "routine-1",
                description = "A test",
                startTime = "2024-01-01T10:00:00Z",
                endTime = "2024-01-01T11:00:00Z",
                updatedAt = "2024-01-01T11:05:00Z",
                createdAt = "2024-01-01T10:00:00Z",
                exercises =
                    listOf(
                        WorkoutExercise(
                            index = 0,
                            title = "Squat",
                            notes = "",
                            exerciseTemplateId = "SQ-1",
                            supersetsId = null,
                            sets =
                                listOf(
                                    WorkoutSet(index = 0, type = SetType.NORMAL, weightKg = 140.0, reps = 5),
                                ),
                        ),
                    ),
            )

        val encoded = json.encodeToString(Workout.serializer(), original)
        val decoded = json.decodeFromString<Workout>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun deserializesWorkoutWithNullRoutineId() {
        val input =
            """
            {
                "id": "abc",
                "title": "Ad-hoc Workout",
                "routine_id": null,
                "description": "",
                "start_time": "2024-01-01T10:00:00Z",
                "end_time": "2024-01-01T11:00:00Z",
                "updated_at": "2024-01-01T11:00:00Z",
                "created_at": "2024-01-01T10:00:00Z",
                "exercises": []
            }
            """.trimIndent()

        val workout = json.decodeFromString<Workout>(input)

        assertNull(workout.routineId)
    }

    @Test
    fun serializesWithSnakeCaseFieldNames() {
        val workout =
            Workout(
                id = "id",
                title = "T",
                routineId = "r",
                description = "",
                startTime = "t",
                endTime = "t",
                updatedAt = "t",
                createdAt = "t",
                exercises = emptyList(),
            )

        val encoded = json.encodeToString(Workout.serializer(), workout)

        assertTrue(encoded.contains("\"routine_id\""))
        assertTrue(encoded.contains("\"start_time\""))
        assertTrue(encoded.contains("\"end_time\""))
        assertTrue(encoded.contains("\"updated_at\""))
        assertTrue(encoded.contains("\"created_at\""))
    }
}
