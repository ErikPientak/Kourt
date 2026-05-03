package com.kourt.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_match_stats")
data class PendingMatchStatsEntity(
    @PrimaryKey val eventId: String,
    val opponentScore: Int,
    val playerStatsJson: String,
    val createdBy: String,
    val createdAtSeconds: Long,
    val teamId: String,
    val isPendingSync: Boolean,
)
