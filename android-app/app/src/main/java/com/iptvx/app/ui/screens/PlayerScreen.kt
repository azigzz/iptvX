package com.iptvx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.iptvx.app.data.local.ChannelEntity
import com.iptvx.app.data.model.PlaybackItem
import com.iptvx.app.player.BufferMode
import com.iptvx.app.player.PlayerFactory
import com.iptvx.app.player.PlayerFactory.playUrl

@Composable
fun PlayerScreen(
    item: PlaybackItem,
    channels: List<ChannelEntity>,
    onBack: () -> Unit,
    onProgress: (PlaybackItem, Long, Long) -> Unit,
    onSwitch: (ChannelEntity) -> Unit
) {
    val context = LocalContext.current
    val player = remember(item.url) {
        PlayerFactory.create(context, BufferMode.MEDIUM).apply { playUrl(item.url) }
    }

    LaunchedEffect(item.url) {
        player.playUrl(item.url)
    }

    DisposableEffect(player, item) {
        onDispose {
            onProgress(item, player.currentPosition.coerceAtLeast(0L), player.duration.coerceAtLeast(0L))
            player.release()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    event.key == Key.DirectionUp -> {
                        switchRelative(item, channels, 1, onSwitch)
                        true
                    }
                    event.key == Key.DirectionDown -> {
                        switchRelative(item, channels, -1, onSwitch)
                        true
                    }
                    event.key == Key.Back -> {
                        onBack()
                        true
                    }
                    else -> false
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = true
                    this.player = player
                }
            },
            update = { it.player = player }
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp),
            color = Color(0xCC101821)
        ) {
            Text(item.title, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        }
    }
}

private fun switchRelative(
    item: PlaybackItem,
    channels: List<ChannelEntity>,
    offset: Int,
    onSwitch: (ChannelEntity) -> Unit
) {
    if (channels.isEmpty()) return
    val index = channels.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: 0
    val next = (index + offset + channels.size) % channels.size
    onSwitch(channels[next])
}
