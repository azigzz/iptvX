package com.iptvx.app.data.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class M3uParserTest {
    @Test
    fun parsesLargePlaylistAttributes() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="news-1" tvg-name="News 1" tvg-logo="https://img/logo.png" group-title="News" catchup="default",News 1 HD
            https://example.com/live/news1.m3u8
            #EXTINF:-1 group-title="Movies",Movie 1
            https://example.com/movie1.m3u8
        """.trimIndent()

        val result = M3uParser().parse(content)

        assertEquals(2, result.size)
        assertEquals("News 1 HD", result.first().name)
        assertEquals("news-1", result.first().tvgId)
        assertEquals("News", result.first().groupTitle)
        assertEquals("https://example.com/live/news1.m3u8", result.first().url)
    }
}
