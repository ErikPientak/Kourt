package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class TeamMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val teamId: String = "",
    @PropertyName("jersey_number") val jerseyNumber: Int = 0,
    val role: String = "",
    @PropertyName("joined_at") val joinedAt: Timestamp = Timestamp.now()
)
