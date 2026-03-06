package com.hevy.sdk.model.folder

import com.hevy.sdk.common.SdkJson
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutineFolderTest {
    private val json = SdkJson.instance

    @Test
    fun deserializesFromJson() {
        val input =
            """
            {
                "id": 42,
                "index": 1,
                "title": "Push Pull",
                "updated_at": "2021-09-14T12:00:00Z",
                "created_at": "2021-09-14T12:00:00Z"
            }
            """.trimIndent()

        val folder = json.decodeFromString<RoutineFolder>(input)

        assertEquals(42, folder.id)
        assertEquals(1, folder.index)
        assertEquals("Push Pull", folder.title)
        assertEquals("2021-09-14T12:00:00Z", folder.updatedAt)
        assertEquals("2021-09-14T12:00:00Z", folder.createdAt)
    }

    @Test
    fun roundTripsSerializationCorrectly() {
        val folder =
            RoutineFolder(
                id = 7,
                index = 0,
                title = "Leg Day",
                updatedAt = "2024-06-01T08:00:00Z",
                createdAt = "2024-01-15T10:30:00Z",
            )

        val encoded = json.encodeToString(RoutineFolder.serializer(), folder)
        val decoded = json.decodeFromString<RoutineFolder>(encoded)

        assertEquals(folder, decoded)
    }
}
