package com.iptvx.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iptvx.app.data.IptvRepository
import com.iptvx.app.data.shortNumericDeviceId
import com.iptvx.app.data.local.AppPreferences
import com.iptvx.app.data.local.ChannelEntity
import com.iptvx.app.data.local.IptvDatabase
import com.iptvx.app.data.model.ContentType
import com.iptvx.app.data.model.NowPlayingInfo
import com.iptvx.app.data.model.PlaybackItem
import com.iptvx.app.data.model.PlaylistConfig
import com.iptvx.app.data.model.SeriesEpisode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview

data class IptvUiState(
    val loading: Boolean = false,
    val preferencesLoaded: Boolean = false,
    val initialLoadComplete: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val deviceId: String = "",
    val displayDeviceId: String = "",
    val virtualMac: String? = null,
    val pairingCode: String? = null,
    val panelUrl: String = BuildConfig.DEFAULT_PANEL_URL,
    val paired: Boolean = false,
    val playlists: List<PlaylistConfig> = emptyList(),
    val selectedPlaylist: PlaylistConfig? = null,
    val contentType: ContentType = ContentType.LIVE,
    val categories: List<String?> = emptyList(),
    val selectedCategory: String? = null,
    val channels: List<ChannelEntity> = emptyList(),
    val searchQuery: String = "",
    val nowPlaying: NowPlayingInfo? = null,
    val selectedSeries: ChannelEntity? = null,
    val seriesEpisodes: List<SeriesEpisode> = emptyList(),
    val seriesLoading: Boolean = false,
    val performanceMode: Boolean = true
)

@OptIn(FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)
    private val repository = IptvRepository(
        preferences = preferences,
        dao = IptvDatabase.get(application).dao()
    )
    private val _uiState = MutableStateFlow(IptvUiState(loading = true))
    val uiState = _uiState.asStateFlow()
    private val searchFlow = MutableStateFlow("")
    private var categoriesJob: Job? = null
    private var channelsJob: Job? = null
    private var syncJob: Job? = null
    private var startupSyncStarted = false

    init {
        viewModelScope.launch {
            repository.preferencesState.collectLatest { prefs ->
                val current = _uiState.value
                val shouldUseCachedPlaylists = current.playlists.isEmpty() && prefs.cachedPlaylists.isNotEmpty()
                val cachedSelected = if (shouldUseCachedPlaylists) prefs.cachedPlaylists.firstOrNull() else current.selectedPlaylist
                val readyFromCache = prefs.cachedPlaylists.isNotEmpty()
                val shouldRefreshBeforeHome = readyFromCache || prefs.paired
                val readyForPairing = !shouldRefreshBeforeHome
                _uiState.update {
                    it.copy(
                        deviceId = prefs.deviceId,
                        displayDeviceId = shortNumericDeviceId(prefs.deviceId),
                        virtualMac = prefs.virtualMac,
                        pairingCode = prefs.pairingCode,
                        panelUrl = prefs.panelUrl,
                        paired = prefs.paired,
                        playlists = if (shouldUseCachedPlaylists) prefs.cachedPlaylists else it.playlists,
                        selectedPlaylist = if (shouldUseCachedPlaylists) cachedSelected else it.selectedPlaylist,
                        preferencesLoaded = true,
                        initialLoadComplete = it.initialLoadComplete || readyForPairing,
                        performanceMode = prefs.performanceMode
                    )
                }
                if (shouldUseCachedPlaylists && cachedSelected != null) {
                    observeContent(cachedSelected, _uiState.value.contentType)
                }
                if (shouldRefreshBeforeHome && !startupSyncStarted) {
                    startupSyncStarted = true
                    syncNow(silent = false, startup = true)
                }
            }
        }
        viewModelScope.launch {
            registerDevice()
        }
        viewModelScope.launch {
            searchFlow.debounce(250).collectLatest { query ->
                if (query.isBlank()) return@collectLatest
                repository.searchChannels(query, _uiState.value.contentType).collectLatest { channels ->
                    _uiState.update { it.copy(channels = channels, selectedCategory = "Busca") }
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(if (_uiState.value.playlists.isEmpty()) 15 * 1000L else 60 * 1000L)
                syncNow(silent = true)
            }
        }
    }

    fun setPanelUrl(url: String) {
        viewModelScope.launch {
            preferences.savePanelUrl(url)
            registerDevice()
        }
    }

    fun registerDevice() {
        viewModelScope.launch {
            if (syncJob?.isActive != true) {
                _uiState.update { it.copy(loading = true, error = null) }
            }
            repository.registerOrRefresh()
                .onSuccess {
                    _uiState.update {
                        if (syncJob?.isActive == true && !it.initialLoadComplete) {
                            it
                        } else {
                            it.copy(loading = false, message = "Dispositivo registrado. Use o codigo exibido para parear.")
                        }
                    }
                }
                .onFailure { error ->
                    if (syncJob?.isActive != true) {
                        _uiState.update { it.copy(loading = false, initialLoadComplete = true, error = friendlyError(error)) }
                    }
                }
        }
    }

    fun syncNow(silent: Boolean = false, startup: Boolean = false) {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            val stateBeforeSync = _uiState.value
            val startupNeedsList = startup || (!stateBeforeSync.initialLoadComplete && stateBeforeSync.playlists.isEmpty())
            if (startupNeedsList) {
                _uiState.update {
                    it.copy(
                        initialLoadComplete = false,
                        loading = true,
                        error = null,
                        message = "Atualizando canais, filmes e series..."
                    )
                }
            } else if (!silent) {
                _uiState.update {
                    it.copy(
                        loading = true,
                        error = null,
                        message = "Sincronizando..."
                    )
                }
            }
            val panelResult = repository.syncFromPanel()
            val result = if (panelResult.isSuccess) {
                panelResult
            } else {
                repository.refreshSavedPlaylists()
            }
            result
                .onSuccess { playlists ->
                    val usedSavedPlaylists = panelResult.isFailure
                    val previous = _uiState.value.selectedPlaylist
                    val selected = repository.preferredPlayablePlaylist(playlists, previous?.id)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            initialLoadComplete = true,
                            playlists = playlists,
                            selectedPlaylist = selected,
                            message = when {
                                playlists.isEmpty() -> "Nenhuma lista ativa encontrada."
                                usedSavedPlaylists -> "Lista salva atualizada."
                                else -> "Sync concluido."
                            }
                        )
                    }
                    selected?.let { selectPlaylist(it) }
                }
                .onFailure { error ->
                    val failure = panelResult.exceptionOrNull() ?: error
                    val currentPlaylists = _uiState.value.playlists
                    val hasCachedContent = repository.hasCachedContent(currentPlaylists)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            initialLoadComplete = true,
                            playlists = if (hasCachedContent) it.playlists else emptyList(),
                            selectedPlaylist = if (hasCachedContent) it.selectedPlaylist else null,
                            categories = if (hasCachedContent) it.categories else emptyList(),
                            channels = if (hasCachedContent) it.channels else emptyList(),
                            error = if (startup && hasCachedContent) null else friendlyError(failure),
                            message = if (startup && hasCachedContent) "Usando lista salva." else "Falha ao carregar lista."
                        )
                    }
                }
        }
    }

    fun addManualM3u(name: String, url: String, epgUrl: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            repository.addManualM3u(name, url, epgUrl)
                .onSuccess { playlist ->
                    val playlists = (_uiState.value.playlists + playlist).distinctBy { it.id }
                    repository.rememberPlaylists(playlists)
                    _uiState.update {
                        it.copy(loading = false, playlists = playlists, selectedPlaylist = playlist, message = "Lista manual adicionada.")
                    }
                    selectPlaylist(playlist)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(loading = false, error = friendlyError(error)) }
                }
        }
    }

    fun addManualXtream(name: String, serverUrl: String, username: String, password: String, epgUrl: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, message = "Entrando...") }
            repository.addManualXtream(name, normalizeServerUrl(serverUrl), username, password, epgUrl)
                .onSuccess { playlist ->
                    val playlists = (_uiState.value.playlists + playlist).distinctBy { it.id }
                    repository.rememberPlaylists(playlists)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            playlists = playlists,
                            selectedPlaylist = playlist,
                            message = "Login concluido. Canais carregados."
                        )
                    }
                    selectPlaylist(playlist)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(loading = false, error = friendlyError(error)) }
                }
        }
    }

    fun selectPlaylist(playlist: PlaylistConfig) {
        _uiState.update { it.copy(selectedPlaylist = playlist, selectedCategory = null, searchQuery = "") }
        observeContent(playlist, _uiState.value.contentType)
    }

    fun openContent(contentType: ContentType) {
        val playlist = _uiState.value.selectedPlaylist ?: _uiState.value.playlists.firstOrNull()
        _uiState.update {
            it.copy(
                contentType = contentType,
                selectedPlaylist = playlist,
                selectedCategory = null,
                searchQuery = "",
                channels = emptyList()
            )
        }
        playlist?.let { observeContent(it, contentType) }
    }

    fun ensureContentReady(contentType: ContentType) {
        val state = _uiState.value
        if (state.contentType != contentType || state.channels.isEmpty()) {
            openContent(contentType)
        }
    }

    private fun observeContent(playlist: PlaylistConfig, contentType: ContentType) {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            repository.categories(playlist.id, contentType).collectLatest { categories ->
                val selected = _uiState.value.selectedCategory?.takeIf { it in categories } ?: categories.firstOrNull()
                _uiState.update { it.copy(categories = categories, selectedCategory = selected) }
                selectCategory(selected)
            }
        }
    }

    fun selectCategory(category: String?) {
        val playlist = _uiState.value.selectedPlaylist ?: return
        val contentType = _uiState.value.contentType
        channelsJob?.cancel()
        channelsJob = viewModelScope.launch {
            repository.channelsByCategory(playlist.id, contentType, category).collectLatest { channels ->
                _uiState.update { it.copy(selectedCategory = category, channels = channels, searchQuery = "") }
            }
        }
    }

    fun search(query: String) {
        channelsJob?.cancel()
        _uiState.update { it.copy(searchQuery = query) }
        searchFlow.value = query
        if (query.isBlank()) {
            selectCategory(_uiState.value.selectedCategory)
        }
    }

    fun playbackItem(channel: ChannelEntity): PlaybackItem {
        return PlaybackItem(
            id = channel.id,
            title = channel.name,
            url = channel.url,
            playlistId = channel.playlistId,
            contentType = channel.contentType(),
            logoUrl = channel.logoUrl,
            category = channel.category,
            tvgId = channel.tvgId
        )
    }

    fun loadSeriesEpisodes(series: ChannelEntity) {
        val seriesId = series.tvgId ?: series.url.removePrefix("series://")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedSeries = series,
                    seriesEpisodes = emptyList(),
                    seriesLoading = true,
                    error = null
                )
            }
            repository.seriesEpisodes(_uiState.value.selectedPlaylist, seriesId)
                .onSuccess { episodes ->
                    _uiState.update {
                        it.copy(
                            seriesEpisodes = episodes,
                            seriesLoading = false,
                            message = if (episodes.isEmpty()) "Nenhum episodio encontrado." else null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(seriesLoading = false, error = friendlyError(error)) }
                }
        }
    }

    fun playbackItem(series: ChannelEntity, episode: SeriesEpisode): PlaybackItem {
        return PlaybackItem(
            id = "${series.id}:episode:${episode.id}",
            title = "${series.name} - ${episode.title}",
            url = episode.url,
            playlistId = series.playlistId,
            contentType = ContentType.SERIES,
            logoUrl = episode.imageUrl ?: series.logoUrl,
            category = "T${episode.season} E${episode.episode}".takeIf { episode.episode > 0 } ?: "Serie",
            tvgId = episode.id
        )
    }

    fun loadNowPlaying(item: PlaybackItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(nowPlaying = null) }
            repository.nowPlaying(_uiState.value.selectedPlaylist, item)
                .onSuccess { info -> _uiState.update { it.copy(nowPlaying = info) } }
        }
    }

    fun saveProgress(item: PlaybackItem, positionMs: Long, durationMs: Long) {
        viewModelScope.launch { repository.saveProgress(item, positionMs, durationMs) }
    }

    fun setPerformanceMode(enabled: Boolean) {
        viewModelScope.launch { preferences.savePerformanceMode(enabled) }
    }

    private fun friendlyError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("Unable to resolve host", true) -> "Sem conexao ou painel indisponivel."
            message.contains("401") -> "Dispositivo nao autorizado. Registre novamente."
            message.contains("404") -> "Servidor nao respondeu como esperado."
            message.contains("timeout", true) -> "Servidor nao respondeu."
            else -> message.ifBlank { "Falha inesperada." }
        }
    }

    private fun normalizeServerUrl(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
    }

    private fun ChannelEntity.contentType(): ContentType {
        return runCatching { ContentType.valueOf(sourceType) }.getOrDefault(ContentType.LIVE)
    }
}
