package com.hevy.sdk.model.routine

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.SetType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreateRoutineRequestTest {
    private val json = SdkJson.instance

    @Test
    fun serializesWithRoutineEnvelope() {
        val request =
            CreateRoutineRequest(
                routine =
                    CreateRoutineBody(
                        title = "PPL Day A",
                        folderId = 3,
                        notes = "Push focus",
                        exercises = emptyList(),
                    ),
            )

        val encoded = json.encodeToString(CreateRoutineRequest.serializer(), request)

        assertTrue(encoded.contains("\"routine\""))
        assertTrue(encoded.contains("\"folder_id\""))
        assertTrue(encoded.contains("\"PPL Day A\""))
    }

    @Test
    fun omitsFolderIdWhenNull() {
        val request =
            CreateRoutineRequest(
                routine =
                    CreateRoutineBody(
                        title = "Default Folder Routine",
                        folderId = null,
                        notes = null,
                        exercises = emptyList(),
                    ),
            )

        val encoded = json.encodeToString(CreateRoutineRequest.serializer(), request)

        assertFalse(encoded.contains("folder_id"), "Null folder_id should be omitted")
    }

    @Test
    fun serializesExercisesWithSetsAndRepRange() {
        val request =
            CreateRoutineRequest(
                routine =
                    CreateRoutineBody(
                        title = "Full Routine",
                        exercises =
                            listOf(
                                CreateRoutineExercise(
                                    exerciseTemplateId = "BP-001",
                                    supersetId = null,
                                    restSeconds = 90,
                                    notes = "Warm up",
                                    sets =
                                        listOf(
                                            CreateRoutineSet(
                                                type = SetType.NORMAL,
                                                weightKg = 80.0,
                                                reps = 10,
                                                repRange = RepRange(start = 8, end = 12),
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        val encoded = json.encodeToString(CreateRoutineRequest.serializer(), request)

        assertTrue(encoded.contains("\"exercise_template_id\""))
        assertTrue(encoded.contains("\"rest_seconds\""))
        assertTrue(encoded.contains("\"rep_range\""))
        assertTrue(encoded.contains("\"weight_kg\""))
    }

    @Test
    fun roundTripPreservesAllFields() {
        val original =
            CreateRoutineRequest(
                routine =
                    CreateRoutineBody(
                        title = "Test",
                        folderId = 1,
                        notes = "Notes",
                        exercises =
                            listOf(
                                CreateRoutineExercise(
                                    exerciseTemplateId = "EX-1",
                                    supersetId = 1,
                                    restSeconds = 60,
                                    notes = "Go heavy",
                                    sets =
                                        listOf(
                                            CreateRoutineSet(
                                                type = SetType.WARMUP,
                                                weightKg = 40.0,
                                                reps = 15,
                                            ),
                                        ),
                                ),
                            ),
                    ),
            )

        val encoded = json.encodeToString(CreateRoutineRequest.serializer(), original)
        val decoded = json.decodeFromString<CreateRoutineRequest>(encoded)

        assertEquals(original, decoded)
    }
}
