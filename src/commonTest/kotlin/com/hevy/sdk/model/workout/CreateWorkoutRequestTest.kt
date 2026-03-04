package com.hevy.sdk.model.workout

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateWorkoutRequestTest {
    private val json = SdkJson.instance

    @Test
    fun serializesFullRequest() {
        val request =
            CreateWorkoutRequest(
                workout =
                    CreateWorkoutBody(
                        title = "Friday Leg Day",
                        description = "Quad focus",
                        startTime = "2024-08-14T12:00:00Z",
                        endTime = "2024-08-14T12:30:00Z",
                        isPrivate = false,
                        exercises =
                            listOf(
                                CreateWorkoutExercise(
                                    exerciseTemplateId = "D04AC939",
                                    supersetId = null,
                                    notes = "Felt good",
                                    sets =
                                        listOf(
                                            CreateWorkoutSet(
                                                type = SetType.NORMAL,
                                                weightKg = 100.0,
                                                reps = 10,
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        val encoded = json.encodeToString(CreateWorkoutRequest.serializer(), request)

        assertTrue(encoded.contains("\"workout\""), "Should wrap in workout envelope")
        assertTrue(encoded.contains("\"start_time\""))
        assertTrue(encoded.contains("\"end_time\""))
        assertTrue(encoded.contains("\"is_private\""))
        assertTrue(encoded.contains("\"exercise_template_id\""))
        assertTrue(encoded.contains("\"superset_id\""))
        assertTrue(encoded.contains("\"weight_kg\""))
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            CreateWorkoutRequest(
                workout =
                    CreateWorkoutBody(
                        title = "Test",
                        description = "Desc",
                        startTime = "2024-01-01T10:00:00Z",
                        endTime = "2024-01-01T11:00:00Z",
                        isPrivate = true,
                        exercises =
                            listOf(
                                CreateWorkoutExercise(
                                    exerciseTemplateId = "TPL-1",
                                    supersetId = 1,
                                    notes = "Notes",
                                    sets =
                                        listOf(
                                            CreateWorkoutSet(
                                                type = SetType.WARMUP,
                                                weightKg = 60.0,
                                                reps = 12,
                                                distanceMeters = null,
                                                durationSeconds = null,
                                                customMetric = null,
                                                rpe = 6.0,
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        val encoded = json.encodeToString(CreateWorkoutRequest.serializer(), original)
        val decoded = json.decodeFromString<CreateWorkoutRequest>(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun deserializesRequestSet() {
        val input =
            """
            {
                "type": "normal",
                "weight_kg": 100.0,
                "reps": 10,
                "distance_meters": null,
                "duration_seconds": null,
                "custom_metric": null,
                "rpe": null
            }
            """.trimIndent()

        val set = json.decodeFromString<CreateWorkoutSet>(input)

        assertEquals(SetType.NORMAL, set.type)
        assertEquals(100.0, set.weightKg)
        assertEquals(10, set.reps)
        assertNull(set.distanceMeters)
        assertNull(set.rpe)
    }

    @Test
    fun requestSetUsesIntForRepsAndDistance() {
        val set =
            CreateWorkoutSet(
                type = SetType.NORMAL,
                reps = 10,
                distanceMeters = 5000,
                durationSeconds = 1800,
            )

        val encoded = json.encodeToString(CreateWorkoutSet.serializer(), set)
        val decoded = json.decodeFromString<CreateWorkoutSet>(encoded)

        assertEquals(10, decoded.reps)
        assertEquals(5000, decoded.distanceMeters)
        assertEquals(1800, decoded.durationSeconds)
    }

    @Test
    fun defaultsOptionalFieldsToNull() {
        val set = CreateWorkoutSet(type = SetType.NORMAL)

        assertNull(set.weightKg)
        assertNull(set.reps)
        assertNull(set.distanceMeters)
        assertNull(set.durationSeconds)
        assertNull(set.customMetric)
        assertNull(set.rpe)
    }

    @Test
    fun defaultsIsPrivateToFalse() {
        val body =
            CreateWorkoutBody(
                title = "Test",
                startTime = "t",
                endTime = "t",
                exercises = emptyList(),
            )

        assertEquals(false, body.isPrivate)
    }

    @Test
    fun defaultsDescriptionToNull() {
        val body =
            CreateWorkoutBody(
                title = "Test",
                startTime = "t",
                endTime = "t",
                exercises = emptyList(),
            )

        assertNull(body.description)
    }
}
