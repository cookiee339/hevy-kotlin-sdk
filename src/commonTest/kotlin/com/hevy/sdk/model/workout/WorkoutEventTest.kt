package com.hevy.sdk.model.workout

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WorkoutEventTest {
    private val json = SdkJson.instance

    @Test
    fun deserializesUpdatedEvent() {
        val input =
            """
            {
                "type": "updated",
                "workout": {
                    "id": "w-1",
                    "title": "Morning Workout",
                    "routine_id": "r-1",
                    "description": "",
                    "start_time": "2021-09-14T12:00:00Z",
                    "end_time": "2021-09-14T13:00:00Z",
                    "updated_at": "2021-09-14T13:05:00Z",
                    "created_at": "2021-09-14T12:00:00Z",
                    "exercises": []
                }
            }
            """.trimIndent()

        val event = json.decodeFromString<WorkoutEvent>(input)

        val updated = assertIs<WorkoutEvent.Updated>(event)
        assertEquals("w-1", updated.workout.id)
        assertEquals("Morning Workout", updated.workout.title)
    }

    @Test
    fun deserializesDeletedEvent() {
        val input =
            """
            {
                "type": "deleted",
                "id": "efe6801c-4aee-4959-bcdd-fca3f272821b",
                "deleted_at": "2021-09-13T12:00:00Z"
            }
            """.trimIndent()

        val event = json.decodeFromString<WorkoutEvent>(input)

        val deleted = assertIs<WorkoutEvent.Deleted>(event)
        assertEquals("efe6801c-4aee-4959-bcdd-fca3f272821b", deleted.id)
        assertEquals("2021-09-13T12:00:00Z", deleted.deletedAt)
    }

    @Test
    fun deserializesUpdatedEventWithExercises() {
        val input =
            """
            {
                "type": "updated",
                "workout": {
                    "id": "w-2",
                    "title": "Leg Day",
                    "routine_id": "",
                    "description": "Heavy squats",
                    "start_time": "2024-01-01T10:00:00Z",
                    "end_time": "2024-01-01T11:00:00Z",
                    "updated_at": "2024-01-01T11:05:00Z",
                    "created_at": "2024-01-01T10:00:00Z",
                    "exercises": [
                        {
                            "index": 0,
                            "title": "Squat",
                            "notes": "",
                            "exercise_template_id": "SQ-1",
                            "supersets_id": null,
                            "sets": [
                                {"index": 0, "type": "normal", "weight_kg": 140.0, "reps": 5}
                            ]
                        }
                    ]
                }
            }
            """.trimIndent()

        val event = json.decodeFromString<WorkoutEvent>(input)

        val updated = assertIs<WorkoutEvent.Updated>(event)
        assertEquals(1, updated.workout.exercises.size)
        assertEquals(SetType.NORMAL, updated.workout.exercises[0].sets[0].type)
    }

    @Test
    fun roundTripUpdatedEvent() {
        val original =
            WorkoutEvent.Updated(
                workout =
                    Workout(
                        id = "test",
                        title = "Test",
                        routineId = "",
                        description = "",
                        startTime = "t",
                        endTime = "t",
                        updatedAt = "t",
                        createdAt = "t",
                        exercises = emptyList(),
                    ),
            )

        val encoded = json.encodeToString(WorkoutEvent.serializer(), original)
        val decoded = json.decodeFromString<WorkoutEvent>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun roundTripDeletedEvent() {
        val original =
            WorkoutEvent.Deleted(
                id = "del-1",
                deletedAt = "2024-01-01T00:00:00Z",
            )

        val encoded = json.encodeToString(WorkoutEvent.serializer(), original)
        val decoded = json.decodeFromString<WorkoutEvent>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun deserializesListOfMixedEvents() {
        val input =
            """
            [
                {
                    "type": "updated",
                    "workout": {
                        "id": "w-1",
                        "title": "Updated Workout",
                        "routine_id": "",
                        "description": "",
                        "start_time": "t",
                        "end_time": "t",
                        "updated_at": "t",
                        "created_at": "t",
                        "exercises": []
                    }
                },
                {
                    "type": "deleted",
                    "id": "w-2",
                    "deleted_at": "2024-01-01T00:00:00Z"
                }
            ]
            """.trimIndent()

        val events = json.decodeFromString<List<WorkoutEvent>>(input)

        assertEquals(2, events.size)
        assertIs<WorkoutEvent.Updated>(events[0])
        assertIs<WorkoutEvent.Deleted>(events[1])
    }
}
