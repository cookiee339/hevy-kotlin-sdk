package com.hevy.sdk

import com.hevy.sdk.api.ExerciseHistoryApi
import com.hevy.sdk.api.ExerciseTemplatesApi
import com.hevy.sdk.api.RoutineFoldersApi
import com.hevy.sdk.api.RoutinesApi
import com.hevy.sdk.api.UsersApi
import com.hevy.sdk.api.WorkoutsApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HevyClientTest {
    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    // -- construction --

    @Test
    fun createsClientWithApiKey() {
        val client = HevyClient(apiKey = "test-api-key-123")
        assertNotNull(client.workouts)
        assertNotNull(client.routines)
        assertNotNull(client.exerciseTemplates)
        assertNotNull(client.routineFolders)
        assertNotNull(client.exerciseHistory)
        assertNotNull(client.users)
        client.close()
    }

    @Test
    fun createsClientWithConfig() {
        val config = HevyClientConfig(apiKey = "test-key")
        val client = HevyClient(config)
        assertNotNull(client.workouts)
        client.close()
    }

    @Test
    fun rejectsBlankApiKey() {
        assertFailsWith<IllegalArgumentException> {
            HevyClient(apiKey = "")
        }
    }

    // -- domain API types --

    @Test
    fun workoutsPropertyReturnsWorkoutsApi() {
        val client = HevyClient(apiKey = "test-key")
        assertIs<WorkoutsApi>(client.workouts)
        client.close()
    }

    @Test
    fun routinesPropertyReturnsRoutinesApi() {
        val client = HevyClient(apiKey = "test-key")
        assertIs<RoutinesApi>(client.routines)
        client.close()
    }

    @Test
    fun exerciseTemplatesPropertyReturnsExerciseTemplatesApi() {
        val client = HevyClient(apiKey = "test-key")
        assertIs<ExerciseTemplatesApi>(client.exerciseTemplates)
        client.close()
    }

    @Test
    fun routineFoldersPropertyReturnsRoutineFoldersApi() {
        val client = HevyClient(apiKey = "test-key")
        assertIs<RoutineFoldersApi>(client.routineFolders)
        client.close()
    }

    @Test
    fun exerciseHistoryPropertyReturnsExerciseHistoryApi() {
        val client = HevyClient(apiKey = "test-key")
        assertIs<ExerciseHistoryApi>(client.exerciseHistory)
        client.close()
    }

    @Test
    fun usersPropertyReturnsUsersApi() {
        val client = HevyClient(apiKey = "test-key")
        assertIs<UsersApi>(client.users)
        client.close()
    }

    // -- integration: verify client wires through to API --

    @Test
    fun clientDelegatesWorkoutsListToApi() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"page": 1, "page_count": 1, "workouts": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val client = HevyClient.create(apiKey = "test-key", engine = engine)
            val page = client.workouts.list()

            assertEquals(1, page.page)
            assertEquals(0, page.items.size)
            client.close()
        }

    @Test
    fun clientDelegatesRoutinesListToApi() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"page": 1, "page_count": 1, "routines": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val client = HevyClient.create(apiKey = "test-key", engine = engine)
            val page = client.routines.list()

            assertEquals(1, page.page)
            assertEquals(0, page.items.size)
            client.close()
        }

    @Test
    fun clientDelegatesExerciseTemplatesListToApi() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """{"page": 1, "page_count": 1, "exercise_templates": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val client = HevyClient.create(apiKey = "test-key", engine = engine)
            val page = client.exerciseTemplates.list()

            assertEquals(1, page.page)
            assertEquals(0, page.items.size)
            client.close()
        }

    @Test
    fun clientDelegatesRoutineFoldersListToApi() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """{"page": 1, "page_count": 1, "routine_folders": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val client = HevyClient.create(apiKey = "test-key", engine = engine)
            val page = client.routineFolders.list()

            assertEquals(1, page.page)
            assertEquals(0, page.items.size)
            client.close()
        }

    @Test
    fun clientDelegatesUsersGetInfoToApi() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                                "data": {
                                    "id": "u-1",
                                    "name": "Test User",
                                    "url": "https://hevy.com/user/test"
                                }
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val client = HevyClient.create(apiKey = "test-key", engine = engine)
            val user = client.users.getInfo()

            assertEquals("Test User", user.name)
            client.close()
        }

    @Test
    fun clientDelegatesExerciseHistoryToApi() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"exercise_history": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val client = HevyClient.create(apiKey = "test-key", engine = engine)
            val history = client.exerciseHistory.getByTemplateId("template-1")

            assertTrue(history.isEmpty())
            client.close()
        }

    // -- close --

    @Test
    fun closeIsIdempotent() {
        val client = HevyClient(apiKey = "test-key")
        client.close()
        client.close() // should not throw
    }
}
