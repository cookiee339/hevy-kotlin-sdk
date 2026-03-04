package com.hevy.sdk.api

import com.hevy.sdk.HevyClientConfig
import com.hevy.sdk.common.ApiConstants
import com.hevy.sdk.common.HttpClientFactory
import com.hevy.sdk.error.HevyException
import com.hevy.sdk.model.common.SetType
import com.hevy.sdk.model.workout.CreateWorkoutBody
import com.hevy.sdk.model.workout.CreateWorkoutExercise
import com.hevy.sdk.model.workout.CreateWorkoutRequest
import com.hevy.sdk.model.workout.CreateWorkoutSet
import com.hevy.sdk.model.workout.WorkoutEvent
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WorkoutsApiTest {
    private val config = HevyClientConfig(apiKey = "test-key")

    private fun createApi(engine: MockEngine): WorkoutsApi {
        val client = HttpClientFactory.create(config, engine)
        return WorkoutsApi(client, ApiConstants.BASE_URL)
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    // -- list --

    @Test
    fun listReturnsPageOfWorkouts() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                                "page": 1,
                                "page_count": 2,
                                "workouts": [
                                    {
                                        "id": "w-1",
                                        "title": "Workout 1",
                                        "routine_id": "",
                                        "description": "",
                                        "start_time": "2024-01-01T10:00:00Z",
                                        "end_time": "2024-01-01T11:00:00Z",
                                        "updated_at": "2024-01-01T11:00:00Z",
                                        "created_at": "2024-01-01T10:00:00Z",
                                        "exercises": []
                                    }
                                ]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val page = api.list(page = 1, pageSize = 5)

            assertEquals(1, page.page)
            assertEquals(2, page.pageCount)
            assertEquals(1, page.items.size)
            assertEquals("w-1", page.items[0].id)
            assertEquals("Workout 1", page.items[0].title)
        }

    @Test
    fun listSendsCorrectQueryParameters() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = """{"page": 2, "page_count": 3, "workouts": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.list(page = 2, pageSize = 10)

            assertTrue(capturedUrl.contains("page=2"), "Expected page=2 in URL: $capturedUrl")
            assertTrue(capturedUrl.contains("pageSize=10"), "Expected pageSize=10 in URL: $capturedUrl")
            assertTrue(capturedUrl.contains("/v1/workouts"), "Expected /v1/workouts in URL: $capturedUrl")
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
    fun createSendsPostAndReturnsWorkout() =
        runTest {
            var capturedMethod: HttpMethod? = null
            val engine =
                MockEngine { request ->
                    capturedMethod = request.method
                    respond(
                        content =
                            """
                            {
                                "id": "new-1",
                                "title": "Friday Leg Day",
                                "routine_id": "",
                                "description": "Quad focus",
                                "start_time": "2024-08-14T12:00:00Z",
                                "end_time": "2024-08-14T12:30:00Z",
                                "updated_at": "2024-08-14T12:30:00Z",
                                "created_at": "2024-08-14T12:00:00Z",
                                "exercises": []
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.Created,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val request =
                CreateWorkoutRequest(
                    workout =
                        CreateWorkoutBody(
                            title = "Friday Leg Day",
                            description = "Quad focus",
                            startTime = "2024-08-14T12:00:00Z",
                            endTime = "2024-08-14T12:30:00Z",
                            exercises =
                                listOf(
                                    CreateWorkoutExercise(
                                        exerciseTemplateId = "D04AC939",
                                        sets =
                                            listOf(
                                                CreateWorkoutSet(type = SetType.NORMAL, weightKg = 100.0, reps = 10),
                                            ),
                                    ),
                                ),
                        ),
                )

            val workout = api.create(request)

            assertEquals(HttpMethod.Post, capturedMethod)
            assertEquals("new-1", workout.id)
            assertEquals("Friday Leg Day", workout.title)
        }

    // -- count --

    @Test
    fun countReturnsWorkoutCount() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"workout_count": 42}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val count = api.count()

            assertEquals(42, count)
        }

    // -- events --

    @Test
    fun eventsReturnsPageOfMixedEvents() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                                "page": 1,
                                "page_count": 1,
                                "events": [
                                    {
                                        "type": "updated",
                                        "workout": {
                                            "id": "w-1",
                                            "title": "Updated",
                                            "routine_id": "",
                                            "description": "",
                                            "start_time": "t",
                                            "end_time": "t",
                                            "updated_at": "t",
                                            "created_at": "t",
                                            "exercises": []
                                        }
                                    },
                                    {
                                        "type": "deleted",
                                        "id": "w-2",
                                        "deleted_at": "2024-01-01T00:00:00Z"
                                    }
                                ]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val page = api.events(page = 1, pageSize = 5, since = "2024-01-01T00:00:00Z")

            assertEquals(1, page.page)
            assertEquals(1, page.pageCount)
            assertEquals(2, page.items.size)
            assertIs<WorkoutEvent.Updated>(page.items[0])
            assertIs<WorkoutEvent.Deleted>(page.items[1])
        }

    @Test
    fun eventsSendsSinceQueryParameter() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = """{"page": 1, "page_count": 1, "events": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.events(since = "2024-06-01T00:00:00Z")

            assertTrue(capturedUrl.contains("since="), "Expected since parameter in URL: $capturedUrl")
        }

    @Test
    fun eventsValidatesPageNumber() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.events(page = -1)
            }
        }

    @Test
    fun eventsValidatesBlankSince() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.events(since = "")
            }
        }

    // -- get --

    @Test
    fun getReturnsWorkoutById() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content =
                            """
                            {
                                "id": "abc-123",
                                "title": "My Workout",
                                "routine_id": "",
                                "description": "Great session",
                                "start_time": "2024-01-01T10:00:00Z",
                                "end_time": "2024-01-01T11:00:00Z",
                                "updated_at": "2024-01-01T11:00:00Z",
                                "created_at": "2024-01-01T10:00:00Z",
                                "exercises": []
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val workout = api.get("abc-123")

            assertEquals("abc-123", workout.id)
            assertEquals("My Workout", workout.title)
            assertTrue(capturedUrl.contains("/v1/workouts/abc-123"))
        }

    @Test
    fun getValidatesWorkoutId() =
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
                        content = """{"error": "Workout not found"}""",
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
    fun updateSendsPutAndReturnsWorkout() =
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
                                "id": "w-1",
                                "title": "Updated Title",
                                "routine_id": "",
                                "description": "",
                                "start_time": "2024-01-01T10:00:00Z",
                                "end_time": "2024-01-01T11:00:00Z",
                                "updated_at": "2024-01-01T12:00:00Z",
                                "created_at": "2024-01-01T10:00:00Z",
                                "exercises": []
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val request =
                CreateWorkoutRequest(
                    workout =
                        CreateWorkoutBody(
                            title = "Updated Title",
                            startTime = "2024-01-01T10:00:00Z",
                            endTime = "2024-01-01T11:00:00Z",
                            exercises = emptyList(),
                        ),
                )

            val workout = api.update("w-1", request)

            assertEquals(HttpMethod.Put, capturedMethod)
            assertTrue(capturedUrl.contains("/v1/workouts/w-1"))
            assertEquals("Updated Title", workout.title)
        }

    @Test
    fun updateValidatesWorkoutId() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)
            val request =
                CreateWorkoutRequest(
                    workout =
                        CreateWorkoutBody(
                            title = "T",
                            startTime = "t",
                            endTime = "t",
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
}
