package com.iptvx.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iptvx.app.IptvUiState
import com.iptvx.app.MainViewModel
import com.iptvx.app.data.model.PlaybackItem
import kotlinx.coroutines.launch

private enum class Screen {
    PAIRING,
    HOME,
    LIVE,
    PLAYER,
    MANUAL,
    SETTINGS,
    VOD,
    SERIES,
    EPG,
    FAVORITES,
    HISTORY
}

@Composable
fun IptvApp(state: IptvUiState, viewModel: MainViewModel) {
    var screen by remember { mutableStateOf(if (state.paired) Screen.HOME else Screen.PAIRING) }
    var playerItem by remember { mutableStateOf<PlaybackItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.message, state.error) {
        val text = state.error ?: state.message
        if (!text.isNullOrBlank()) snackbarHostState.showSnackbar(text)
    }

    LaunchedEffect(state.paired) {
        if (state.paired && screen == Screen.PAIRING) screen = Screen.HOME
    }

    BackHandler(enabled = screen != Screen.HOME && screen != Screen.PAIRING) {
        screen = Screen.HOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D12))
            .padding(24.dp)
    ) {
        when (screen) {
            Screen.PAIRING -> PairingScreen(
                state = state,
                onSync = { viewModel.syncNow() },
                onRefreshCode = { viewModel.registerDevice() },
                onXtreamLogin = { serverUrl, username, password ->
                    viewModel.addManualXtream("Xtream", serverUrl, username, password)
                    screen = Screen.HOME
                },
                onManual = { screen = Screen.MANUAL },
                onSettings = { screen = Screen.SETTINGS }
            )
            Screen.HOME -> HomeScreen(
                state = state,
                onSync = { viewModel.syncNow() },
                onPairing = { screen = Screen.PAIRING },
                onManual = { screen = Screen.MANUAL },
                onLive = { screen = Screen.LIVE },
                onVod = { screen = Screen.VOD },
                onSeries = { screen = Screen.SERIES },
                onEpg = { screen = Screen.EPG },
                onFavorites = { screen = Screen.FAVORITES },
                onHistory = { screen = Screen.HISTORY },
                onSettings = { screen = Screen.SETTINGS },
                onPlaylistSelected = { viewModel.selectPlaylist(it) }
            )
            Screen.LIVE -> LiveTvScreen(
                state = state,
                onBack = { screen = Screen.HOME },
                onCategory = viewModel::selectCategory,
                onSearch = viewModel::search,
                onPlay = {
                    playerItem = viewModel.playbackItem(it)
                    screen = Screen.PLAYER
                }
            )
            Screen.PLAYER -> playerItem?.let { item ->
                PlayerScreen(
                    item = item,
                    channels = state.channels,
                    onBack = { screen = Screen.LIVE },
                    onProgress = viewModel::saveProgress,
                    onSwitch = { channel ->
                        playerItem = viewModel.playbackItem(channel)
                    }
                )
            } ?: run { screen = Screen.LIVE }
            Screen.MANUAL -> ManualPlaylistScreen(
                loading = state.loading,
                onBack = { screen = if (state.paired) Screen.HOME else Screen.PAIRING },
                onSave = { name, url, epg -> viewModel.addManualM3u(name, url, epg) }
            )
            Screen.SETTINGS -> SettingsScreen(
                state = state,
                onBack = { screen = Screen.HOME },
                onPanelUrl = viewModel::setPanelUrl,
                onPerformanceMode = viewModel::setPerformanceMode
            )
            Screen.VOD -> PlaceholderScreen("Filmes", onBack = { screen = Screen.HOME })
            Screen.SERIES -> PlaceholderScreen("Series", onBack = { screen = Screen.HOME })
            Screen.EPG -> PlaceholderScreen("EPG", onBack = { screen = Screen.HOME })
            Screen.FAVORITES -> PlaceholderScreen("Favoritos", onBack = { screen = Screen.HOME })
            Screen.HISTORY -> PlaceholderScreen("Historico", onBack = { screen = Screen.HOME })
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(24.dp)
        )
    }
}
