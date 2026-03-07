package com.hevy.sdk.api

import com.hevy.sdk.HevyClientConfig
import com.hevy.sdk.common.ApiConstants
import com.hevy.sdk.common.HttpClientFactory
import com.hevy.sdk.error.HevyException
import com.hevy.sdk.model.folder.CreateRoutineFolderBody
import com.hevy.sdk.model.folder.CreateRoutineFolderRequest
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
import kotlin.test.assertTrue

class RoutineFoldersApiTest {
    private val config = HevyClientConfig(apiKey = "test-key")

    private fun createApi(engine: MockEngine): RoutineFoldersApi {
        val client = HttpClientFactory.create(config, engine)
        return RoutineFoldersApi(client, ApiConstants.BASE_URL)
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    private val sampleFolderJson =
        """
        {
            "id": 42,
            "index": 1,
            "title": "Push Pull",
            "updated_at": "2021-09-14T12:00:00Z",
            "created_at": "2021-09-14T12:00:00Z"
        }
        """.trimIndent()

    // -- list --

    @Test
    fun listReturnsPageOfFolders() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                                "page": 1,
                                "page_count": 3,
                                "routine_folders": [$sampleFolderJson]
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
            assertEquals(42, page.items[0].id)
            assertEquals("Push Pull", page.items[0].title)
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
                            """{"page": 2, "page_count": 5, "routine_folders": []}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            api.list(page = 2, pageSize = 10)

            assertTrue(capturedUrl.contains("page=2"), "Expected page=2 in URL: $capturedUrl")
            assertTrue(
                capturedUrl.contains("pageSize=10"),
                "Expected pageSize=10 in URL: $capturedUrl",
            )
            assertTrue(
                capturedUrl.contains("/v1/routine_folders"),
                "Expected /v1/routine_folders in URL: $capturedUrl",
            )
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
                api.list(pageSize = ApiConstants.MAX_PAGE_SIZE + 1)
            }
        }

    // -- create --

    @Test
    fun createSendsPostAndReturnsFolder() =
        runTest {
            var capturedMethod: HttpMethod? = null
            val engine =
                MockEngine { request ->
                    capturedMethod = request.method
                    respond(
                        content = sampleFolderJson,
                        status = HttpStatusCode.Created,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val request =
                CreateRoutineFolderRequest(
                    routineFolder = CreateRoutineFolderBody(title = "Push Pull"),
                )

            val folder = api.create(request)

            assertEquals(HttpMethod.Post, capturedMethod)
            assertEquals(42, folder.id)
            assertEquals("Push Pull", folder.title)
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
                    CreateRoutineFolderRequest(
                        routineFolder = CreateRoutineFolderBody(title = "  "),
                    ),
                )
            }
        }

    @Test
    fun createValidatesEmptyTitle() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.create(
                    CreateRoutineFolderRequest(
                        routineFolder = CreateRoutineFolderBody(title = ""),
                    ),
                )
            }
        }

    // -- get --

    @Test
    fun getReturnsFolderById() =
        runTest {
            var capturedUrl = ""
            val engine =
                MockEngine { request ->
                    capturedUrl = request.url.toString()
                    respond(
                        content = sampleFolderJson,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)
            val folder = api.get(42)

            assertEquals(42, folder.id)
            assertEquals("Push Pull", folder.title)
            assertTrue(
                capturedUrl.contains("/v1/routine_folders/42"),
                "Expected /v1/routine_folders/42 in URL: $capturedUrl",
            )
        }

    @Test
    fun getValidatesFolderId() =
        runTest {
            val engine =
                MockEngine {
                    respond(content = "", status = HttpStatusCode.OK, headers = jsonHeaders())
                }

            val api = createApi(engine)

            assertFailsWith<IllegalArgumentException> {
                api.get(0)
            }

            assertFailsWith<IllegalArgumentException> {
                api.get(-1)
            }
        }

    @Test
    fun getThrowsNotFoundOn404() =
        runTest {
            val engine =
                MockEngine {
                    respond(
                        content = """{"error": "Routine folder not found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = jsonHeaders(),
                    )
                }

            val api = createApi(engine)

            assertFailsWith<HevyException.NotFound> {
                api.get(999)
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
