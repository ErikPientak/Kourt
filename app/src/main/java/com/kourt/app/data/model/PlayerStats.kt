package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class PlayerStats(
    @DocumentId val id: String = "",
    val teamMemberId: String = "",
    val teamId: String = "",
    val matchesPlayed: Int = 0,
    val totalPoints: Int = 0,
    val totalRebounds: Int = 0,
    val totalAssists: Int = 0,
    val totalFouls: Int = 0,
    val totalMinutes: Double = 0.0,
    val totalFreeThrowsAttempted: Int = 0,
    val totalFreeThrowsMade: Int = 0,
    val totalTrainings: Int = 0,
    val totalTrainingsOnTime: Int = 0,
    val totalTrainingsLate: Int = 0,
    val totalTrainingsExcused: Int = 0,
    val totalTrainingsUnexcused: Int = 0,
    val attendanceRate: Float = 0f,
    @PropertyName("updated_at") val updatedAt: Timestamp = Timestamp.now()
)
