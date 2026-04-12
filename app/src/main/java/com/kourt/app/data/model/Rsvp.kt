package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Rsvp(
    @DocumentId val id: String = "",
    val status: String = "",
    val reason: String = "",
    val reasonNote: String = "",
    @PropertyName("submitted_by") val submittedBy: String = "",
    @PropertyName("submitted_at") val submittedAt: Timestamp = Timestamp.now()
)
