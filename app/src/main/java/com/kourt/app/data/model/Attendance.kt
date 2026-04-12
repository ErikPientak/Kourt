package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Attendance(
    @DocumentId val id: String = "",
    val status: String = "",
    @PropertyName("recorded_by") val recordedBy: String = "",
    @PropertyName("recorded_at") val recordedAt: Timestamp = Timestamp.now()
)
