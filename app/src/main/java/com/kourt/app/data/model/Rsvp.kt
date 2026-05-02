package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Rsvp(
    @DocumentId val id: String = "",
    val eventId: String = "",
    val status: String = "",
    val reason: String = "",
    val reasonNote: String = "",
    val submittedBy: String = "",
    val submittedAt: Timestamp = Timestamp.now()
)
