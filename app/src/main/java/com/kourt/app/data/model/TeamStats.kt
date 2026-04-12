package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class TeamStats(
    @DocumentId val id: String = "",
    val teamId: String = "",
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val totalPointsScored: Int = 0,
    val totalPointsAgainst: Int = 0,
    val totalTrainings: Int = 0,
    @PropertyName("updated_at") val updatedAt: Timestamp = Timestamp.now()
)
