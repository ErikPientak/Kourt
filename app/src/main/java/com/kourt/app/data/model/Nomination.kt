package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Nomination(
    @DocumentId val id: String = "",
    val eventId: String = "",
    val players: List<String> = emptyList(),
    val status: String = "",
    @PropertyName("created_by") val createdBy: String = "",
    @PropertyName("created_at") val createdAt: Timestamp = Timestamp.now(),
    @PropertyName("updated_at") val updatedAt: Timestamp = Timestamp.now()
)
