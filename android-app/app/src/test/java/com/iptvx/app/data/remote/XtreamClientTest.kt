package com.iptvx.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class XtreamClientTest {
    @Test
    fun fetchesLiveStreamsFromPlayerApi() = runTest {
        val seenUrls = mutableListOf<String>()
        val engine = MockEngine { request ->
            seenUrls += request.url.toString()
            respond(
                content = """[{"stream_id":12,"name":"Canal","category_id":"1","stream_icon":"https://img/icon.png","container_extension":"m3u8"}]""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val xtream = XtreamClient("https://example.com", "user", "pass", client)
        val streams = xtream.liveStreams()

        assertEquals("Canal", streams.first().name)
        assertEquals("https://example.com/live/user/pass/12.m3u8", streams.first().directUrl)
        assertTrue(seenUrls.first().contains("action=get_live_streams"))
    }
}
