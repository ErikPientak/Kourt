package com.kourt.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingMatchStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingMatchStatsEntity)

    @Query("SELECT * FROM pending_match_stats WHERE eventId = :eventId LIMIT 1")
    suspend fun getByEventId(eventId: String): PendingMatchStatsEntity?

    @Query("SELECT * FROM pending_match_stats WHERE isPendingSync = 1")
    suspend fun getAllPending(): List<PendingMatchStatsEntity>

    @Query("DELETE FROM pending_match_stats WHERE eventId = :eventId")
    suspend fun deleteByEventId(eventId: String)

    /** Deletes the row only if it still holds what was synced, so a newer save survives. */
    @Query(
        "DELETE FROM pending_match_stats WHERE eventId = :eventId " +
            "AND playerStatsJson = :playerStatsJson AND opponentScore = :opponentScore"
    )
    suspend fun deleteIfUnchanged(eventId: String, playerStatsJson: String, opponentScore: Int)
}
