package com.hevy.sdk.api

import com.hevy.sdk.HevyClientConfig
import com.hevy.sdk.common.ApiConstants
import com.hevy.sdk.common.HttpClientFactory
import com.hevy.sdk.error.HevyException
import com.hevy.sdk.model.common.SetType
import com.hevy.sdk.model.routine.CreateRoutineBody
import com.hevy.sdk.model.routine.CreateRoutineExercise
import com.hevy.sdk.model.routine.CreateRoutineRequest
import com.hevy.sdk.model.routine.CreateRoutineSet
import com.hevy.sdk.model.routine.RepRange
import com.hevy.sdk.model.routine.UpdateRoutineBody
import com.hevy.sdk.model.routine.UpdateRoutineRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutinesApiTest {
    private val config = HevyClientConfig(apiKey = "test-key")

    private fun createApi(engine: MockEngine): RoutinesApi {
        val client = HttpClientFactory.create(config, engine)
        return RoutinesApi(client, ApiConstants.BASE_URL)
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    private val sampleRoutineJson =
        """
        {
            "id": "routine-1",
            "title": "Push Pull Legs",
            "folder_id": 42,
            "updated_at": "2024-06-15T10:00:00Z",
            "created_at": "2024-01-01T08:00:00Z",
            "exercises": [
                {
                    "index": 0,
                    "title": "Bench Press",
                    "rest_seconds": "90",
                    "notes": "Go heavy",
                    "exercise_template_id": "BP-001",
                    "supersets_id": null,
                    "sets": [
                        {
                            "index": 0,
                            "type": "normal",
                            "weight_kg": 100.0,
                            "reps": 10,
                            "rep_range": {"start": 8, "end": 12},
                            "rpe": 8.0
                        }
                    ]
                }
            ]
        }
        """.trimIndent()

    // -- list --

    @Test
    fun listReturnsPageOfRoutines() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                                "page": 1,
                                "page_count": 3,
                                "routines": [$sampleRoutineJson]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val page = api.list(page = 1, pageSize = 5)

            assertEquals(1, page.page)
            assertEquals(3, page.pageCount)
            assertEquals(1, page.items.size)
            assertEquals("routine-1", page.items[0].id)
            assertEquals("Push Pull Legs", page.items[0].title)
            assertEquals(42, page.items[0].folderId)
            assertEquals(1, page.items[0].exercises.size)
        }

    @Test
    fun listSendsCorrectQueryParameters() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = """{"page": 2, "page_count": 5, "routines": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.list(page = 2, pageSize = 10)

            assertTrue(capturedUrl.contains("page=2"), "Expected page=2 in URL: $capturedUrl")
            assertTrue(capturedUrl.contains("pageSize=10"), "Expected pageSize=10 in URL: $capturedUrl")
            assertTrue(capturedUrl.contains("/v1/routines"), "Expected /v1/routines in URL: $capturedUrl")
        }

    @Test
    fun listValidatesPageNumber() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.list(page = 0)
            }
        }

    @Test
    fun listValidatesPageSize() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.list(pageSize = 11)
            }
        }

    // -- create --

    @Test
    fun createSendsPostAndReturnsRoutine() =
        runTest {
            var capturedMethod: HttpMethod? = null
            val engine =
                MockEngine { request ->
                    capturedMethod = request.method
                    respond(
                        content = sampleRoutineJson,
                        status = HttpStatusCode.Created,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val request =
                CreateRoutineRequest(
                    routine =
                        CreateRoutineBody(
                            title = "Push Pull Legs",
                            folderId = 42,
                            notes = "Weekly split",
                            exercises =
                                listOf(
                                    CreateRoutineExercise(
                                        exerciseTemplateId = "BP-001",
                                        restSeconds = 90,
                                        sets =
                                            listOf(
                                                CreateRoutineSet(
                                                    type = SetType.NORMAL,
                                                    weightKg = 100.0,
                                                    reps = 10,
                                                    repRange = RepRange(start = 8, end = 12),
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                )

            val routine = api.create(request)

            assertEquals(HttpMethod.Post, capturedMethod)
            assertEquals("routine-1", routine.id)
            assertEquals("Push Pull Legs", routine.title)
        }

    @Test
    fun createValidatesBlankTitle() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.create(
                    CreateRoutineRequest(
                        routine =
                            CreateRoutineBody(
                                title = "  ",
                                exercises = emptyList(),
                            ),
                    ),
                )
            }
        }

    // -- get --

    @Test
    fun getReturnsRoutineById() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content =
                            """
                            {"routine": $sampleRoutineJson}
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val routine = api.get("routine-1")

            assertEquals("routine-1", routine.id)
            assertEquals("Push Pull Legs", routine.title)
            assertTrue(capturedUrl.contains("/v1/routines/routine-1"))
        }

    @Test
    fun getUnwrapsRoutineEnvelope() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                                "routine": {
                                    "id": "r-wrapped",
                                    "title": "Wrapped",
                                    "folder_id": null,
                                    "updated_at": "t",
                                    "created_at": "t",
                                    "exercises": []
                                }
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val routine = api.get("r-wrapped")

            assertEquals("r-wrapped", routine.id)
            assertEquals("Wrapped", routine.title)
            assertNull(routine.folderId)
        }

    @Test
    fun getValidatesRoutineId() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.get("")
            }
        }

    @Test
    fun getThrowsNotFoundOn404() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error": "Routine not found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.NotFound> {
                api.get("nonexistent-id")
            }
        }

    // -- update --

    @Test
    fun updateSendsPutAndReturnsRoutine() =
        runTest {
            var capturedMethod: HttpMethod? = null
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedMethod = request.method
                    capturedUrl = request.url.toString()
                    respond(
                        content =
                            """
                            {
                                "id": "routine-1",
                                "title": "Updated PPL",
                                "folder_id": 42,
                                "updated_at": "2024-06-16T10:00:00Z",
                                "created_at": "2024-01-01T08:00:00Z",
                                "exercises": []
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val request =
                UpdateRoutineRequest(
                    routine =
                        UpdateRoutineBody(
                            title = "Updated PPL",
                            notes = "Changed focus",
                            exercises = emptyList(),
                        ),
                )

            val routine = api.update("routine-1", request)

            assertEquals(HttpMethod.Put, capturedMethod)
            assertTrue(capturedUrl.contains("/v1/routines/routine-1"))
            assertEquals("Updated PPL", routine.title)
        }

    @Test
    fun updateValidatesBlankTitle() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.update(
                    "routine-1",
                    UpdateRoutineRequest(
                        routine =
                            UpdateRoutineBody(
                                title = "",
                                exercises = emptyList(),
                            ),
                    ),
                )
            }
        }

    @Test
    fun updateValidatesRoutineId() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)
            val request =
                UpdateRoutineRequest(
                    routine =
                        UpdateRoutineBody(
                            title = "T",
                            exercises = emptyList(),
                        ),
                )

            assertFailsWith<IllegalArgumentException> {
                api.update("", request)
            }
        }

    // -- error handling --

    @Test
    fun throwsUnauthorizedOn401() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error": "Unauthorized"}""",
                        status = HttpStatusCode.Unauthorized,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.Unauthorized> {
                api.list()
            }
        }

    @Test
    fun throwsRateLimitedOn429() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error": "Rate limited"}""",
                        status = HttpStatusCode.TooManyRequests,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.RateLimited> {
                api.list()
            }
        }

    @Test
    fun throwsUnknownOn403() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error": "Routine limit exceeded"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.Unknown> {
                api.create(
                    CreateRoutineRequest(
                        routine =
                            CreateRoutineBody(
                                title = "Too many",
                                exercises = emptyList(),
                            ),
                    ),
                )
            }
        }
}
