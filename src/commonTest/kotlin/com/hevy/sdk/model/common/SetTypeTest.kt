package com.hevy.sdk.model.common

import com.hevy.sdk.common.SdkJson
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class SetTypeTest {
    private val json = SdkJson.instance

    @Test
    fun serializesWarmupToSnakeCase() {
        val encoded = json.encodeToString(SetType.WARMUP)

        assertEquals("\"warmup\"", encoded)
    }

    @Test
    fun serializesNormalToSnakeCase() {
        val encoded = json.encodeToString(SetType.NORMAL)

        assertEquals("\"normal\"", encoded)
    }

    @Test
    fun serializesFailureToSnakeCase() {
        val encoded = json.encodeToString(SetType.FAILURE)

        assertEquals("\"failure\"", encoded)
    }

    @Test
    fun serializesDropsetToSnakeCase() {
        val encoded = json.encodeToString(SetType.DROPSET)

        assertEquals("\"dropset\"", encoded)
    }

    @Test
    fun deserializesFromSnakeCase() {
        val decoded = json.decodeFromString<SetType>("\"warmup\"")

        assertEquals(SetType.WARMUP, decoded)
    }

    @Test
    fun roundTripForAllValues() {
        SetType.entries.forEach { value ->
            val encoded = json.encodeToString(value)
            val decoded = json.decodeFromString<SetType>(encoded)

            assertEquals(value, decoded, "Round-trip failed for $value")
        }
    }

    @Test
    fun hasExactlyFourValues() {
        assertEquals(4, SetType.entries.size)
    }

    @Test
    fun allExpectedValuesPresent() {
        val expected = listOf("warmup", "normal", "failure", "dropset")

        val actual =
            SetType.entries
                .map { json.encodeToString(it).removeSurrounding("\"") }

        assertEquals(expected.sorted(), actual.sorted())
    }
}
