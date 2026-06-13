package com.iptvx.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvx.app.IptvUiState
import com.iptvx.app.MainViewModel
import com.iptvx.app.R
import com.iptvx.app.data.model.ContentType
import com.iptvx.app.data.model.PlaybackItem

private enum class Screen {
    BOOT,
    PAIRING,
    HOME,
    LIVE,
    PLAYER,
    MANUAL,
    SETTINGS,
    VOD,
    SERIES,
    SERIES_DETAIL,
    EPG,
    FAVORITES,
    HISTORY
}

@Composable
fun IptvApp(state: IptvUiState, viewModel: MainViewModel) {
    var screen by remember { mutableStateOf(Screen.BOOT) }
    var playerItem by remember { mutableStateOf<PlaybackItem?>(null) }
    var returnScreen by remember { mutableStateOf(Screen.LIVE) }

    LaunchedEffect(state.preferencesLoaded, state.initialLoadComplete, state.playlists.size) {
        if (!state.preferencesLoaded || !state.initialLoadComplete) return@LaunchedEffect
        val hasHome = state.playlists.isNotEmpty()
        if (screen == Screen.BOOT || (screen == Screen.PAIRING && hasHome)) {
            screen = if (hasHome) Screen.HOME else Screen.PAIRING
        }
    }

    BackHandler(enabled = screen != Screen.HOME && screen != Screen.PAIRING && screen != Screen.BOOT) {
        screen = Screen.HOME
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBackdrop()
        when (screen) {
            Screen.BOOT -> BootScreen(state = state)
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
                    onRefresh = { viewModel.syncNow() },
                    onLive = {
                        viewModel.openContent(ContentType.LIVE)
                        screen = Screen.LIVE
                    },
                    onVod = {
                        viewModel.openContent(ContentType.MOVIE)
                        screen = Screen.VOD
                    },
                    onSeries = {
                        viewModel.openContent(ContentType.SERIES)
                        screen = Screen.SERIES
                    },
                    onSettings = { screen = Screen.SETTINGS }
                )
            }
            Screen.LIVE -> {
                LaunchedEffect(state.selectedPlaylist?.id) {
                    viewModel.ensureContentReady(ContentType.LIVE)
                }
                PaddedScreen { LiveTvScreen(
                        state = state,
                        onBack = { screen = Screen.HOME },
                        onRefresh = { viewModel.syncNow() },
                        onCategory = viewModel::selectCategory,
                        onSearch = viewModel::search,
                        onPlay = {
                            playerItem = viewModel.playbackItem(it)
                            viewModel.loadNowPlaying(playerItem!!)
                            returnScreen = Screen.LIVE
                            screen = Screen.PLAYER
                        }
                    )
                }
            }
            Screen.VOD -> {
                LaunchedEffect(state.selectedPlaylist?.id) {
                    viewModel.ensureContentReady(ContentType.MOVIE)
                }
                PaddedScreen { CatalogScreen(
                        title = "Filmes",
                        icon = R.drawable.ic_movies,
                        state = state,
                        emptyText = "Nenhum filme sincronizado ainda.",
                        onBack = { screen = Screen.HOME },
                        onRefresh = { viewModel.syncNow() },
                        onCategory = viewModel::selectCategory,
                        onSearch = viewModel::search,
                        onOpen = {
                            playerItem = viewModel.playbackItem(it)
                            returnScreen = Screen.VOD
                            screen = Screen.PLAYER
                        }
                    )
                }
            }
            Screen.SERIES -> {
                LaunchedEffect(state.selectedPlaylist?.id) {
                    viewModel.ensureContentReady(ContentType.SERIES)
                }
                PaddedScreen { CatalogScreen(
                        title = "Series",
                        icon = R.drawable.ic_series,
                        state = state,
                        emptyText = "Nenhuma serie sincronizada ainda.",
                        onBack = { screen = Screen.HOME },
                        onRefresh = { viewModel.syncNow() },
                        onCategory = viewModel::selectCategory,
                        onSearch = viewModel::search,
                        onOpen = {
                            viewModel.loadSeriesEpisodes(it)
                            screen = Screen.SERIES_DETAIL
                        }
                    )
                }
            }
            Screen.SERIES_DETAIL -> PaddedScreen { SeriesDetailScreen(
                    state = state,
                    onBack = { screen = Screen.SERIES },
                    onRefresh = { state.selectedSeries?.let { viewModel.loadSeriesEpisodes(it) } },
                    onPlay = { episode ->
                        state.selectedSeries?.let { series ->
                            playerItem = viewModel.playbackItem(series, episode)
                            returnScreen = Screen.SERIES_DETAIL
                            screen = Screen.PLAYER
                        }
                    }
                )
            }
            Screen.PLAYER -> playerItem?.let { item ->
                LaunchedEffect(item.id) {
                    viewModel.loadNowPlaying(item)
                }
                PlayerScreen(
                    item = item,
                    channels = state.channels,
                    nowPlaying = state.nowPlaying,
                    onBack = { screen = returnScreen },
                    onProgress = viewModel::saveProgress,
                    onSwitch = { channel ->
                        playerItem = viewModel.playbackItem(channel)
                        viewModel.loadNowPlaying(playerItem!!)
                        returnScreen = Screen.LIVE
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
            Screen.EPG -> PaddedScreen { PlaceholderScreen("EPG", R.drawable.ic_epg, onBack = { screen = Screen.HOME }) }
            Screen.FAVORITES -> PaddedScreen { PlaceholderScreen("Favoritos", R.drawable.ic_favorite, onBack = { screen = Screen.HOME }) }
            Screen.HISTORY -> PaddedScreen { PlaceholderScreen("Historico", R.drawable.ic_history, onBack = { screen = Screen.HOME }) }
        }
    }
}

@Composable
private fun BootScreen(state: IptvUiState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Image(painter = painterResource(R.drawable.brand_mark), contentDescription = null, modifier = Modifier.size(82.dp))
        Text("IPTVX", color = Color(0xFFF4D21B), fontSize = 44.sp, fontWeight = FontWeight.Black)
        Text(
            state.message ?: if (state.preferencesLoaded) "Carregando listas..." else "Preparando app...",
            color = Color(0xFFC9D6E2),
            fontSize = 16.sp
        )
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
