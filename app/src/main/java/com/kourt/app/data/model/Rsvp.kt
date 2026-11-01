package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Rsvp(
    @DocumentId val id: String = "",
    val eventId: String = "",
    /** Who the RSVP is for: the player, or the child a parent answers for. */
    val userId: String = "",
    val status: String = "",
    val reason: String = "",
    val reasonNote: String = "",
    /** Who tapped it; differs from [userId] when a parent answers for their child. */
    val submittedBy: String = "",
    val submittedAt: Timestamp = Timestamp.now()
)
