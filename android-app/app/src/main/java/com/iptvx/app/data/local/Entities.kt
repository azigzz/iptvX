package com.iptvx.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    indices = [
        Index("playlistId"),
        Index("category"),
        Index("searchableName")
    ]
)
data class ChannelEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val name: String,
    val searchableName: String,
    val url: String,
    val logoUrl: String?,
    val category: String?,
    val tvgId: String?,
    val sourceType: String
)

@Entity(
    tableName = "favorites",
    primaryKeys = ["playlistId", "contentType", "contentId"],
    indices = [Index("playlistId"), Index("contentType")]
)
data class FavoriteEntity(
    val playlistId: String,
    val contentType: String,
    val contentId: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "watch_history",
    primaryKeys = ["playlistId", "contentType", "contentId"],
    indices = [Index("playlistId"), Index("updatedAt")]
)
data class WatchHistoryEntity(
    val playlistId: String,
    val contentType: String,
    val contentId: String,
    val name: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
)
