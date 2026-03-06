package com.hevy.sdk.model.common

import com.hevy.sdk.common.SdkJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
private data class EquipmentCategoryWrapper(
    @SerialName("category") val category: EquipmentCategory = EquipmentCategory.UNKNOWN,
)

class EquipmentCategoryTest {
    private val json = SdkJson.instance

    @Test
    fun serializesNoneToSnakeCase() {
        val encoded = json.encodeToString(EquipmentCategory.NONE)

        assertEquals("\"none\"", encoded)
    }

    @Test
    fun serializesResistanceBandToSnakeCase() {
        val encoded = json.encodeToString(EquipmentCategory.RESISTANCE_BAND)

        assertEquals("\"resistance_band\"", encoded)
    }

    @Test
    fun deserializesFromSnakeCase() {
        val decoded = json.decodeFromString<EquipmentCategory>("\"kettlebell\"")

        assertEquals(EquipmentCategory.KETTLEBELL, decoded)
    }

    @Test
    fun roundTripForAllValues() {
        EquipmentCategory.entries.forEach { value ->
            val encoded = json.encodeToString(value)
            val decoded = json.decodeFromString<EquipmentCategory>(encoded)

            assertEquals(value, decoded, "Round-trip failed for $value")
        }
    }

    @Test
    fun unknownValueCoercesToDefault() {
        val input = """{"category": "new_future_category"}"""
        val wrapper = json.decodeFromString<EquipmentCategoryWrapper>(input)

        assertEquals(EquipmentCategory.UNKNOWN, wrapper.category)
    }

    @Test
    fun hasExactlyTenValues() {
        assertEquals(10, EquipmentCategory.entries.size)
    }

    @Test
    fun allExpectedValuesPresent() {
        val expected =
            listOf(
                "none", "barbell", "dumbbell", "kettlebell", "machine",
                "plate", "resistance_band", "suspension", "other",
            )

        val actual =
            EquipmentCategory.entries
                .filter { it != EquipmentCategory.UNKNOWN }
                .map { json.encodeToString(it).removeSurrounding("\"") }

        assertEquals(expected.sorted(), actual.sorted())
    }
}
