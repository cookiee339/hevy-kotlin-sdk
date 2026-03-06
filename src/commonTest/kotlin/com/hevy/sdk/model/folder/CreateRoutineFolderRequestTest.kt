package com.hevy.sdk.model.folder

import com.hevy.sdk.common.SdkJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateRoutineFolderRequestTest {
    private val json = SdkJson.instance

    @Test
    fun serializesWithNestedWrapper() {
        val request =
            CreateRoutineFolderRequest(
                routineFolder = CreateRoutineFolderBody(title = "Push Pull"),
            )

        val encoded = json.encodeToString(CreateRoutineFolderRequest.serializer(), request)

        assertTrue(encoded.contains("\"routine_folder\""), "Expected routine_folder key")
        assertTrue(encoded.contains("\"title\""), "Expected title key")
        assertTrue(encoded.contains("Push Pull"), "Expected title value")
    }

    @Test
    fun roundTripsCorrectly() {
        val request =
            CreateRoutineFolderRequest(
                routineFolder = CreateRoutineFolderBody(title = "Leg Day"),
            )

        val encoded = json.encodeToString(CreateRoutineFolderRequest.serializer(), request)
        val decoded = json.decodeFromString<CreateRoutineFolderRequest>(encoded)

        assertEquals(request, decoded)
    }
}
