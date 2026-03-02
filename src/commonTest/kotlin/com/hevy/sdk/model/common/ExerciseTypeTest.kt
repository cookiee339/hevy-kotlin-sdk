package com.hevy.sdk.model.common

import com.hevy.sdk.common.SdkJson
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class ExerciseTypeTest {
    private val json = SdkJson.instance

    @Test
    fun serializesWeightRepsToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.WEIGHT_REPS)

        assertEquals("\"weight_reps\"", encoded)
    }

    @Test
    fun serializesRepsOnlyToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.REPS_ONLY)

        assertEquals("\"reps_only\"", encoded)
    }

    @Test
    fun serializesBodyweightRepsToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.BODYWEIGHT_REPS)

        assertEquals("\"bodyweight_reps\"", encoded)
    }

    @Test
    fun serializesBodyweightAssistedRepsToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.BODYWEIGHT_ASSISTED_REPS)

        assertEquals("\"bodyweight_assisted_reps\"", encoded)
    }

    @Test
    fun serializesDurationToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.DURATION)

        assertEquals("\"duration\"", encoded)
    }

    @Test
    fun serializesWeightDurationToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.WEIGHT_DURATION)

        assertEquals("\"weight_duration\"", encoded)
    }

    @Test
    fun serializesDistanceDurationToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.DISTANCE_DURATION)

        assertEquals("\"distance_duration\"", encoded)
    }

    @Test
    fun serializesShortDistanceWeightToSnakeCase() {
        val encoded = json.encodeToString(ExerciseType.SHORT_DISTANCE_WEIGHT)

        assertEquals("\"short_distance_weight\"", encoded)
    }

    @Test
    fun deserializesFromSnakeCase() {
        val decoded = json.decodeFromString<ExerciseType>("\"bodyweight_assisted_reps\"")

        assertEquals(ExerciseType.BODYWEIGHT_ASSISTED_REPS, decoded)
    }

    @Test
    fun roundTripForAllValues() {
        ExerciseType.entries.forEach { value ->
            val encoded = json.encodeToString(value)
            val decoded = json.decodeFromString<ExerciseType>(encoded)

            assertEquals(value, decoded, "Round-trip failed for $value")
        }
    }

    @Test
    fun hasExactlyEightValues() {
        assertEquals(8, ExerciseType.entries.size)
    }

    @Test
    fun allExpectedValuesPresent() {
        val expected = listOf(
            "weight_reps", "reps_only", "bodyweight_reps", "bodyweight_assisted_reps",
            "duration", "weight_duration", "distance_duration", "short_distance_weight",
        )

        val actual = ExerciseType.entries
            .map { json.encodeToString(it).removeSurrounding("\"") }

        assertEquals(expected.sorted(), actual.sorted())
    }
}
