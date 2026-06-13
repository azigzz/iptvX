package com.iptvx.app.data.remote

import android.util.Base64
import com.iptvx.app.data.model.SeriesEpisode
import com.iptvx.app.data.model.XtreamCategory
import com.iptvx.app.data.model.XtreamProgramme
import com.iptvx.app.data.model.XtreamStream
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class XtreamClient(
    private val serverUrl: String,
    private val username: String,
    private val password: String,
    private val client: HttpClient = PanelApi.defaultClient()
) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun liveCategories(): List<XtreamCategory> {
        return get<List<CategoryDto>>("get_live_categories").map {
            XtreamCategory(id = it.categoryId, name = it.categoryName)
        }
    }

    suspend fun liveStreams(categoryId: String? = null): List<XtreamStream> {
        return get<List<StreamDto>>("get_live_streams", categoryId).map { stream ->
            XtreamStream(
                id = stream.streamId?.toString() ?: stream.name,
                name = stream.name,
                categoryId = stream.categoryId,
                streamIcon = stream.streamIcon,
                containerExtension = stream.containerExtension,
                directUrl = buildStreamUrl(stream.streamId, stream.containerExtension)
            )
        }
    }

    suspend fun vodCategories(): List<XtreamCategory> {
        return get<List<CategoryDto>>("get_vod_categories").map {
            XtreamCategory(id = it.categoryId, name = it.categoryName)
        }
    }

    suspend fun vodStreams(categoryId: String? = null): List<XtreamStream> {
        return get<List<StreamDto>>("get_vod_streams", categoryId).map { stream ->
            XtreamStream(
                id = stream.streamId?.toString() ?: stream.name,
                name = stream.name,
                categoryId = stream.categoryId,
                streamIcon = stream.streamIcon,
                containerExtension = stream.containerExtension,
                directUrl = buildMovieUrl(stream.streamId, stream.containerExtension)
            )
        }
    }

    suspend fun seriesCategories(): List<XtreamCategory> {
        return get<List<CategoryDto>>("get_series_categories").map {
            XtreamCategory(id = it.categoryId, name = it.categoryName)
        }
    }

    suspend fun series(categoryId: String? = null): List<XtreamStream> {
        return get<List<SeriesDto>>("get_series", categoryId).map { series ->
            XtreamStream(
                id = series.seriesId?.toString() ?: series.name,
                name = series.name,
                categoryId = series.categoryId,
                streamIcon = series.cover,
                containerExtension = null,
                directUrl = null
            )
        }
    }

    suspend fun shortEpg(streamId: String): List<XtreamProgramme> {
        return get<ShortEpgDto>("get_short_epg", null, streamId).epgListings.mapNotNull { listing ->
            val start = listing.startTimestamp.longValue() ?: return@mapNotNull null
            val stop = listing.stopTimestamp.longValue() ?: return@mapNotNull null
            XtreamProgramme(
                title = decodeMaybeBase64(listing.title).orEmpty().ifBlank { "Programacao" },
                description = decodeMaybeBase64(listing.description),
                startUtc = start * 1000L,
                endUtc = stop * 1000L
            )
        }
    }

    suspend fun seriesEpisodes(seriesId: String): List<SeriesEpisode> {
        val info = get<SeriesInfoDto>(action = "get_series_info", seriesId = seriesId)
        return info.episodes.flatMap { (seasonKey, episodes) ->
            val season = seasonKey.toIntOrNull() ?: 1
            episodes.jsonArrayOrEmpty().mapNotNull { element ->
                val episode = runCatching { json.decodeFromJsonElement<SeriesEpisodeDto>(element) }.getOrNull() ?: return@mapNotNull null
                val id = episode.id.stringValue() ?: return@mapNotNull null
                val number = episode.episodeNum.intValue() ?: 0
                val title = episode.title?.takeIf { it.isNotBlank() } ?: "Episodio $number"
                val extension = episode.containerExtension?.ifBlank { "mp4" } ?: "mp4"
                SeriesEpisode(
                    id = id,
                    title = title,
                    season = season,
                    episode = number,
                    url = buildSeriesUrl(id, extension),
                    imageUrl = episode.info?.movieImage,
                    plot = episode.info?.plot
                )
            }
        }.sortedWith(compareBy<SeriesEpisode> { it.season }.thenBy { it.episode }.thenBy { it.title })
    }

    private suspend inline fun <reified T> get(
        action: String,
        categoryId: String? = null,
        streamId: String? = null,
        seriesId: String? = null
    ): T {
        val url = playerApiUrl(action, categoryId, streamId, seriesId)
        return client.get(url).body()
    }

    private fun playerApiUrl(action: String, categoryId: String?, streamId: String? = null, seriesId: String? = null): String {
        val base = serverUrl.trimEnd('/')
        val params = buildString {
            append("?username=").append(username.encodeUrlParam())
            append("&password=").append(password.encodeUrlParam())
            append("&action=").append(action)
            if (!categoryId.isNullOrBlank()) append("&category_id=").append(categoryId.encodeUrlParam())
            if (!streamId.isNullOrBlank()) append("&stream_id=").append(streamId.encodeUrlParam())
            if (!seriesId.isNullOrBlank()) append("&series_id=").append(seriesId.encodeUrlParam())
        }
        return "$base/player_api.php$params"
    }

    private fun buildStreamUrl(streamId: Int?, extension: String?): String? {
        if (streamId == null) return null
        val ext = extension?.ifBlank { "m3u8" } ?: "m3u8"
        return "${serverUrl.trimEnd('/')}/live/${username.encodeUrlParam()}/${password.encodeUrlParam()}/$streamId.$ext"
    }

    private fun buildMovieUrl(streamId: Int?, extension: String?): String? {
        if (streamId == null) return null
        val ext = extension?.ifBlank { "mp4" } ?: "mp4"
        return "${serverUrl.trimEnd('/')}/movie/${username.encodeUrlParam()}/${password.encodeUrlParam()}/$streamId.$ext"
    }

    private fun buildSeriesUrl(episodeId: String, extension: String): String {
        return "${serverUrl.trimEnd('/')}/series/${username.encodeUrlParam()}/${password.encodeUrlParam()}/${episodeId.encodeUrlParam()}.$extension"
    }
}

@Serializable
private data class CategoryDto(
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryName: String
)

@Serializable
private data class StreamDto(
    @SerialName("stream_id") val streamId: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("container_extension") val containerExtension: String? = null
)

@Serializable
private data class SeriesDto(
    @SerialName("series_id") val seriesId: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("cover") val cover: String? = null
)

@Serializable
private data class ShortEpgDto(
    @SerialName("epg_listings") val epgListings: List<EpgListingDto> = emptyList()
)

@Serializable
private data class SeriesInfoDto(
    @SerialName("episodes") val episodes: JsonObject = JsonObject(emptyMap())
)

@Serializable
private data class SeriesEpisodeDto(
    @SerialName("id") val id: JsonElement? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("episode_num") val episodeNum: JsonElement? = null,
    @SerialName("container_extension") val containerExtension: String? = null,
    @SerialName("info") val info: SeriesEpisodeInfoDto? = null
)

@Serializable
private data class SeriesEpisodeInfoDto(
    @SerialName("movie_image") val movieImage: String? = null,
    @SerialName("plot") val plot: String? = null
)

@Serializable
private data class EpgListingDto(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("start_timestamp") val startTimestamp: JsonElement? = null,
    @SerialName("stop_timestamp") val stopTimestamp: JsonElement? = null
)

private fun String.encodeUrlParam(): String = java.net.URLEncoder.encode(this, "UTF-8")

private fun JsonElement?.longValue(): Long? = this?.jsonPrimitive?.contentOrNull?.toLongOrNull()

private fun JsonElement?.intValue(): Int? = this?.jsonPrimitive?.intOrNull

private fun JsonElement?.stringValue(): String? = this?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

private fun JsonElement.jsonArrayOrEmpty() = runCatching { jsonArray }.getOrDefault(emptyList())

private fun decodeMaybeBase64(value: String?): String? {
    val text = value?.trim().orEmpty()
    if (text.isBlank()) return null
    return runCatching {
        String(Base64.decode(text, Base64.DEFAULT), Charsets.UTF_8)
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: text
}
