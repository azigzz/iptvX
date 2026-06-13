package com.iptvx.app.data

import android.os.Build
import com.iptvx.app.BuildConfig
import com.iptvx.app.data.local.AppPreferences
import com.iptvx.app.data.local.ChannelEntity
import com.iptvx.app.data.local.FavoriteEntity
import com.iptvx.app.data.local.IptvDao
import com.iptvx.app.data.local.WatchHistoryEntity
import com.iptvx.app.data.model.ContentType
import com.iptvx.app.data.model.NowPlayingInfo
import com.iptvx.app.data.model.PlaybackItem
import com.iptvx.app.data.model.PlaylistConfig
import com.iptvx.app.data.model.PlaylistType
import com.iptvx.app.data.model.RegisterDeviceRequest
import com.iptvx.app.data.model.SeriesEpisode
import com.iptvx.app.data.parser.M3uParser
import com.iptvx.app.data.remote.PanelApi
import com.iptvx.app.data.remote.XtreamClient
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class IptvRepository(
    private val preferences: AppPreferences,
    private val dao: IptvDao,
    private val api: PanelApi = PanelApi(),
    private val m3uParser: M3uParser = M3uParser()
) {
    val preferencesState = preferences.state

    suspend fun registerOrRefresh(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val deviceId = preferences.ensureDeviceId()
            val prefs = preferences.state.first()
            val response = api.register(
                prefs.panelUrl,
                RegisterDeviceRequest(
                    deviceId = deviceId,
                    model = Build.MODEL,
                    manufacturer = Build.MANUFACTURER,
                    androidVersion = Build.VERSION.RELEASE,
                    appVersion = BuildConfig.VERSION_NAME
                )
            )
            preferences.saveRegistration(
                virtualMac = response.virtualMac,
                pairingCode = response.pairingCode,
                panelUrl = response.panelUrl,
                paired = response.paired,
                deviceToken = response.deviceToken
            )
        }
    }

    suspend fun syncFromPanel(): Result<List<PlaylistConfig>> = withContext(Dispatchers.IO) {
        runCatching {
            val prefs = preferences.state.first()
            val token = requireNotNull(prefs.deviceToken) { "Token do dispositivo ausente. Registre novamente." }
            val response = api.sync(prefs.panelUrl, prefs.deviceId, token)
            preferences.markPaired()
            preferences.savePlaylists(response.playlists)
            response.playlists.forEach { playlist ->
                runCatching { cachePlaylistContent(playlist) }
            }
            response.playlists
        }
    }

    suspend fun addManualM3u(name: String, m3uUrl: String, epgUrl: String?): Result<PlaylistConfig> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val playlist = PlaylistConfig(
                    id = "local-${stableId(m3uUrl)}",
                    name = name,
                    type = PlaylistType.M3U,
                    m3uUrl = m3uUrl,
                    epgUrl = epgUrl
                )
                cachePlaylistContent(playlist)
                playlist
            }
        }
    }

    suspend fun addManualXtream(
        name: String,
        serverUrl: String,
        username: String,
        password: String,
        epgUrl: String?
    ): Result<PlaylistConfig> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val playlist = PlaylistConfig(
                    id = "local-xtream-${stableId("$serverUrl|$username")}",
                    name = name.ifBlank { username },
                    type = PlaylistType.XTREAM,
                    serverUrl = serverUrl.trim(),
                    username = username.trim(),
                    password = password,
                    epgUrl = epgUrl
                )
                cachePlaylistContent(playlist)
                playlist
            }
        }
    }

    suspend fun cachePlaylistContent(playlist: PlaylistConfig) {
        when (playlist.type) {
            PlaylistType.M3U -> cacheM3u(playlist)
            PlaylistType.XTREAM -> cacheXtream(playlist)
        }
    }

    suspend fun rememberPlaylists(playlists: List<PlaylistConfig>) {
        preferences.savePlaylists(playlists)
    }

    private suspend fun cacheM3u(playlist: PlaylistConfig) {
        val url = playlist.m3uUrl ?: return
        val content = api.downloadText(url)
        val parsed = m3uParser.parse(content)
        val channels = parsed.map { channel ->
            ChannelEntity(
                id = "${playlist.id}:${stableId(channel.url)}",
                playlistId = playlist.id,
                name = channel.name,
                searchableName = channel.name.lowercase(),
                url = channel.url,
                logoUrl = channel.tvgLogo,
                category = channel.groupTitle ?: "Sem categoria",
                tvgId = channel.tvgId,
                sourceType = ContentType.LIVE.name
            )
        }
        dao.replacePlaylistChannels(playlist.id, channels)
    }

    private suspend fun cacheXtream(playlist: PlaylistConfig) {
        val serverUrl = playlist.serverUrl ?: return
        val username = playlist.username ?: return
        val password = playlist.password ?: return
        val client = XtreamClient(serverUrl, username, password)
        val categoryNames = runCatching {
            client.liveCategories().associate { it.id to it.name }
        }.getOrDefault(emptyMap())
        val streams = client.liveStreams()
        val liveChannels = streams.mapNotNull { stream ->
            val url = stream.directUrl ?: return@mapNotNull null
            ChannelEntity(
                id = "${playlist.id}:${stream.id}",
                playlistId = playlist.id,
                name = stream.name,
                searchableName = stream.name.lowercase(),
                url = url,
                logoUrl = stream.streamIcon,
                category = stream.categoryId?.let { categoryNames[it] ?: it } ?: "Live",
                tvgId = stream.id,
                sourceType = ContentType.LIVE.name
            )
        }

        val movieCategoryNames = runCatching {
            client.vodCategories().associate { it.id to it.name }
        }.getOrDefault(emptyMap())
        val movies = runCatching { client.vodStreams() }.getOrDefault(emptyList())
        val movieChannels = movies.mapNotNull { stream ->
            val url = stream.directUrl ?: return@mapNotNull null
            ChannelEntity(
                id = "${playlist.id}:movie:${stream.id}",
                playlistId = playlist.id,
                name = stream.name,
                searchableName = stream.name.lowercase(),
                url = url,
                logoUrl = stream.streamIcon,
                category = stream.categoryId?.let { movieCategoryNames[it] ?: it } ?: "Filmes",
                tvgId = stream.id,
                sourceType = ContentType.MOVIE.name
            )
        }

        val seriesCategoryNames = runCatching {
            client.seriesCategories().associate { it.id to it.name }
        }.getOrDefault(emptyMap())
        val series = runCatching { client.series() }.getOrDefault(emptyList())
        val seriesChannels = series.map { item ->
            ChannelEntity(
                id = "${playlist.id}:series:${item.id}",
                playlistId = playlist.id,
                name = item.name,
                searchableName = item.name.lowercase(),
                url = "series://${item.id}",
                logoUrl = item.streamIcon,
                category = item.categoryId?.let { seriesCategoryNames[it] ?: it } ?: "Series",
                tvgId = item.id,
                sourceType = ContentType.SERIES.name
            )
        }
        dao.replacePlaylistChannels(playlist.id, liveChannels + movieChannels + seriesChannels)
    }

    fun categories(playlistId: String, contentType: ContentType): Flow<List<String?>> =
        dao.categoriesForPlaylist(playlistId, contentType.name)

    fun channelsByCategory(
        playlistId: String,
        contentType: ContentType,
        category: String?,
        limit: Int = 250,
        offset: Int = 0
    ): Flow<List<ChannelEntity>> = dao.channelsByCategory(playlistId, category, contentType.name, limit, offset)

    fun searchChannels(query: String, contentType: ContentType): Flow<List<ChannelEntity>> =
        dao.searchChannels(query.lowercase(), contentType.name)

    suspend fun nowPlaying(playlist: PlaylistConfig?, item: PlaybackItem): Result<NowPlayingInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            if (playlist?.type != PlaylistType.XTREAM || item.contentType != ContentType.LIVE) return@runCatching null
            val serverUrl = playlist.serverUrl ?: return@runCatching null
            val username = playlist.username ?: return@runCatching null
            val password = playlist.password ?: return@runCatching null
            val streamId = item.tvgId ?: return@runCatching null
            val programmes = XtreamClient(serverUrl, username, password).shortEpg(streamId)
            val now = System.currentTimeMillis()
            val current = programmes.firstOrNull { now in it.startUtc until it.endUtc } ?: programmes.firstOrNull()
            current?.let {
                NowPlayingInfo(
                    title = it.title,
                    description = it.description,
                    startUtc = it.startUtc,
                    endUtc = it.endUtc
                )
            }
        }
    }

    suspend fun seriesEpisodes(playlist: PlaylistConfig?, seriesId: String): Result<List<SeriesEpisode>> = withContext(Dispatchers.IO) {
        runCatching {
            if (playlist?.type != PlaylistType.XTREAM) return@runCatching emptyList()
            val serverUrl = playlist.serverUrl ?: return@runCatching emptyList()
            val username = playlist.username ?: return@runCatching emptyList()
            val password = playlist.password ?: return@runCatching emptyList()
            XtreamClient(serverUrl, username, password).seriesEpisodes(seriesId)
        }
    }

    fun favorites() = dao.favorites()

    fun history() = dao.watchHistory()

    suspend fun toggleFavorite(item: PlaybackItem, enabled: Boolean) {
        if (enabled) {
            dao.upsertFavorite(
                FavoriteEntity(
                    playlistId = item.playlistId,
                    contentType = item.contentType.name,
                    contentId = item.id,
                    name = item.title
                )
            )
        } else {
            dao.removeFavorite(item.playlistId, item.contentType.name, item.id)
        }
    }

    suspend fun saveProgress(item: PlaybackItem, positionMs: Long, durationMs: Long) {
        dao.upsertHistory(
            WatchHistoryEntity(
                playlistId = item.playlistId,
                contentType = item.contentType.name,
                contentId = item.id,
                name = item.title,
                positionMs = positionMs,
                durationMs = durationMs
            )
        )
    }

    private fun stableId(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .take(10)
            .joinToString("") { "%02x".format(it) }
    }
}
