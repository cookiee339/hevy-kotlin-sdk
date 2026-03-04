package com.hevy.sdk.model.routine

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdateRoutineRequestTest {
    private val json = SdkJson.instance

    @Test
    fun serializesWithRoutineEnvelope() {
        val request =
            UpdateRoutineRequest(
                routine =
                    UpdateRoutineBody(
                        title = "Updated PPL",
                        notes = "Changed focus",
                        exercises = emptyList(),
                    ),
            )

        val encoded = json.encodeToString(UpdateRoutineRequest.serializer(), request)

        assertTrue(encoded.contains("\"routine\""))
        assertTrue(encoded.contains("\"Updated PPL\""))
    }

    @Test
    fun doesNotContainFolderId() {
        val request =
            UpdateRoutineRequest(
                routine =
                    UpdateRoutineBody(
                        title = "No Folder",
                        exercises = emptyList(),
                    ),
            )

        val encoded = json.encodeToString(UpdateRoutineRequest.serializer(), request)

        assertFalse(encoded.contains("folder_id"), "Update request must not contain folder_id")
    }

    @Test
    fun serializesExercisesWithSetsAndRepRange() {
        val request =
            UpdateRoutineRequest(
                routine =
                    UpdateRoutineBody(
                        title = "Updated",
                        exercises =
                            listOf(
                                CreateRoutineExercise(
                                    exerciseTemplateId = "BP-001",
                                    restSeconds = 120,
                                    sets =
                                        listOf(
                                            CreateRoutineSet(
                                                type = SetType.NORMAL,
                                                weightKg = 100.0,
                                                reps = 5,
                                                repRange = RepRange(start = null, end = null),
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        val encoded = json.encodeToString(UpdateRoutineRequest.serializer(), request)

        assertTrue(encoded.contains("\"exercise_template_id\""))
        assertTrue(encoded.contains("\"rep_range\""))
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            UpdateRoutineRequest(
                routine =
                    UpdateRoutineBody(
                        title = "Test",
                        notes = "Notes",
                        exercises =
                            listOf(
                                CreateRoutineExercise(
                                    exerciseTemplateId = "EX-1",
                                    supersetId = 2,
                                    restSeconds = 90,
                                    notes = "Focus",
                                    sets =
                                        listOf(
                                            CreateRoutineSet(
                                                type = SetType.FAILURE,
                                                weightKg = 60.0,
                                                reps = 8,
                                                repRange = RepRange(start = 6, end = 10),
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        val encoded = json.encodeToString(UpdateRoutineRequest.serializer(), original)
        val decoded = json.decodeFromString<UpdateRoutineRequest>(encoded)

        assertEquals(original, decoded)
    }
}
