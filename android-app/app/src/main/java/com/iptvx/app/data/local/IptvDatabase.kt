package com.iptvx.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ChannelEntity::class, FavoriteEntity::class, WatchHistoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class IptvDatabase : RoomDatabase() {
    abstract fun dao(): IptvDao

    companion object {
        @Volatile private var instance: IptvDatabase? = null

        fun get(context: Context): IptvDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IptvDatabase::class.java,
                    "iptvx.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
