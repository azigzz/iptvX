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
import com.iptvx.app.data.model.PlaybackItem
import com.iptvx.app.data.model.PlaylistConfig
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
    val categories: List<String?> = emptyList(),
    val selectedCategory: String? = null,
    val channels: List<ChannelEntity> = emptyList(),
    val searchQuery: String = "",
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

    init {
        viewModelScope.launch {
            repository.preferencesState.collectLatest { prefs ->
                _uiState.update {
                    it.copy(
                        deviceId = prefs.deviceId,
                        displayDeviceId = shortNumericDeviceId(prefs.deviceId),
                        virtualMac = prefs.virtualMac,
                        pairingCode = prefs.pairingCode,
                        panelUrl = prefs.panelUrl,
                        paired = prefs.paired,
                        performanceMode = prefs.performanceMode
                    )
                }
            }
        }
        viewModelScope.launch {
            registerDevice()
        }
        viewModelScope.launch {
            searchFlow.debounce(250).collectLatest { query ->
                if (query.isBlank()) return@collectLatest
                repository.searchChannels(query).collectLatest { channels ->
                    _uiState.update { it.copy(channels = channels, selectedCategory = "Busca") }
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(60 * 1000L)
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
            _uiState.update { it.copy(loading = true, error = null) }
            repository.registerOrRefresh()
                .onSuccess {
                    _uiState.update {
                        it.copy(loading = false, message = "Dispositivo registrado. Use o codigo exibido para parear.")
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(loading = false, error = friendlyError(error)) }
                }
        }
    }

    fun syncNow(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _uiState.update { it.copy(loading = true, error = null, message = "Sincronizando...") }
            repository.syncFromPanel()
                .onSuccess { playlists ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            playlists = playlists,
                            selectedPlaylist = playlists.firstOrNull(),
                            message = if (playlists.isEmpty()) "Nenhuma lista ativa encontrada." else "Sync concluido."
                        )
                    }
                    playlists.firstOrNull()?.let { selectPlaylist(it) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(loading = false, error = friendlyError(error)) }
                }
        }
    }

    fun addManualM3u(name: String, url: String, epgUrl: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            repository.addManualM3u(name, url, epgUrl)
                .onSuccess { playlist ->
                    val playlists = (_uiState.value.playlists + playlist).distinctBy { it.id }
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
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            repository.categories(playlist.id).collectLatest { categories ->
                val firstCategory = categories.firstOrNull()
                _uiState.update { it.copy(categories = categories, selectedCategory = firstCategory) }
                selectCategory(firstCategory)
            }
        }
    }

    fun selectCategory(category: String?) {
        val playlist = _uiState.value.selectedPlaylist ?: return
        channelsJob?.cancel()
        channelsJob = viewModelScope.launch {
            repository.channelsByCategory(playlist.id, category).collectLatest { channels ->
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
            contentType = ContentType.LIVE,
            logoUrl = channel.logoUrl,
            category = channel.category
        )
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
}
