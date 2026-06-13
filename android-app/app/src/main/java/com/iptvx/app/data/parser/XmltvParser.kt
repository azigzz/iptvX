package com.iptvx.app.data.parser

import com.iptvx.app.data.model.EpgProgramme
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class XmltvParser {
    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun parse(xml: String, maxItems: Int = 20_000): List<EpgProgramme> {
        val programmes = ArrayList<EpgProgramme>()
        val factory = SAXParserFactory.newInstance()
        val parser = factory.newSAXParser()

        parser.parse(xml.byteInputStream(), object : DefaultHandler() {
            private var inProgramme = false
            private var currentChannel = ""
            private var currentStart = 0L
            private var currentEnd = 0L
            private var currentElement = ""
            private var title = StringBuilder()
            private var desc = StringBuilder()

            override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                currentElement = qName.orEmpty()
                if (qName == "programme" && programmes.size < maxItems) {
                    inProgramme = true
                    currentChannel = attributes?.getValue("channel").orEmpty()
                    currentStart = parseDate(attributes?.getValue("start"))
                    currentEnd = parseDate(attributes?.getValue("stop"))
                    title = StringBuilder()
                    desc = StringBuilder()
                }
            }

            override fun characters(ch: CharArray?, start: Int, length: Int) {
                if (!inProgramme || ch == null) return
                when (currentElement) {
                    "title" -> title.append(ch, start, length)
                    "desc" -> desc.append(ch, start, length)
                }
            }

            override fun endElement(uri: String?, localName: String?, qName: String?) {
                if (qName == "programme" && inProgramme) {
                    programmes += EpgProgramme(
                        channelId = currentChannel,
                        channelName = null,
                        title = title.toString().trim(),
                        description = desc.toString().trim().ifBlank { null },
                        startUtc = currentStart,
                        endUtc = currentEnd
                    )
                    inProgramme = false
                }
                currentElement = ""
            }
        })

        return programmes
    }

    private fun parseDate(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        return runCatching { dateFormat.parse(raw)?.time ?: 0L }.getOrDefault(0L)
    }
}
