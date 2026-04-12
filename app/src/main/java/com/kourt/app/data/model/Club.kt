package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Club(
    @DocumentId val id: String = "",
    val name: String = "",
    @PropertyName("shortName") val shortName: String = "",
    @PropertyName("logoURL") val logoURL: String = "",
    val president: String = "",
    @PropertyName("technical_director") val technicalDirector: String = "",
    val country: String = "",
    val city: String = "",
    @PropertyName("join_code") val joinCode: String = "",
    @PropertyName("created_by") val createdBy: String = "",
    @PropertyName("created_at") val createdAt: Timestamp = Timestamp.now()
)
