package com.iptvx.app.data.remote

import com.iptvx.app.data.model.XtreamCategory
import com.iptvx.app.data.model.XtreamStream
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class XtreamClient(
    private val serverUrl: String,
    private val username: String,
    private val password: String,
    private val client: HttpClient = PanelApi.defaultClient()
) {
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

    private suspend inline fun <reified T> get(action: String, categoryId: String? = null): T {
        val url = playerApiUrl(action, categoryId)
        return client.get(url).body()
    }

    private fun playerApiUrl(action: String, categoryId: String?): String {
        val base = serverUrl.trimEnd('/')
        val params = buildString {
            append("?username=").append(username.encodeUrlParam())
            append("&password=").append(password.encodeUrlParam())
            append("&action=").append(action)
            if (!categoryId.isNullOrBlank()) append("&category_id=").append(categoryId.encodeUrlParam())
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

private fun String.encodeUrlParam(): String = java.net.URLEncoder.encode(this, "UTF-8")
