package com.iptvx.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface IptvDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY category, name LIMIT :limit OFFSET :offset")
    fun channelsForPlaylist(playlistId: String, limit: Int, offset: Int): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT DISTINCT category FROM channels
        WHERE playlistId = :playlistId
        AND ((:sourceType = 'LIVE' AND sourceType IN ('LIVE', 'XTREAM', 'M3U')) OR sourceType = :sourceType)
        ORDER BY category
        """
    )
    fun categoriesForPlaylist(playlistId: String, sourceType: String): Flow<List<String?>>

    @Query(
        """
        SELECT * FROM channels
        WHERE playlistId = :playlistId
        AND ((:sourceType = 'LIVE' AND sourceType IN ('LIVE', 'XTREAM', 'M3U')) OR sourceType = :sourceType)
        AND (:category IS NULL OR category = :category)
        ORDER BY name LIMIT :limit OFFSET :offset
        """
    )
    fun channelsByCategory(playlistId: String, category: String?, sourceType: String, limit: Int, offset: Int): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT * FROM channels
        WHERE searchableName LIKE '%' || :query || '%'
        AND ((:sourceType = 'LIVE' AND sourceType IN ('LIVE', 'XTREAM', 'M3U')) OR sourceType = :sourceType)
        ORDER BY name LIMIT 80
        """
    )
    fun searchChannels(query: String, sourceType: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun clearPlaylistChannels(playlistId: String)

    @Transaction
    suspend fun replacePlaylistChannels(playlistId: String, channels: List<ChannelEntity>) {
        clearPlaylistChannels(playlistId)
        channels.chunked(500).forEach { upsertChannels(it) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE playlistId = :playlistId AND contentType = :contentType AND contentId = :contentId")
    suspend fun removeFavorite(playlistId: String, contentType: String, contentId: String)

    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun favorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC LIMIT 50")
    fun watchHistory(): Flow<List<WatchHistoryEntity>>
}
