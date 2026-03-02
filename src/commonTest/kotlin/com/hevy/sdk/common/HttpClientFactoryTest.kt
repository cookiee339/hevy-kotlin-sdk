package com.hevy.sdk.common

import com.hevy.sdk.HevyClientConfig
import com.hevy.sdk.error.HevyException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HttpClientFactoryTest {

    @Test
    fun addsApiKeyHeaderToEveryRequest() = runTest {
        var capturedApiKey: String? = null
        val engine = MockEngine { request ->
            capturedApiKey = request.headers[ApiConstants.API_KEY_HEADER]
            respond(
                content = """{"ok": true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val config = HevyClientConfig(apiKey = "my-test-key")
        val client = HttpClientFactory.create(config, engine)

        client.get("${ApiConstants.BASE_URL}/v1/test")

        assertEquals("my-test-key", capturedApiKey)
    }

    @Test
    fun setsJsonContentNegotiation() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"value": "hello"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val config = HevyClientConfig(apiKey = "key")
        val client = HttpClientFactory.create(config, engine)

        // If JSON content negotiation is NOT installed, this would fail
        val response = client.get("${ApiConstants.BASE_URL}/v1/test")
        val body = response.bodyAsText()

        assertTrue(body.contains("hello"))
    }

    @Test
    fun throwsHevyExceptionOn401() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"error": "Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val config = HevyClientConfig(apiKey = "bad-key")
        val client = HttpClientFactory.create(config, engine)

        assertFailsWith<HevyException.Unauthorized> {
            client.get("${ApiConstants.BASE_URL}/v1/test")
        }
    }

    @Test
    fun throwsHevyExceptionOn500() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"error": "Internal server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val config = HevyClientConfig(apiKey = "key")
        val client = HttpClientFactory.create(config, engine)

        assertFailsWith<HevyException.ServerError> {
            client.get("${ApiConstants.BASE_URL}/v1/test")
        }
    }

    @Test
    fun usesProvidedHttpClientWhenConfigured() = runTest {
        var customClientUsed = false
        val customEngine = MockEngine {
            customClientUsed = true
            respond(
                content = """{}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val customClient = HttpClient(customEngine)

        val config = HevyClientConfig(apiKey = "key", httpClient = customClient)

        // When a custom client is provided, createOrUse should return it
        val client = HttpClientFactory.createOrUse(config)
        client.get("https://example.com/test")

        assertTrue(customClientUsed)
    }
}
