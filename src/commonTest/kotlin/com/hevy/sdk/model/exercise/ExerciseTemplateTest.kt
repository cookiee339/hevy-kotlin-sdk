package com.hevy.sdk.model.exercise

import com.hevy.sdk.common.SdkJson
import com.hevy.sdk.model.common.ExerciseType
import com.hevy.sdk.model.common.MuscleGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseTemplateTest {
    private val json = SdkJson.instance

    private val sampleJson =
        """
        {
            "id": "b459cba5-cd6d-463c-abd6-54f8eafcadcb",
            "title": "Bench Press (Barbell)",
            "type": "weight_reps",
            "primary_muscle_group": "chest",
            "secondary_muscle_groups": ["triceps", "shoulders"],
            "is_custom": false
        }
        """.trimIndent()

    @Test
    fun deserializesFullTemplate() {
        val template = json.decodeFromString<ExerciseTemplate>(sampleJson)

        assertEquals("b459cba5-cd6d-463c-abd6-54f8eafcadcb", template.id)
        assertEquals("Bench Press (Barbell)", template.title)
        assertEquals(ExerciseType.WEIGHT_REPS, template.type)
        assertEquals(MuscleGroup.CHEST, template.primaryMuscleGroup)
        assertEquals(
            listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
            template.secondaryMuscleGroups,
        )
        assertFalse(template.isCustom)
    }

    @Test
    fun deserializesCustomTemplate() {
        val customJson =
            """
            {
                "id": "custom-001",
                "title": "My Custom Exercise",
                "type": "reps_only",
                "primary_muscle_group": "biceps",
                "secondary_muscle_groups": [],
                "is_custom": true
            }
            """.trimIndent()

        val template = json.decodeFromString<ExerciseTemplate>(customJson)

        assertTrue(template.isCustom)
        assertEquals(ExerciseType.REPS_ONLY, template.type)
        assertEquals(MuscleGroup.BICEPS, template.primaryMuscleGroup)
        assertTrue(template.secondaryMuscleGroups.isEmpty())
    }

    @Test
    fun serializationRoundTrip() {
        val template = json.decodeFromString<ExerciseTemplate>(sampleJson)
        val serialized = json.encodeToString(ExerciseTemplate.serializer(), template)
        val deserialized = json.decodeFromString<ExerciseTemplate>(serialized)

        assertEquals(template, deserialized)
    }

    @Test
    fun ignoresUnknownFields() {
        val jsonWithExtra =
            """
            {
                "id": "t-1",
                "title": "Test",
                "type": "duration",
                "primary_muscle_group": "cardio",
                "secondary_muscle_groups": [],
                "is_custom": false,
                "some_future_field": "ignored"
            }
            """.trimIndent()

        val template = json.decodeFromString<ExerciseTemplate>(jsonWithExtra)
        assertEquals("t-1", template.id)
    }
}
