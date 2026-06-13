package com.iptvx.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.iptvx.app.R
import com.iptvx.app.data.local.ChannelEntity
import com.iptvx.app.data.model.ContentType
import com.iptvx.app.data.model.NowPlayingInfo
import com.iptvx.app.data.model.PlaybackItem
import com.iptvx.app.player.BufferMode
import com.iptvx.app.player.PlayerFactory
import com.iptvx.app.player.PlayerFactory.playUrl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    item: PlaybackItem,
    channels: List<ChannelEntity>,
    nowPlaying: NowPlayingInfo?,
    onBack: () -> Unit,
    onProgress: (PlaybackItem, Long, Long) -> Unit,
    onSwitch: (ChannelEntity) -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val player = remember(item.url) {
        PlayerFactory.create(context, BufferMode.MEDIUM).apply { playUrl(item.url) }
    }
    var overlayVisible by remember(item.id) { mutableStateOf(true) }
    var clockTick by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(item.url) {
        player.playUrl(item.url)
    }

    LaunchedEffect(overlayVisible, item.id) {
        if (overlayVisible) {
            delay(5_000)
            overlayVisible = false
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            clockTick = System.currentTimeMillis()
            delay(1_000)
        }
    }

    DisposableEffect(player, item) {
        onDispose {
            onProgress(item, player.currentPosition.coerceAtLeast(0L), player.duration.coerceAtLeast(0L))
            player.release()
        }
    }

    fun revealOverlay() {
        overlayVisible = true
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                        revealOverlay()
                        true
                    }
                    Key.DirectionUp -> {
                        if (item.contentType == ContentType.LIVE) {
                            switchRelative(item, channels, 1, onSwitch)
                            revealOverlay()
                            true
                        } else {
                            false
                        }
                    }
                    Key.DirectionDown -> {
                        if (item.contentType == ContentType.LIVE) {
                            switchRelative(item, channels, -1, onSwitch)
                            revealOverlay()
                            true
                        } else {
                            false
                        }
                    }
                    Key.Back -> {
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
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                }
            },
            update = { it.player = player }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(item.id) {
                    detectTapGestures(onTap = { revealOverlay() })
                }
        )
        if (overlayVisible) {
            PlayerInfoOverlay(
                item = item,
                nowPlaying = nowPlaying,
                nowMs = clockTick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlayerInfoOverlay(
    item: PlaybackItem,
    nowPlaying: NowPlayingInfo?,
    nowMs: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xCC05070A), Color(0xF205070A))
                )
            )
            .padding(start = 32.dp, end = 32.dp, bottom = 28.dp, top = 82.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xE6111824)),
            border = BorderStroke(1.dp, Color(0x664DD7FF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ChannelArtwork(item = item)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LiveBadge(enabled = item.contentType == ContentType.LIVE)
                        Text(
                            item.category ?: "IPTVX",
                            color = Color(0xFF9BC8FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        item.title,
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        nowPlaying?.title ?: if (item.contentType == ContentType.LIVE) "Sem EPG para este canal" else "Reproduzindo agora",
                        color = Color(0xFFEFF5FF),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        nowPlaying?.description?.takeIf { it.isNotBlank() } ?: programWindow(nowPlaying),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    ProgramProgress(nowPlaying = nowPlaying, nowMs = nowMs)
                }
                Column(
                    modifier = Modifier.width(132.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(currentClock(nowMs), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text(programWindow(nowPlaying), color = Color(0xFFB7C3D6), fontSize = 13.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ChannelArtwork(item: PlaybackItem) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF111B2B), Color(0xFF244B72), Color(0xFF0FD0A2)))),
        contentAlignment = Alignment.Center
    ) {
        if (item.logoUrl.isNullOrBlank()) {
            Image(
                painter = painterResource(if (item.contentType == ContentType.MOVIE) R.drawable.ic_movies else R.drawable.ic_live_tv),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        } else {
            AsyncImage(
                model = item.logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun LiveBadge(enabled: Boolean) {
    Surface(
        color = if (enabled) Color(0xFFE92E5B) else Color(0xFF32547C),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            if (enabled) "AO VIVO" else "PLAY",
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ProgramProgress(nowPlaying: NowPlayingInfo?, nowMs: Long) {
    val progress = nowPlaying.progress(nowMs)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF263241))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Brush.horizontalGradient(listOf(Color(0xFF10E0A0), Color(0xFF39A7FF), Color(0xFFFFD447))))
        )
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

private fun NowPlayingInfo?.progress(nowMs: Long): Float {
    val start = this?.startUtc ?: return 0f
    val end = this.endUtc ?: return 0f
    val duration = (end - start).takeIf { it > 0 } ?: return 0f
    return ((nowMs - start).toFloat() / duration.toFloat()).coerceIn(0f, 1f)
}

private fun currentClock(nowMs: Long): String = timeFormatter().format(Date(nowMs))

private fun programWindow(nowPlaying: NowPlayingInfo?): String {
    val start = nowPlaying?.startUtc
    val end = nowPlaying?.endUtc
    return if (start != null && end != null) {
        "${timeFormatter().format(Date(start))} - ${timeFormatter().format(Date(end))}"
    } else {
        "Agora"
    }
}

private fun timeFormatter(): SimpleDateFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
