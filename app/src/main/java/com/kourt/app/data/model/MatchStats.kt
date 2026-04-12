package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class PlayerMatchStat(
    val points: Int = 0,
    val rebounds: Int = 0,
    val assists: Int = 0,
    val fouls: Int = 0,
    val minutesPlayed: Double = 0.0,
    val freeThrowsAttempted: Int = 0,
    val freeThrowsMade: Int = 0,
)

data class MatchStats(
    @DocumentId val id: String = "",
    val eventId: String = "",
    val myTeamScore: Int = 0,
    val opponentScore: Int = 0,
    val playerStats: Map<String, PlayerMatchStat> = emptyMap(),
    @PropertyName("created_by") val createdBy: String = "",
    @PropertyName("created_at") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("updated_at") val updatedAt: Timestamp = Timestamp.now()
)
