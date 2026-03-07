package com.hevy.sdk.model.user

import com.hevy.sdk.common.SdkJson
import kotlin.test.Test
import kotlin.test.assertEquals

class UserInfoTest {
    private val json = SdkJson.instance

    @Test
    fun deserializesFromJson() {
        val input =
            """
            {
                "id": "9c465af3-de7d-42bc-9c7c-f0170396358b",
                "name": "John Doe",
                "url": "https://hevy.com/user/john"
            }
            """.trimIndent()

        val user = json.decodeFromString<UserInfo>(input)

        assertEquals("9c465af3-de7d-42bc-9c7c-f0170396358b", user.id)
        assertEquals("John Doe", user.name)
        assertEquals("https://hevy.com/user/john", user.url)
    }

    @Test
    fun roundTripsCorrectly() {
        val user =
            UserInfo(
                id = "abc-123",
                name = "Jane",
                url = "https://hevy.com/user/jane",
            )

        val encoded = json.encodeToString(UserInfo.serializer(), user)
        val decoded = json.decodeFromString<UserInfo>(encoded)

        assertEquals(user, decoded)
    }
}
