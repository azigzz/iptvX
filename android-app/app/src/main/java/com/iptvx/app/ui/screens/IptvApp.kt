package com.iptvx.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iptvx.app.IptvUiState
import com.iptvx.app.MainViewModel
import com.iptvx.app.R
import com.iptvx.app.data.model.PlaybackItem

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

    LaunchedEffect(state.paired) {
        if (state.paired && screen == Screen.PAIRING) screen = Screen.HOME
    }

    LaunchedEffect(state.playlists.size) {
        if (screen == Screen.PAIRING && state.playlists.isNotEmpty()) screen = Screen.HOME
    }

    BackHandler(enabled = screen != Screen.HOME && screen != Screen.PAIRING) {
        screen = Screen.HOME
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D12))
    ) {
        when (screen) {
            Screen.PAIRING -> PairingScreen(
                state = state,
                onSync = { viewModel.syncNow() },
                onRefreshCode = { viewModel.registerDevice() },
                onXtreamLogin = { serverUrl, username, password ->
                    viewModel.addManualXtream("Xtream", serverUrl, username, password)
                },
                onManual = { screen = Screen.MANUAL },
                onSettings = { screen = Screen.SETTINGS }
            )
            Screen.HOME -> PaddedScreen { HomeScreen(
                    state = state,
                    onLive = { screen = Screen.LIVE },
                    onVod = { screen = Screen.VOD },
                    onSeries = { screen = Screen.SERIES },
                    onSettings = { screen = Screen.SETTINGS }
                )
            }
            Screen.LIVE -> PaddedScreen { LiveTvScreen(
                    state = state,
                    onBack = { screen = Screen.HOME },
                    onCategory = viewModel::selectCategory,
                    onSearch = viewModel::search,
                    onPlay = {
                        playerItem = viewModel.playbackItem(it)
                        screen = Screen.PLAYER
                    }
                )
            }
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
            Screen.MANUAL -> PaddedScreen { ManualPlaylistScreen(
                    loading = state.loading,
                    onBack = { screen = if (state.paired) Screen.HOME else Screen.PAIRING },
                    onSave = { name, url, epg -> viewModel.addManualM3u(name, url, epg) }
                )
            }
            Screen.SETTINGS -> PaddedScreen { SettingsScreen(
                    state = state,
                    onBack = { screen = Screen.HOME },
                    onPanelUrl = viewModel::setPanelUrl,
                    onPerformanceMode = viewModel::setPerformanceMode,
                    onManual = { screen = Screen.MANUAL },
                    onPairing = { screen = Screen.PAIRING },
                    onEpg = { screen = Screen.EPG },
                    onFavorites = { screen = Screen.FAVORITES },
                    onHistory = { screen = Screen.HISTORY },
                    onSync = { viewModel.syncNow() }
                )
            }
            Screen.VOD -> PaddedScreen { PlaceholderScreen("Filmes", R.drawable.ic_movies, onBack = { screen = Screen.HOME }) }
            Screen.SERIES -> PaddedScreen { PlaceholderScreen("Series", R.drawable.ic_series, onBack = { screen = Screen.HOME }) }
            Screen.EPG -> PaddedScreen { PlaceholderScreen("EPG", R.drawable.ic_epg, onBack = { screen = Screen.HOME }) }
            Screen.FAVORITES -> PaddedScreen { PlaceholderScreen("Favoritos", R.drawable.ic_favorite, onBack = { screen = Screen.HOME }) }
            Screen.HISTORY -> PaddedScreen { PlaceholderScreen("Historico", R.drawable.ic_history, onBack = { screen = Screen.HOME }) }
        }
    }
}

@Composable
private fun PaddedScreen(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        content()
    }
}
