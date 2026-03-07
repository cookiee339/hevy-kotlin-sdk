package com.hevy.sdk.api

import com.hevy.sdk.HevyClientConfig
import com.hevy.sdk.common.ApiConstants
import com.hevy.sdk.common.HttpClientFactory
import com.hevy.sdk.error.HevyException
import com.hevy.sdk.model.common.SetType
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExerciseHistoryApiTest {
    private val config = HevyClientConfig(apiKey = "test-key")

    private fun createApi(engine: MockEngine): ExerciseHistoryApi {
        val client = HttpClientFactory.create(config, engine)
        return ExerciseHistoryApi(client, ApiConstants.BASE_URL)
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    private val sampleEntryJson =
        """
        {
            "workout_id": "abc-123",
            "workout_title": "Morning Workout",
            "workout_start_time": "2024-01-01T12:00:00Z",
            "workout_end_time": "2024-01-01T13:00:00Z",
            "exercise_template_id": "D04AC939",
            "weight_kg": 100.0,
            "reps": 10,
            "distance_meters": null,
            "duration_seconds": null,
            "rpe": 8.5,
            "custom_metric": null,
            "set_type": "normal"
        }
        """.trimIndent()

    @Test
    fun getByTemplateIdReturnsList() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """{"exercise_history": [$sampleEntryJson]}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val entries = api.getByTemplateId("D04AC939")

            assertEquals(1, entries.size)
            assertEquals("abc-123", entries[0].workoutId)
            assertEquals("Morning Workout", entries[0].workoutTitle)
            assertEquals(100.0, entries[0].weightKg)
            assertEquals(10, entries[0].reps)
            assertNull(entries[0].distanceMeters)
            assertEquals(8.5, entries[0].rpe)
            assertEquals(SetType.NORMAL, entries[0].setType)
        }

    @Test
    fun getByTemplateIdReturnsEmptyList() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"exercise_history": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val entries = api.getByTemplateId("D04AC939")

            assertTrue(entries.isEmpty())
        }

    @Test
    fun getByTemplateIdSendsCorrectUrl() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = """{"exercise_history": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.getByTemplateId("D04AC939")

            assertTrue(
                capturedUrl.contains("/v1/exercise_history/D04AC939"),
                "Expected /v1/exercise_history/D04AC939 in URL: $capturedUrl",
            )
        }

    @Test
    fun getByTemplateIdSendsDateFilters() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = """{"exercise_history": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.getByTemplateId(
                exerciseTemplateId = "D04AC939",
                startDate = "2024-01-01T00:00:00Z",
                endDate = "2024-12-31T23:59:59Z",
            )

            assertTrue(
                capturedUrl.contains("start_date=2024-01-01"),
                "Expected start_date param in URL: $capturedUrl",
            )
            assertTrue(
                capturedUrl.contains("end_date=2024-12-31"),
                "Expected end_date param in URL: $capturedUrl",
            )
        }

    @Test
    fun getByTemplateIdOmitsNullDateFilters() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = """{"exercise_history": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.getByTemplateId("D04AC939")

            assertTrue(
                !capturedUrl.contains("start_date"),
                "Expected no start_date param in URL: $capturedUrl",
            )
            assertTrue(
                !capturedUrl.contains("end_date"),
                "Expected no end_date param in URL: $capturedUrl",
            )
        }

    @Test
    fun getByTemplateIdRejectsBlankStartDate() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"exercise_history": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.getByTemplateId("D04AC939", startDate = "  ")
            }
        }

    @Test
    fun getByTemplateIdRejectsBlankEndDate() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"exercise_history": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.getByTemplateId("D04AC939", endDate = "")
            }
        }

    @Test
    fun getByTemplateIdValidatesId() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.getByTemplateId("")
            }
        }

    @Test
    fun throwsNotFoundOn404() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error": "Exercise template not found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.NotFound> {
                api.getByTemplateId("nonexistent-id")
            }
        }

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
                api.getByTemplateId("D04AC939")
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
                api.getByTemplateId("D04AC939")
            }
        }
}
