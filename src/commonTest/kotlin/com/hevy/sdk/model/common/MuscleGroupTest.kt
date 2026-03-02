package com.hevy.sdk.model.common

import com.hevy.sdk.common.SdkJson
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class MuscleGroupTest {
    private val json = SdkJson.instance

    @Test
    fun serializesAbdominalsToSnakeCase() {
        val encoded = json.encodeToString(MuscleGroup.ABDOMINALS)

        assertEquals("\"abdominals\"", encoded)
    }

    @Test
    fun serializesUpperBackToSnakeCase() {
        val encoded = json.encodeToString(MuscleGroup.UPPER_BACK)

        assertEquals("\"upper_back\"", encoded)
    }

    @Test
    fun serializesLowerBackToSnakeCase() {
        val encoded = json.encodeToString(MuscleGroup.LOWER_BACK)

        assertEquals("\"lower_back\"", encoded)
    }

    @Test
    fun serializesFullBodyToSnakeCase() {
        val encoded = json.encodeToString(MuscleGroup.FULL_BODY)

        assertEquals("\"full_body\"", encoded)
    }

    @Test
    fun deserializesFromSnakeCase() {
        val decoded = json.decodeFromString<MuscleGroup>("\"quadriceps\"")

        assertEquals(MuscleGroup.QUADRICEPS, decoded)
    }

    @Test
    fun roundTripForAllValues() {
        MuscleGroup.entries.forEach { value ->
            val encoded = json.encodeToString(value)
            val decoded = json.decodeFromString<MuscleGroup>(encoded)

            assertEquals(value, decoded, "Round-trip failed for $value")
        }
    }

    @Test
    fun hasExactlyTwentyValues() {
        assertEquals(20, MuscleGroup.entries.size)
    }

    @Test
    fun allExpectedValuesPresent() {
        val expected = listOf(
            "abdominals", "shoulders", "biceps", "triceps", "forearms",
            "quadriceps", "hamstrings", "calves", "glutes", "abductors",
            "adductors", "lats", "upper_back", "traps", "lower_back",
            "chest", "cardio", "neck", "full_body", "other",
        )

        val actual = MuscleGroup.entries
            .map { json.encodeToString(it).removeSurrounding("\"") }

        assertEquals(expected.sorted(), actual.sorted())
    }
}
