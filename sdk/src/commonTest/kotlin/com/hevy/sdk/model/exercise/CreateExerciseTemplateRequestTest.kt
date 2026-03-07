package com.hevy.sdk.model.exercise

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.EquipmentCategory
import com.hevy.sdk.model.common.ExerciseType
import com.hevy.sdk.model.common.MuscleGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateExerciseTemplateRequestTest {
    private val json = SdkJson.instance

    @Test
    fun serializesRequestWithAllFields() {
        val request =
            CreateExerciseTemplateRequest(
                exercise =
                    CreateExerciseTemplateBody(
                        title = "My Press",
                        exerciseType = ExerciseType.WEIGHT_REPS,
                        equipmentCategory = EquipmentCategory.BARBELL,
                        muscleGroup = MuscleGroup.CHEST,
                        otherMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                    ),
            )

        val serialized = json.encodeToString(CreateExerciseTemplateRequest.serializer(), request)

        assertTrue(serialized.contains("\"title\":\"My Press\""))
        assertTrue(serialized.contains("\"exercise_type\":\"weight_reps\""))
        assertTrue(serialized.contains("\"equipment_category\":\"barbell\""))
        assertTrue(serialized.contains("\"muscle_group\":\"chest\""))
        assertTrue(serialized.contains("\"other_muscles\":[\"triceps\",\"shoulders\"]"))
    }

    @Test
    fun serializesRequestWithEmptyOtherMuscles() {
        val request =
            CreateExerciseTemplateRequest(
                exercise =
                    CreateExerciseTemplateBody(
                        title = "Curls",
                        exerciseType = ExerciseType.WEIGHT_REPS,
                        equipmentCategory = EquipmentCategory.DUMBBELL,
                        muscleGroup = MuscleGroup.BICEPS,
                    ),
            )

        val serialized = json.encodeToString(CreateExerciseTemplateRequest.serializer(), request)

        assertTrue(serialized.contains("\"other_muscles\":[]"))
    }

    @Test
    fun serializationRoundTrip() {
        val request =
            CreateExerciseTemplateRequest(
                exercise =
                    CreateExerciseTemplateBody(
                        title = "Plank",
                        exerciseType = ExerciseType.DURATION,
                        equipmentCategory = EquipmentCategory.NONE,
                        muscleGroup = MuscleGroup.ABDOMINALS,
                        otherMuscles = listOf(MuscleGroup.LOWER_BACK),
                    ),
            )

        val serialized = json.encodeToString(CreateExerciseTemplateRequest.serializer(), request)
        val deserialized =
            json.decodeFromString<CreateExerciseTemplateRequest>(serialized)

        assertEquals(request, deserialized)
    }

    @Test
    fun deserializesResponse() {
        val responseJson = """{"id": 123}"""
        val response =
            json.decodeFromString<CreateExerciseTemplateResponse>(responseJson)

        assertEquals(123, response.id)
    }
}
