package com.hevy.sdk.api

import com.hevy.sdk.HevyClientConfig
import com.hevy.sdk.common.ApiConstants
import com.hevy.sdk.common.HttpClientFactory
import com.hevy.sdk.error.HevyException
import com.hevy.sdk.model.common.EquipmentCategory
import com.hevy.sdk.model.common.ExerciseType
import com.hevy.sdk.model.common.MuscleGroup
import com.hevy.sdk.model.exercise.CreateExerciseTemplateBody
import com.hevy.sdk.model.exercise.CreateExerciseTemplateRequest
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExerciseTemplatesApiTest {
    private val config = HevyClientConfig(apiKey = "test-key")

    private fun createApi(engine: MockEngine): ExerciseTemplatesApi {
        val client = HttpClientFactory.create(config, engine)
        return ExerciseTemplatesApi(client, ApiConstants.BASE_URL)
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    private val sampleTemplateJson =
        """
        {
            "id": "b459cba5-cd6d-463c-abd6-54f8eafcadcb",
            "title": "Bench Press (Barbell)",
            "type": "weight_reps",
            "primary_muscle_group": "chest",
            "secondary_muscle_groups": ["triceps", "shoulders"],
            "is_custom": false
        }
        """.trimIndent()

    // -- list --

    @Test
    fun listReturnsPageOfTemplates() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                                "page": 1,
                                "page_count": 10,
                                "exercise_templates": [$sampleTemplateJson]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val page = api.list(page = 1, pageSize = 5)

            assertEquals(1, page.page)
            assertEquals(10, page.pageCount)
            assertEquals(1, page.items.size)
            assertEquals("Bench Press (Barbell)", page.items[0].title)
            assertEquals(ExerciseType.WEIGHT_REPS, page.items[0].type)
            assertEquals(MuscleGroup.CHEST, page.items[0].primaryMuscleGroup)
            assertFalse(page.items[0].isCustom)
        }

    @Test
    fun listSendsCorrectQueryParameters() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content =
                            """{"page": 3, "page_count": 20, "exercise_templates": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.list(page = 3, pageSize = 50)

            assertTrue(capturedUrl.contains("page=3"), "Expected page=3 in URL: $capturedUrl")
            assertTrue(
                capturedUrl.contains("pageSize=50"),
                "Expected pageSize=50 in URL: $capturedUrl",
            )
            assertTrue(
                capturedUrl.contains("/v1/exercise_templates"),
                "Expected /v1/exercise_templates in URL: $capturedUrl",
            )
        }

    @Test
    fun listAllowsPageSizeUpTo100() =
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

            val api = createApi(engine)
            val page = api.list(pageSize = 100)

            assertEquals(1, page.page)
        }

    @Test
    fun listRejectsPageSizeOver100() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.list(pageSize = 101)
            }
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

    // -- create --

    @Test
    fun createSendsPostAndReturnsId() =
        runTest {
            var capturedMethod: HttpMethod? = null
            val engine =
                MockEngine { request ->
                    capturedMethod = request.method
                    respond(
                        content = """{"id": 456}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val request =
                CreateExerciseTemplateRequest(
                    exercise =
                        CreateExerciseTemplateBody(
                            title = "My Custom Press",
                            exerciseType = ExerciseType.WEIGHT_REPS,
                            equipmentCategory = EquipmentCategory.BARBELL,
                            muscleGroup = MuscleGroup.CHEST,
                            otherMuscles = listOf(MuscleGroup.TRICEPS),
                        ),
                )

            val response = api.create(request)

            assertEquals(HttpMethod.Post, capturedMethod)
            assertEquals(456, response.id)
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
                    CreateExerciseTemplateRequest(
                        exercise =
                            CreateExerciseTemplateBody(
                                title = "  ",
                                exerciseType = ExerciseType.WEIGHT_REPS,
                                equipmentCategory = EquipmentCategory.BARBELL,
                                muscleGroup = MuscleGroup.CHEST,
                            ),
                    ),
                )
            }
        }

    // -- get --

    @Test
    fun getReturnsTemplateById() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = sampleTemplateJson,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val template = api.get("b459cba5-cd6d-463c-abd6-54f8eafcadcb")

            assertEquals("b459cba5-cd6d-463c-abd6-54f8eafcadcb", template.id)
            assertEquals("Bench Press (Barbell)", template.title)
            assertTrue(
                capturedUrl.contains(
                    "/v1/exercise_templates/b459cba5-cd6d-463c-abd6-54f8eafcadcb",
                ),
            )
        }

    @Test
    fun getValidatesTemplateId() =
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
                        content = """{"error": "Exercise template not found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.NotFound> {
                api.get("nonexistent-id")
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
    fun createThrowsUnknownOn403() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error": "exceeds-custom-exercise-limit"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.Unknown> {
                api.create(
                    CreateExerciseTemplateRequest(
                        exercise =
                            CreateExerciseTemplateBody(
                                title = "Too many",
                                exerciseType = ExerciseType.REPS_ONLY,
                                equipmentCategory = EquipmentCategory.NONE,
                                muscleGroup = MuscleGroup.OTHER,
                            ),
                    ),
                )
            }
        }
}
