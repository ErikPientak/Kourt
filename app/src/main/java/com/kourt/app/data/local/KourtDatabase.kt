package com.kourt.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PendingMatchStatsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class KourtDatabase : RoomDatabase() {
    abstract fun pendingMatchStatsDao(): PendingMatchStatsDao
}
