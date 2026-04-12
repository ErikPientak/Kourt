package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Event(
    @DocumentId val id: String = "",
    val teamId: String = "",
    val title: String = "",
    val seriesId: String = "",
    val type: String = "",
    val venueType: String = "",
    val opponent: String = "",
    val status: String = "",
    val date: Timestamp = Timestamp.now(),
    val location: String = "",
    @PropertyName("start_time") val startTime: String = "",
    @PropertyName("end_time") val endTime: String = "",
    val notes: String = "",
    @PropertyName("created_by") val createdBy: String = ""
)
