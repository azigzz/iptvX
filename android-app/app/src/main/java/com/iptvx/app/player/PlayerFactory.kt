package com.iptvx.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer

enum class BufferMode(val minMs: Int, val maxMs: Int, val playbackMs: Int, val rebufferMs: Int) {
    LOW(1_000, 3_000, 300, 600),
    MEDIUM(1_500, 5_000, 500, 900),
    HIGH(2_500, 5_000, 700, 1_100)
}

object PlayerFactory {
    fun create(context: Context, bufferMode: BufferMode = BufferMode.MEDIUM): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                bufferMode.minMs,
                bufferMode.maxMs,
                bufferMode.playbackMs,
                bufferMode.rebufferMs
            )
            .build()

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
    }

    fun ExoPlayer.playUrl(url: String) {
        setMediaItem(MediaItem.fromUri(url))
        prepare()
        playWhenReady = true
    }
}
