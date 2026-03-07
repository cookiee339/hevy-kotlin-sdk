package com.hevy.sdk.error

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ResponseExtensionsTest {
    private fun mockClient(
        status: HttpStatusCode,
        body: String = "",
        headers: Map<String, String> = emptyMap(),
    ): HttpClient {
        val engine =
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers =
                        headersOf(
                            HttpHeaders.ContentType to listOf("application/json"),
                            *headers.map { (k, v) -> k to listOf(v) }.toTypedArray(),
                        ),
                )
            }
        return HttpClient(engine)
    }

    @Test
    fun successResponseDoesNotThrow() =
        runTest {
            val client = mockClient(HttpStatusCode.OK, """{"data": "ok"}""")

            val response: HttpResponse = client.get("https://example.com/test")

            response.ensureSuccess() // should not throw
        }

    @Test
    fun status401ThrowsUnauthorized() =
        runTest {
            val client = mockClient(HttpStatusCode.Unauthorized, """{"error": "Invalid API key"}""")

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.Unauthorized> {
                    response.ensureSuccess()
                }

            assertEquals("Invalid API key", ex.message)
        }

    @Test
    fun status400ThrowsBadRequest() =
        runTest {
            val client = mockClient(HttpStatusCode.BadRequest, """{"error": "Invalid page size"}""")

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.BadRequest> {
                    response.ensureSuccess()
                }

            assertEquals("Invalid page size", ex.message)
        }

    @Test
    fun status400WithNoBodyThrowsBadRequest() =
        runTest {
            val client = mockClient(HttpStatusCode.BadRequest)

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.BadRequest> {
                    response.ensureSuccess()
                }

            assertEquals("Bad request", ex.message)
        }

    @Test
    fun status404ThrowsNotFound() =
        runTest {
            val client = mockClient(HttpStatusCode.NotFound)

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.NotFound> {
                    response.ensureSuccess()
                }

            assertEquals("Not found", ex.message)
        }

    @Test
    fun status429ThrowsRateLimitedWithRetryAfter() =
        runTest {
            val client =
                mockClient(
                    HttpStatusCode.TooManyRequests,
                    headers = mapOf("Retry-After" to "60"),
                )

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.RateLimited> {
                    response.ensureSuccess()
                }

            assertEquals(60, ex.retryAfterSeconds)
        }

    @Test
    fun status429WithoutRetryAfterHeader() =
        runTest {
            val client = mockClient(HttpStatusCode.TooManyRequests)

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.RateLimited> {
                    response.ensureSuccess()
                }

            assertNull(ex.retryAfterSeconds)
        }

    @Test
    fun status500ThrowsServerError() =
        runTest {
            val client = mockClient(HttpStatusCode.InternalServerError, """{"error": "Oops"}""")

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.ServerError> {
                    response.ensureSuccess()
                }

            assertEquals(500, ex.statusCode)
        }

    @Test
    fun status502ThrowsServerError() =
        runTest {
            val client = mockClient(HttpStatusCode.BadGateway)

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.ServerError> {
                    response.ensureSuccess()
                }

            assertEquals(502, ex.statusCode)
        }

    @Test
    fun status418ThrowsUnknown() =
        runTest {
            val client = mockClient(HttpStatusCode(418, "I'm a teapot"))

            val response = client.get("https://example.com/test")
            val ex =
                assertFailsWith<HevyException.Unknown> {
                    response.ensureSuccess()
                }

            assertEquals(418, ex.statusCode)
        }
}
