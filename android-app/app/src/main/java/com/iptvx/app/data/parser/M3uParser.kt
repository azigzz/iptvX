package com.iptvx.app.data.parser

import com.iptvx.app.data.model.M3uChannel

class M3uParser {
    private val attrRegex = Regex("""([a-zA-Z0-9_-]+)="([^"]*)"""")

    fun parse(content: String): List<M3uChannel> {
        val channels = ArrayList<M3uChannel>()
        var pending: PendingExtinf? = null

        content.lineSequence().forEach { raw ->
            val line = raw.trim().removePrefix("\uFEFF")
            if (line.isBlank() || line == "#EXTM3U") return@forEach

            when {
                line.startsWith("#EXTINF:") -> pending = parseExtinf(line)
                line.startsWith("#") -> Unit
                pending != null -> {
                    val extinf = pending ?: return@forEach
                    val attrs = extinf.attributes
                    channels += M3uChannel(
                        name = extinf.name.ifBlank { attrs["tvg-name"] ?: line },
                        url = line,
                        tvgId = attrs["tvg-id"],
                        tvgName = attrs["tvg-name"],
                        tvgLogo = attrs["tvg-logo"],
                        groupTitle = attrs["group-title"],
                        catchup = attrs["catchup"],
                        rawAttributes = attrs
                    )
                    pending = null
                }
            }
        }

        return channels
    }

    fun parseExtinf(line: String): PendingExtinf {
        val attributes = attrRegex.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
        val name = line.substringAfterLast(",", "").trim()
        return PendingExtinf(name = name, attributes = attributes)
    }
}

data class PendingExtinf(
    val name: String,
    val attributes: Map<String, String>
)
