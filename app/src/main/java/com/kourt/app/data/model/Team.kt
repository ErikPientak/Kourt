package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Team(
    @DocumentId val id: String = "",
    val clubId: String = "",
    val name: String = "",
    val category: String = "",
    val headCoach: String = "",
    val location: String = "",
    val arena: String = "",
    val joinCode: String = "",
    @PropertyName("created_by") val createdBy: String = "",
    @PropertyName("created_at") val createdAt: Timestamp = Timestamp.now(),
    val accentColor: String = "",
    val initials: String = "",
)
